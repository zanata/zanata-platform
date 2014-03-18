package org.zanata.limits;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
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
public class RestCallLimiter {
    private Semaphore maxConcurrentSemaphore;
    private Semaphore maxActiveSemaphore;
    private RateLimiter rateLimiter;
    private RateLimitConfig limitConfig;
    private volatile ActiveLimitChange change;

    public RestCallLimiter(RateLimitConfig limitConfig) {
        this.limitConfig = limitConfig;
        this.maxConcurrentSemaphore =
                new Semaphore(limitConfig.maxConcurrent, true);
        this.maxActiveSemaphore = new Semaphore(limitConfig.maxActive, true);
        rateLimiter = RateLimiter.create(limitConfig.rateLimitPerSecond);
    }

    public boolean tryAcquire() {
        log.debug("before try acquire concurrent semaphore:{}",
                maxConcurrentSemaphore);
        boolean got = maxConcurrentSemaphore.tryAcquire();
        log.debug("get permit:{}", got);
        if (got) {
            acquireActiveAndRatePermit();
            log.debug("got all permits and ready to go: {}", this);
        }
        return got;
    }

    private void acquireActiveAndRatePermit() {
        if (change != null) {
            synchronized (this) {
                if (change != null) {
                    // since this block is synchronized, there won't be new
                    // permit acquired from maxActiveSemaphore other than this
                    // thread. It ought to be the last and only one entering in
                    // this block. It will have to wait for all other previous
                    // blocked threads to complete before changing the semaphore
                    log.debug(
                            "detects max active permit change [{}]. Will sleep until all blocking threads [#{}] released.",
                            change, maxActiveSemaphore.getQueueLength());
                    while (maxActiveSemaphore.availablePermits() != change.oldLimit) {
                        Uninterruptibles.sleepUninterruptibly(1,
                                TimeUnit.NANOSECONDS);
                    }
                    log.debug("change max active semaphore with new permit");
                    maxActiveSemaphore = new Semaphore(change.newLimit, true);
                    change = null;
                }
            }
        }
        log.debug("before acquire active semaphore:{}", maxActiveSemaphore);
        maxActiveSemaphore.acquireUninterruptibly();
// if we want to enable timeout here,
// we must ensure release is not called when it timed out
//        try {
//            boolean gotIt = maxActiveSemaphore.tryAcquire(30, TimeUnit.SECONDS);
//            if (!gotIt) {
//                // timed out
//                throw new WebApplicationException(Response.status(
//                        Response.Status.SERVICE_UNAVAILABLE)
//                        .entity("System too busy").build());
//            }
//        }
//        catch (InterruptedException e) {
//            throw Throwables.propagate(e);
//        }
        log.debug(
                "got active semaphore and before acquire rate limit permit:{}",
                rateLimiter);
        rateLimiter.acquire();
    }

    public void release() {
        log.debug("releasing active semaphore");
        maxActiveSemaphore.release();
        log.debug("releasing concurrent semaphore");
        maxConcurrentSemaphore.release();
    }

    public void changeConfig(RateLimitConfig newLimitConfig) {
        if (newLimitConfig.maxConcurrent != limitConfig.maxConcurrent) {
            changeConcurrentLimit(newLimitConfig.maxConcurrent);
        }
        if (newLimitConfig.rateLimitPerSecond != limitConfig.rateLimitPerSecond) {
            changeRateLimitPermitsPerSecond(newLimitConfig.rateLimitPerSecond);
        }
        limitConfig = newLimitConfig;
    }

    protected synchronized void changeConcurrentLimit(int maxConcurrent) {
        log.info("max concurrent limit changed: {}", maxConcurrent);
        maxConcurrentSemaphore = new Semaphore(maxConcurrent);
    }

    protected synchronized void changeRateLimitPermitsPerSecond(double permits) {
        log.info("rate limit changed: {}", permits);
        rateLimiter.setRate(permits);
    }

    protected synchronized void changeActiveLimit(int oldLimit, int newLimit) {
        this.change = new ActiveLimitChange(oldLimit, newLimit);
        log.info("max active limit changed: {}", change);
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

    @Override
    public String toString() {
        return Objects
                .toStringHelper(this)
                .add("id", super.toString())
                .add("maxConcurrent(available)", maxConcurrentSemaphore.availablePermits())
                .add("maxActive(available)", maxActiveSemaphore.availablePermits())
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
    private static class ActiveLimitChange {
        private final int oldLimit;
        private final int newLimit;
    }
}
