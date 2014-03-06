package org.zanata.rest;

import java.util.concurrent.Semaphore;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.RateLimiter;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class RestRateLimiter {
    private Semaphore maxConcurrentSemaphore;
    private Semaphore maxActiveSemaphore;
    private RateLimiter rateLimiter;
    private RateLimitConfig limitConfig;

    public RestRateLimiter(RateLimitConfig limitConfig) {
        this.limitConfig = limitConfig;
        this.maxConcurrentSemaphore = new Semaphore(limitConfig.maxConcurrent, true);
        this.maxActiveSemaphore = new Semaphore(limitConfig.maxActive, true);
        rateLimiter = RateLimiter.create(limitConfig.rateLimitPerSecond);
    }

    public boolean tryAcquireConcurrentPermit() {
        return maxConcurrentSemaphore.tryAcquire();
    }

    public void acquire() {
        maxActiveSemaphore.acquireUninterruptibly();
        rateLimiter.acquire();
    }

    public void release() {
        maxActiveSemaphore.release();
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("maxConcurrent", maxConcurrentSemaphore.availablePermits())
                .add("maxConcurrent queue", maxConcurrentSemaphore.getQueueLength())
                .add("maxActive", maxActiveSemaphore.availablePermits())
                .add("maxActive queue", maxActiveSemaphore.getQueueLength())
                .add("rateLimiter", rateLimiter)
                .toString();
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class RateLimitConfig {
        private final int maxConcurrent;
        private final int maxActive;
        private final double rateLimitPerSecond;
    }
}
