package org.zanata.limits;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
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
        this.capacity = capacity;
        permit = capacity;
        ticker = Ticker.systemTicker();
        refillPeriod =
                TimeUnit.NANOSECONDS.convert(refillDuration, refillTimeUnit);
        long refillInMillis = TimeUnit.MILLISECONDS
                .convert(refillDuration, refillTimeUnit);
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
            lastRead = ticker.read();
            this.permit -= permit;
            log.debug(
                    "deduct {} permit(s), current left permit {}, return true",
                    permit, this.permit);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Acquire 1 permit. If there is not enough permit, will block the caller.
     */
    public void acquire() {
        acquire(1);
    }

    /**
     * Acquire asking permits. If there is not enough permits, will block the
     * caller.
     *
     * @param permits
     *            number of permits
     */
    public void acquire(long permits) {
        while (!tryAcquire(permits)) {
            try {
                Thread.sleep(waitSleepTime);
            }
            catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
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
