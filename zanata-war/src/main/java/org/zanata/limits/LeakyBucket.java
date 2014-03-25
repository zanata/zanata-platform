package org.zanata.limits;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.base.Ticker;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class LeakyBucket {
    private final long refillPeriod;
    private final long capacity;
    private final Ticker ticker;
    private volatile long permit;
    private volatile long lastRead;

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
        this.capacity = capacity;
        permit = capacity;
        ticker = Ticker.systemTicker();
        refillPeriod =
                TimeUnit.NANOSECONDS.convert(refillDuration, refillTimeUnit);
    }

    public synchronized boolean tryAcquire() {
        onDemandRefill();
        if (permit > 0) {
            log.debug("deduct 1 permit and try acquire return true");
            lastRead = ticker.read();
            permit--;
            return true;
        } else {
            return false;
        }
    }

    private synchronized void onDemandRefill() {
        if (permit == capacity) {
            return;
        }
        long timePassed = ticker.read() - lastRead;
        log.debug("time passed: {}", timePassed);
        long permitsShouldAdd = timePassed / refillPeriod;
        log.debug("permits should add: {}", permitsShouldAdd);
        if (timePassed >= refillPeriod) {
            permit = Math.min(capacity, permit + permitsShouldAdd);
            log.debug("refilled and now with {} permits", permit);
        }
    }

    @Override
    public String toString() {
        // @formatter:off
        return Objects.toStringHelper(this)
                .add("refillPeriod", refillPeriod)
                .add("capacity", capacity)
                .add("permit", permit)
                .add("lastRead", lastRead)
                .toString();
        // @formatter:on
    }
}
