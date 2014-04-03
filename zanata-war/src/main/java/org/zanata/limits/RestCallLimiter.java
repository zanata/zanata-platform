package org.zanata.limits;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class RestCallLimiter {
    private volatile Semaphore maxConcurrentSemaphore;
    private volatile Semaphore maxActiveSemaphore;
    private int maxConcurrent;
    private int maxActive;

    RestCallLimiter(int maxConcurrent, int maxActive) {
        this.maxConcurrent = maxConcurrent;
        this.maxActive = maxActive;
        this.maxConcurrentSemaphore = makeSemaphore(maxConcurrent);
        this.maxActiveSemaphore = makeSemaphore(maxActive);
    }

    /**
     * May throw an exception if it takes too long to obtain one of the
     * semaphores
     *
     * @param taskAfterAcquire
     *            task to perform after acquire
     */
    public boolean tryAcquireAndRun(Runnable taskAfterAcquire) {
        // hang on to the semaphore, so that we can be certain of
        // releasing the same one we acquired
        final Semaphore concSem = maxConcurrentSemaphore;
        boolean gotConcurrentPermit = concSem.tryAcquire();
        if (gotConcurrentPermit) {
            // if acquired, immediately enter try finally (release)
            try {
                log.debug("acquired [concurrent] permit");
                if (!acquireActiveAndRatePermit(taskAfterAcquire)) {
                    throw new RuntimeException(
                            "Couldn't get an [active] permit before timeout");
                }
            } finally {
                concSem.release();
                log.debug("released [concurrent] semaphore");
            }
        } else {
            log.debug("failed to acquire [concurrent] permit");
        }
        return gotConcurrentPermit;
    }

    private boolean acquireActiveAndRatePermit(Runnable taskAfterAcquire) {
        log.debug("before acquire [active] semaphore:{}", maxActiveSemaphore);
        try {
            // hang on to the semaphore, so that we can be certain of
            // releasing the same one we acquired
            final Semaphore activeSem = maxActiveSemaphore;
            boolean gotActivePermit =
                    activeSem.tryAcquire(5, TimeUnit.MINUTES);
            if (gotActivePermit) {
                // if acquired, immediately enter try finally (release)
                try {
                    log.debug("got [active] semaphore");
                    taskAfterAcquire.run();
                } finally {
                    activeSem.release();
                    log.debug("released [active] semaphore");
                }
            }
            return gotActivePermit;
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    public synchronized void setMaxConcurrent(int maxConcurrent) {
        if (maxConcurrent != this.maxConcurrent) {
            log.debug(
                "change max [concurrent] semaphore with new permit {}",
                maxConcurrent);
            maxConcurrentSemaphore =
                makeSemaphore(maxConcurrent);
            this.maxConcurrent = maxConcurrent;
        }
    }

    public synchronized void setMaxActive(int maxActive) {
        if (maxActive != this.maxActive) {
            log.debug(
                "change max [active] semaphore with new permit {}",
                maxActive);
            maxActiveSemaphore = makeSemaphore(maxActive);
            this.maxActive = maxActive;
        }
    }

    public synchronized void changeConfig(int maxConcurrent, int maxActive) {
        setMaxConcurrent(maxConcurrent);
        setMaxActive(maxActive);
    }

    public int availableConcurrentPermit() {
        return maxConcurrentSemaphore.availablePermits();
    }

    public int availableActivePermit() {
        return maxActiveSemaphore.availablePermits();
    }

    private static Semaphore makeSemaphore(int permit) {
        if (permit == 0) {
            return NoLimitSemaphore.INSTANCE;
        } else {
            return new Semaphore(permit, true);
        }
    }

    @Override
    public String toString() {
        return Objects
                .toStringHelper(this)
                .add("id", super.toString())
                .add("maxConcurrent(available)",
                        maxConcurrentSemaphore.availablePermits())
                .add("maxActive(available)",
                        maxActiveSemaphore.availablePermits())
                .add("maxActive(queue)", maxActiveSemaphore.getQueueLength())
                .toString();
    }

    /**
     * Overrides tryAcquire method to return true all the time.
     */
    private static class NoLimitSemaphore extends Semaphore {
        private static final long serialVersionUID = 1L;
        private static final NoLimitSemaphore INSTANCE =
                new NoLimitSemaphore();

        private NoLimitSemaphore() {
            super(1);
        }

        @Override
        public void release() {
            // do nothing
        }

        @Override
        public boolean tryAcquire() {
            return true;
        }

        @Override
        public boolean tryAcquire(long timeout, TimeUnit unit)
                throws InterruptedException {
            return true;
        }
    }
}
