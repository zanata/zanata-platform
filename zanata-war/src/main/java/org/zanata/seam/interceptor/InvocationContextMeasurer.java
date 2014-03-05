package org.zanata.seam.interceptor;

import org.jboss.seam.intercept.InvocationContext;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
* @author Patrick Huang
*         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
@RequiredArgsConstructor(staticName = "of")
@Getter
@Slf4j
class InvocationContextMeasurer {
    private final InvocationContext invocationContext;

    public Object proceedAndMeasure(TokenBucket tokenBucket) throws Exception {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        Object result = invocationContext.proceed();
        stopwatch.stop();
        log.debug("bucket before rebate: {}", tokenBucket);
        log.debug("invocation time used in millis: {}", stopwatch.elapsedMillis());
        long rebateTokens = tokensBasedOnTimeUsed(
                tokenBucket.getPerRequestConsumption(),
                stopwatch.elapsedMillis());
        log.debug("rebate tokens: {}", rebateTokens);
        tokenBucket.refill(rebateTokens);
        log.debug("bucket after rebate: {}", tokenBucket);

        return result;
    }

    private long tokensBasedOnTimeUsed(long perRequestConsumption,
            long timeUsedInMillis) {
        return perRequestConsumption - timeUsedInMillis;
    }
}
