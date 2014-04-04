package org.zanata.limits;

import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@ToString
public class LeakyBucket {
    private final long refillPeriod;
    private final long capacity;
    private final TimeTracker timeTracker;
    private volatile long permit;
    private final long waitSleepTime;

    /**
     * Simple form leaky bucket. Initialized with a capacity and full. Each
     * #tryAcquire() will deduct 1 permit. Permits is refilled on demand after
     * set time period.
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
        permit = capacity;
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
    public synchronized boolean tryAcquire(final long permit) {
        onDemandRefill();
        if (this.permit >= permit) {
            timeTracker.recordCurrentTime();
            this.permit -= permit;
            log.debug(
                    "deduct {} permit(s), current left permit {}, return true",
                    permit, this.permit);
            return true;
        } else {
            return false;
        }
    }

    private synchronized void onDemandRefill() {
        if (permit == capacity) {
            return;
        }
        long timePassed = timeTracker.timePassed();
        log.debug("time passed: {}", timePassed);
        long permitsShouldAdd = timePassed / refillPeriod;
        log.debug("permits should add: {}", permitsShouldAdd);
        if (timePassed >= refillPeriod) {
            permit = Math.min(capacity, permit + permitsShouldAdd);
            log.debug("refilled and now with {} permits", permit);
        }
    }

    @ToString
    static class TimeTracker {
        private final Ticker ticker = Ticker.systemTicker();
        private long lastRead;

        void recordCurrentTime() {
            lastRead = ticker.read();
        }

        long timePassed() {
            return ticker.read() - lastRead;
        }
    }
}
