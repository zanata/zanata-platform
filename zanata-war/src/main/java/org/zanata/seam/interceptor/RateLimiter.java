package org.zanata.seam.interceptor;

import java.lang.reflect.Method;

import org.isomorphism.util.TokenBucket;
import org.jboss.seam.intercept.InvocationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class RateLimiter {

    public Object consume(InvocationContextMeasurer measurer) throws Exception {

        TokenBucket tokenBucket = TokenBucketsHolder.HOLDER
                .getIfPresent(measurer.getApiKey());
        if (tokenBucket != null) {
            consumeFromTokenBucket(measurer, tokenBucket);
        }
        return measurer.proceedAndMeasure();
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
            Class<?> target = ic.getTarget().getClass();
            Method method = ic.getMethod();

            log.debug(
                    "accessing {}#{} will be rate limited to {} per second. Current size {}",
                    target, method.getName(), measurer.getRateLimit(),
                    TokenBucketsHolder.getSize(tokenBucket));
        }
        // current bucket size is 0 doesn't mean request can't be served.
        // org.isomorphism.util.TokenBucket.tryConsume()() will always try
        // refill before checking.
        // @see org.isomorphism.util.TokenBucket
        tokenBucket.consume();
    }

}
