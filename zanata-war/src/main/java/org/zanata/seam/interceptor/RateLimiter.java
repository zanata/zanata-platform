package org.zanata.seam.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.jboss.seam.intercept.InvocationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class RateLimiter {

    private final long capacityTokens;
    private final String apiKey;

    public RateLimiter(long capacityTokens, String apiKey) {
        this.capacityTokens = capacityTokens;
        this.apiKey = apiKey;
    }

    public Object consume(final InvocationContextMeasurer measurer) throws Exception {

        TokenBucket tokenBucket = TokenBucketsHolder.HOLDER
                .get(apiKey, new Callable<TokenBucket>() {
                    @Override
                    public TokenBucket call() throws Exception {
                        return TokenBucket.newFixedIntervalRefill(
                                        capacityTokens, 1, 1, TimeUnit.MILLISECONDS);
                    }
                });
        consumeFromTokenBucket(measurer, tokenBucket);
        return measurer.proceedAndMeasure(tokenBucket);
    }

    /**
     * This method is extracted so that tests can override and making behavior
     * testable.
     *
     * @param measurer
     *            invocation context
     * @param tokenBucket
     */
    protected void consumeFromTokenBucket(InvocationContextMeasurer measurer,
            TokenBucket tokenBucket) {
        if (log.isDebugEnabled()) {
            InvocationContext ic = measurer.getInvocationContext();
//            Class<?> target = ic.getTarget().getClass();
//            Method method = ic.getMethod();

            log.debug(
                    "accessing {} may be rate limited. Current bucket: {}",
                    ic, tokenBucket);
        }
        tokenBucket.consume();
    }

}
