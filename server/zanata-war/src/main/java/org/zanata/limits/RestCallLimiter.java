package org.zanata.limits;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import org.zanata.util.RunnableEx;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class RestCallLimiter {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RestCallLimiter.class);
    private volatile Semaphore maxConcurrentSemaphore;
    private volatile Semaphore maxActiveSemaphore;
    private int maxConcurrent;
    private int maxActive;

    /**
     * Creates a limiter which limits maximum concurrent requests and maximum
     * active requests.
     *
     * i.e. when setting maxConcurrent to 4 and maxActive to 2, only 4
     * concurrent requests is allowed to be served, only 2 of them are actively
     * running, the other 2 will block until the first 2 finishes.
     *
     * @param maxConcurrent
     *            maximum allowed requests/threads for a single user.
     * @param maxActive
     *            maximum allowed active requests/threads for a single user.
     */
    RestCallLimiter(int maxConcurrent, int maxActive) {
        this.maxConcurrent = maxConcurrent;
        this.maxActive = maxActive;
        this.maxConcurrentSemaphore = makeSemaphore(maxConcurrent);
        this.maxActiveSemaphore = makeSemaphore(maxActive);
    }

    @VisibleForTesting
    protected RestCallLimiter changeActiveSemaphore(Semaphore activeSemaphore) {
        this.maxActiveSemaphore = activeSemaphore;
        return this;
    }

    /**
     * This method is potentially blocking on available active permits. It may
     * throw an exception if it takes too long to obtain one of the semaphores.
     * It may immediately return false if there is not enough concurrent
     * permits.
     *
     * @param taskAfterAcquire
     *            task to perform after acquire
     */
    public boolean tryAcquireAndRun(RunnableEx taskAfterAcquire)
            throws Exception {
        // hang on to the semaphore, so that we can be certain of
        // releasing the same one we acquired
        final Semaphore concSem = maxConcurrentSemaphore;
        boolean gotConcurrentPermit = concSem.tryAcquire();
        if (gotConcurrentPermit) {
            // if acquired, immediately enter try finally (release)
            try {
                log.debug("acquired [concurrent] permit");
                if (!acquireActivePermit(taskAfterAcquire)) {
                    throw new RuntimeException(
                            "Couldn\'t get an [active] permit before timeout");
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

    private boolean acquireActivePermit(RunnableEx taskAfterAcquire)
            throws Exception {
        log.debug("before acquire [active] semaphore:{}", maxActiveSemaphore);
        try {
            // hang on to the semaphore, so that we can be certain of
            // releasing the same one we acquired
            final Semaphore activeSem = maxActiveSemaphore;
            boolean gotActivePermit = activeSem.tryAcquire(5, TimeUnit.MINUTES);
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

    /**
     * Due to the fact that semaphore won't allow live update its allowed
     * permits. For efficiency reason, when changing permits limit, any previous
     * requests are accounted separately from future requests, and may in fact
     * lead to oldMaxConcurrent + newMaxConcurrent requests being allowed
     * temporarily.
     *
     * @param maxConcurrent
     *            new maximum allowed concurrent permits
     */
    public synchronized void setMaxConcurrent(int maxConcurrent) {
        if (maxConcurrent != this.maxConcurrent) {
            log.debug("change max [concurrent] semaphore with new permit {}",
                    maxConcurrent);
            maxConcurrentSemaphore = makeSemaphore(maxConcurrent);
            this.maxConcurrent = maxConcurrent;
        }
    }

    /**
     * Due to the fact that semaphore won't allow live update its allowed
     * permits. For efficiency reason, when changing permits limit, any previous
     * requests are accounted separately from future requests, and may in fact
     * lead to oldMaxConcurrent + newMaxConcurrent requests being allowed
     * temporarily.
     *
     * @param maxActive
     *            new maximum allowed active permits
     */
    public synchronized void setMaxActive(int maxActive) {
        if (maxActive != this.maxActive) {
            log.debug("change max [active] semaphore with new permit {}",
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
        return MoreObjects.toStringHelper(this).add("id", super.toString())
                .add("maxConcurrent(available)",
                        maxConcurrentSemaphore.availablePermits())
                .add("maxActive(available)",
                        maxActiveSemaphore.availablePermits())
                .add("maxActive(queue)", maxActiveSemaphore.getQueueLength())
                .toString();
    }

    public int getMaxConcurrentPermits() {
        return maxConcurrent;
    }

    /**
     * Overrides tryAcquire method to return true all the time.
     */
    private static class NoLimitSemaphore extends Semaphore {
        private static final long serialVersionUID = 1L;
        private static final NoLimitSemaphore INSTANCE = new NoLimitSemaphore();

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
