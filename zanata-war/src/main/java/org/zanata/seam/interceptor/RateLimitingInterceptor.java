package org.zanata.seam.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.intercept.JavaBeanInterceptor;
import org.jboss.seam.intercept.OptimizedInterceptor;
import org.zanata.ApplicationConfiguration;
import org.zanata.annotation.RateLimiting;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.client.Application;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Interceptor(stateless = true, around = JavaBeanInterceptor.class)
@Slf4j
public class RateLimitingInterceptor implements OptimizedInterceptor {
    private static final Cache<String, TokenBucket> ACTIVE_CALLERS = CacheBuilder
            .newBuilder().maximumSize(100).build();
    private static final int PERIOD = 1;
    private int rateLimit;

    public static int readRateLimitConfig() {
        ApplicationConfiguration appConfig =
                (ApplicationConfiguration) Component
                        .getInstance("applicationConfiguration");
        return appConfig.getRateLimitPerSecond();
    }

    @AroundInvoke
    @Override
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        Method method = ic.getMethod();
        if (method.getAnnotation(RateLimiting.class) == null) {
            return ic.proceed();
        }

        if (log.isDebugEnabled()) {
            Class<?> target = ic.getTarget().getClass();
            log.debug("rate limit potential target: {}#{}", target, method.getName());
        }

        int oldRateLimit = rateLimit;
        rateLimit = readRateLimitConfig();

        cleanUpBucketIfRateChanged(oldRateLimit, rateLimit);

        if (rateLimit == 0) {
            return ic.proceed();
        }
        String apiKey = ZanataIdentity.instance().getApiKey();
        if (Strings.isNullOrEmpty(apiKey)) {
            return ic.proceed();
        }
        TokenBucket tokenBucket =
                ACTIVE_CALLERS.get(apiKey, new Callable<TokenBucket>() {
                    @Override
                    public TokenBucket call() throws Exception {
                        return TokenBuckets.newFixedIntervalRefill(
                                rateLimit, rateLimit, PERIOD,
                                TimeUnit.SECONDS);
                    }
                });
        log.debug("accessing {} will be rate limited to {} per second", method.getName(),
                rateLimit);
        if (!tokenBucket.tryConsume()) {
            throw new WebApplicationException(Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("requests for this API key has exceeded allowed rate limit per second:" + rateLimit)
                    .build());
        }
        return ic.proceed();
    }

    private static void cleanUpBucketIfRateChanged(int oldRate, int newRate) {
        if (oldRate != newRate) {
            log.info("rate limit has changed from {} to {}", oldRate, newRate);
            ACTIVE_CALLERS.invalidateAll();
        }
    }

    @Override
    public boolean isInterceptorEnabled() {
        return true;
    }
}
