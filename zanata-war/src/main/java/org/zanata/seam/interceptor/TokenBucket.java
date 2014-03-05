package org.zanata.seam.interceptor;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.base.Ticker;
import com.google.common.util.concurrent.Uninterruptibles;

import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Based on org.isomorphism.util.TokenBucket.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class TokenBucket {
    private final RefillStrategy refillStrategy;
    private final long capacity;
    private long size;
    private long perRequestConsumption;

    public TokenBucket(long capacity, RefillStrategy refillStrategy) {
        this.refillStrategy = refillStrategy;
        this.capacity = capacity;
        this.size = capacity;
        perRequestConsumption = capacity / 2; //TODO pahuang we currently support 2 concurrent requests
    }

    /**
     * Construct a token bucket that uses a fixed interval refill strategy.
     * Initially the bucket will start with {@code capacityTokens} tokens in it,
     * and every {@code period} time units {@code refillTokens} will be added to
     * it. The tokens are added all at one time on the interval boundaries. By
     * default the system clock is used for keeping time.
     */
    public static TokenBucket newFixedIntervalRefill(
            long capacityTokens, long refillTokens, long period, TimeUnit unit) {
        Ticker ticker = Ticker.systemTicker();
        RefillStrategy strategy =
                new FixedIntervalRefillStrategy(ticker, refillTokens, period,
                        unit);
        return new TokenBucket(capacityTokens, strategy);
    }

    /**
     * Attempt to consume a single token from the bucket. If it was consumed
     * then {@code true} is returned, otherwise {@code false} is returned.
     *
     * @return {@code true} if a token was consumed, {@code false} otherwise.
     */
    public boolean tryConsume() {
        return tryConsume(1);
    }

    /**
     * Attempt to consume a specified number of tokens from the bucket. If the
     * tokens were consumed then {@code true} is returned, otherwise
     * {@code false} is returned.
     *
     * @param numTokens
     *            The number of tokens to consume from the bucket, must be a
     *            positive number.
     * @return {@code true} if the tokens were consumed, {@code false}
     *         otherwise.
     */
    public synchronized boolean tryConsume(long numTokens) {
        checkArgument(numTokens > 0,
                "Number of tokens to consume must be positive");
        checkArgument(numTokens <= capacity,
                "Number of tokens to consume must be less than the capacity of the bucket.");

        // Give the refill strategy a chance to add tokens if it needs to
        long newTokens = Math.max(0, refillStrategy.refill());
        this.size = Math.max(0, Math.min(this.size + newTokens, capacity));

        // Now try to consume some tokens
        if (numTokens <= this.size) {
            this.size -= numTokens;
            return true;
        }

        return false;
    }

    /**
     * Consume a single token from the bucket. If no token is currently
     * available then this method will block until a token becomes available.
     */
    public void consume() {
        consume(perRequestConsumption);
    }

    /**
     * Consumes multiple tokens from the bucket. If enough tokens are not
     * currently available then this method will block until
     *
     * @param numTokens
     *            The number of tokens to consume from teh bucket, must be a
     *            positive number.
     */
    public void consume(long numTokens) {
        while (true) {
            if (tryConsume(numTokens)) {
                break;
            }

            // Sleep for the smallest unit of time possible just to relinquish
            // control
            // and to allow other threads to run.
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.NANOSECONDS);
        }
    }

    public synchronized void refill(long rebateTokens) {
        size = Math.min(size + rebateTokens, capacity);
        if (size < 0) {
            size = 0;
        }
    }

    public long currentSize() {
        return size;
    }

    public long getPerRequestConsumption() {
        return perRequestConsumption;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("capacity", capacity)
                .add("size", size)
                .add("perRequestConsumption", perRequestConsumption)
                .toString();
    }

    /** Encapsulation of a refilling strategy for a token bucket. */
    public static interface RefillStrategy {
        /**
         * Returns the number of tokens to add to the token bucket.
         *
         * @return The number of tokens to add to the token bucket.
         */
        long refill();
    }

    public static class FixedIntervalRefillStrategy implements RefillStrategy {
        private final Ticker ticker;
        private final long numTokens;
        private final long periodInNanos;
        private long nextRefillTime;

        /**
         * Create a FixedIntervalRefillStrategy.
         *
         * @param ticker
         *            A ticker to use to measure time.
         * @param numTokens
         *            The number of tokens to add to the bucket every interval.
         * @param period
         *            How often to refill the bucket.
         * @param unit
         *            Unit for period.
         */
        public FixedIntervalRefillStrategy(Ticker ticker, long numTokens,
                long period, TimeUnit unit) {
            this.ticker = ticker;
            this.numTokens = numTokens;
            this.periodInNanos = unit.toNanos(period);
            this.nextRefillTime = -1;
        }

        public synchronized long refill() {
            long now = ticker.read();
            if (now < nextRefillTime) {
                return 0;
            }
            nextRefillTime = now + periodInNanos;
            return numTokens;
        }
    }
}
