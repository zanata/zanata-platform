package org.zanata.limits;

import java.util.concurrent.TimeUnit;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LeakyBucket {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LeakyBucket.class);
    private final long refillPeriod;
    private final long capacity;
    private final TimeTracker timeTracker;
    private volatile long availablePermits;
    private final long waitSleepTime;

    /**
     * Simple form leaky bucket. Start off with full permits. Each #tryAcquire()
     * will deduct 1 permit. Permits is refilled on demand after set time
     * period.
     *
     * @param capacity
     *            capacity
     * @param refillDuration
     *            refill duration
     * @param refillTimeUnit
     *            refill time unit
     */
    public LeakyBucket(long capacity, int refillDuration,
            TimeUnit refillTimeUnit) {
        this(capacity, refillDuration, refillTimeUnit, new TimeTracker());
    }

    @VisibleForTesting
    protected LeakyBucket(long capacity, int refillDuration,
            TimeUnit refillTimeUnit, TimeTracker timeTracker) {
        this.capacity = capacity;
        availablePermits = capacity;
        this.timeTracker = timeTracker;
        refillPeriod =
                TimeUnit.NANOSECONDS.convert(refillDuration, refillTimeUnit);
        long refillInMillis =
                TimeUnit.MILLISECONDS.convert(refillDuration, refillTimeUnit);
        // when blocking, the time we should sleep (minimum 1 ms)
        waitSleepTime = Math.max(refillInMillis, 1);
    }

    /**
     * Try acquire 1 permit. Will not block.
     *
     * @return true if there is enough permit
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * Try acquire a number of permits. Will not block.
     *
     * @return true if there is enough permits
     */
    public synchronized boolean tryAcquire(final long requestPermits) {
        onDemandRefill();
        if (this.availablePermits >= requestPermits) {
            timeTracker.recordCurrentTime();
            this.availablePermits -= requestPermits;
            log.debug(
                    "deduct {} permits(s), current left permits {}, return true",
                    requestPermits, this.availablePermits);
            return true;
        } else {
            return false;
        }
    }

    private synchronized void onDemandRefill() {
        if (availablePermits == capacity) {
            return;
        }
        long timePassed = timeTracker.timePassed();
        log.debug("time passed: {}", timePassed);
        long permitsShouldAdd = timePassed / refillPeriod;
        log.debug("permits should add: {}", permitsShouldAdd);
        if (timePassed >= refillPeriod) {
            availablePermits =
                    Math.min(capacity, availablePermits + permitsShouldAdd);
            log.debug("refilled and now with {} permits", availablePermits);
        }
    }

    static class TimeTracker {
        private final Ticker ticker;
        private long lastRead;

        TimeTracker() {
            ticker = Ticker.systemTicker();
        }

        @VisibleForTesting
        TimeTracker(Ticker ticker) {
            this.ticker = ticker;
        }

        void recordCurrentTime() {
            lastRead = ticker.read();
        }

        long timePassed() {
            return ticker.read() - lastRead;
        }

        @Override
        public String toString() {
            return "LeakyBucket.TimeTracker(ticker=" + this.ticker
                    + ", lastRead=" + this.lastRead + ")";
        }
    }

    @Override
    public String toString() {
        return "LeakyBucket(refillPeriod=" + this.refillPeriod + ", capacity="
                + this.capacity + ", timeTracker=" + this.timeTracker
                + ", availablePermits=" + this.availablePermits
                + ", waitSleepTime=" + this.waitSleepTime + ")";
    }
}
