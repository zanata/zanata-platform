package org.zanata.seam.interceptor;

import java.util.concurrent.TimeUnit;

import org.isomorphism.util.TokenBuckets;
import org.jboss.seam.intercept.InvocationContext;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
* @author Patrick Huang
*         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
@RequiredArgsConstructor(staticName = "of")
@Getter
class InvocationContextMeasurer {
    private final InvocationContext invocationContext;
    private final String apiKey;
    private final int rateLimit;

    public Object proceedAndMeasure() throws Exception {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        Object result = invocationContext.proceed();
        stopwatch.stop();
        measureTime(stopwatch.elapsedMillis());
        return result;
    }

    private void measureTime(long timeUsedInMillis) {
        if (isTakingTooLong(timeUsedInMillis)) {
            // if a method call is greater than 1 second we start to rate
            // limit it
            TokenBucketsHolder.HOLDER
                    .put(apiKey, TokenBuckets.newFixedIntervalRefill(
                            rateLimit, rateLimit, 1, TimeUnit.SECONDS));
        } else {
            // else we will cancel rate limiting
            TokenBucketsHolder.HOLDER.invalidate(apiKey);
        }
    }

    /**
     * Test can override.
     */
    // TODO pahuang this should be configurable
    protected boolean isTakingTooLong(long timeUsedInMillis) {
        return timeUsedInMillis > TimeUnit.SECONDS.toMillis(1);
    }
}
