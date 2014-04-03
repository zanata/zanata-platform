package org.zanata.limits;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class RestCallLimiter {
    private volatile Semaphore maxConcurrentSemaphore;
    private volatile Semaphore maxActiveSemaphore;
    private RateLimitConfig limitConfig;
    private volatile LimitChange activeChange;
    private volatile LimitChange concurrentChange;

    RestCallLimiter(RateLimitConfig limitConfig) {
        this.limitConfig = limitConfig;
        this.maxConcurrentSemaphore = makeSemaphore(limitConfig.maxConcurrent);
        this.maxActiveSemaphore = makeSemaphore(limitConfig.maxActive);
    }

    /**
     * May throw an exception if it takes too long to obtain one of the
     * semaphores
     *
     * @param taskAfterAcquire
     *            task to perform after acquire
     */
    public boolean tryAcquireAndRun(Runnable taskAfterAcquire) {
        applyConcurrentPermitChangeIfApplicable();
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
        applyActivePermitChangeIfApplicable();
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

    private void applyConcurrentPermitChangeIfApplicable() {
        if (concurrentChange != null) {
            synchronized (this) {
                if (concurrentChange != null) {
                    log.debug(
                            "change max [concurrent] semaphore with new permit ",
                            concurrentChange.newLimit);
                    maxConcurrentSemaphore =
                            makeSemaphore(concurrentChange.newLimit);
                    concurrentChange = null;
                }
            }
        }
    }

    private void applyActivePermitChangeIfApplicable() {
        if (activeChange != null) {
            synchronized (this) {
                if (activeChange != null) {
                    // since this block is synchronized, there won't be new
                    // permit acquired from maxActiveSemaphore other than this
                    // thread. It ought to be the last and only one entering in
                    // this block. It will replace semaphore and old blocked
                    // threads will release on old semaphore
                    log.debug(
                            "change max [active] semaphore with new permit {}",
                            activeChange.newLimit);
                    maxActiveSemaphore = makeSemaphore(activeChange.newLimit);
                    activeChange = null;
                }
            }
        }
    }

    public void changeConfig(RateLimitConfig newLimitConfig) {
        if (newLimitConfig.maxConcurrent != limitConfig.maxConcurrent) {
            changeConcurrentLimit(limitConfig.maxConcurrent,
                    newLimitConfig.maxConcurrent);
        }
        if (newLimitConfig.maxActive != limitConfig.maxActive) {
            changeActiveLimit(limitConfig.maxActive, newLimitConfig.maxActive);
        }
        limitConfig = newLimitConfig;
    }

    protected synchronized void
            changeConcurrentLimit(int oldLimit, int newLimit) {
        this.concurrentChange = new LimitChange(oldLimit, newLimit);
    }

    protected synchronized void changeActiveLimit(int oldLimit, int newLimit) {
        this.activeChange = new LimitChange(oldLimit, newLimit);
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

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class RateLimitConfig {
        private final int maxConcurrent;
        private final int maxActive;
    }

    @RequiredArgsConstructor
    @ToString
    private static class LimitChange {
        private final int oldLimit;
        private final int newLimit;
    }

    /**
     * Overrides tryAcquire method to return true all the time.
     */
    private static class NoLimitSemaphore extends Semaphore {
        private static final long serialVersionUID = 1L;
        private static final NoLimitSemaphore INSTANCE =
                new NoLimitSemaphore(0);

        private NoLimitSemaphore(int permits) {
            super(1);
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
