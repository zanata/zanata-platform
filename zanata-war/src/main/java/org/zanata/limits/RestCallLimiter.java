package org.zanata.limits;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.Uninterruptibles;
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
    private RateLimiter rateLimiter;
    private RateLimitConfig limitConfig;
    private volatile LimitChange activeChange;
    private volatile LimitChange concurrentChange;

    RestCallLimiter(RateLimitConfig limitConfig) {
        this.limitConfig = limitConfig;
        this.maxConcurrentSemaphore = makeSemaphore(limitConfig.maxConcurrent);
        this.maxActiveSemaphore = makeSemaphore(limitConfig.maxActive);
        if (limitConfig.rateLimitPerSecond == 0) {
            rateLimiter = RateLimiter.create(Integer.MAX_VALUE);
        } else {
            rateLimiter = RateLimiter.create(limitConfig.rateLimitPerSecond);
        }
    }

    public boolean tryAcquireAndRun(Runnable taskAfterAcquire) {
        applyConcurrentPermitChangeIfApplicable();
        boolean gotConcurrentPermit = maxConcurrentSemaphore.tryAcquire();
        log.debug("try acquire [concurrent] permit:{}", gotConcurrentPermit);
        if (gotConcurrentPermit) {
            try {
                if (acquireActiveAndRatePermit()) {
                    try {
                        taskAfterAcquire.run();
                    } finally {
                        log.debug("releasing active concurrent semaphore");
                        maxActiveSemaphore.release();
                    }
                } else {
                    throw new RuntimeException(
                            "Couldn't get an [active] permit in time");
                }
            } finally {
                log.debug("releasing max [concurrent] semaphore");
                maxConcurrentSemaphore.release();
            }
        }
        return gotConcurrentPermit;
    }

    private boolean acquireActiveAndRatePermit() {
        applyActivePermitChangeIfApplicable();
        log.debug("before acquire [active] semaphore:{}", maxActiveSemaphore);
        try {
            boolean gotActivePermit =
                    maxActiveSemaphore.tryAcquire(5, TimeUnit.MINUTES);
            log.debug(
                    "got [active] semaphore [{}] and before acquire rate limit permit:{}",
                    gotActivePermit, rateLimiter);
            if (gotActivePermit) {
                rateLimiter.acquire();
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
                    // this block. It will have to wait for all other previous
                    // blocked threads to complete before changing the semaphore
                    log.debug(
                            "detects max [active] permit change [{}]. Will sleep until all blocking threads [#{}] released.",
                            activeChange, maxActiveSemaphore.getQueueLength());
                    while (maxActiveSemaphore.availablePermits() != activeChange.oldLimit) {
                        Uninterruptibles.sleepUninterruptibly(1,
                                TimeUnit.NANOSECONDS);
                    }
                    log.debug(
                            "change max [active] semaphore with new permit {}",
                            activeChange.newLimit);
                    maxActiveSemaphore = makeSemaphore(activeChange.newLimit);
                    activeChange = null;
                }
            }
        }
    }

    public void release() {
        log.debug("releasing active semaphore");
        maxActiveSemaphore.release();
        log.debug("releasing concurrent semaphore");
        maxConcurrentSemaphore.release();
    }

    public void changeConfig(RateLimitConfig newLimitConfig) {
        if (newLimitConfig.maxConcurrent != limitConfig.maxConcurrent) {
            changeConcurrentLimit(limitConfig.maxConcurrent,
                    newLimitConfig.maxConcurrent);
        }
        if (newLimitConfig.rateLimitPerSecond != limitConfig.rateLimitPerSecond) {
            changeRateLimitPermitsPerSecond(newLimitConfig.rateLimitPerSecond);
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

    protected synchronized void changeRateLimitPermitsPerSecond(double permits) {
        rateLimiter.setRate(permits);
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

    protected double rateLimitRate() {
        return rateLimiter.getRate();
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
                .add("rateLimiter", rateLimiter).toString();
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class RateLimitConfig {
        private final int maxConcurrent;
        private final int maxActive;
        private final double rateLimitPerSecond;
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
