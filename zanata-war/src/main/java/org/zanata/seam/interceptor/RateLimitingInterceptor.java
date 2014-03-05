package org.zanata.seam.interceptor;

import java.lang.reflect.Method;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.intercept.JavaBeanInterceptor;
import org.jboss.seam.intercept.OptimizedInterceptor;
import org.zanata.ApplicationConfiguration;
import org.zanata.annotation.RateLimiting;
import org.zanata.security.ZanataIdentity;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Interceptor(stateless = true, around = JavaBeanInterceptor.class)
@Slf4j
public class RateLimitingInterceptor implements OptimizedInterceptor {
    private static long capacityTokens;

    @AroundInvoke
    @Override
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        Method method = ic.getMethod();
        if (method.getAnnotation(RateLimiting.class) == null) {
            return ic.proceed();
        }

        String apiKey = ZanataIdentity.instance().getApiKey();
        if (Strings.isNullOrEmpty(apiKey)) {
            return ic.proceed();
        }

        long oldRateLimit = capacityTokens;
        capacityTokens = readRateLimitConfig();

        cleanUpBucketIfRateChanged(oldRateLimit, capacityTokens);

        if (capacityTokens == 0) {
            return ic.proceed();
        }

        RateLimiter rateLimiter = new RateLimiter(capacityTokens, apiKey);
        InvocationContextMeasurer measurer = InvocationContextMeasurer.of(ic);
        return rateLimiter.consume(measurer);
    }

    @Override
    public boolean isInterceptorEnabled() {
        return true;
    }

    /**
     * Read rate limit from application configuration. Test can override.
     *
     * @return rate limit
     */
    protected int readRateLimitConfig() {
        ApplicationConfiguration appConfig =
                (ApplicationConfiguration) Component
                        .getInstance("applicationConfiguration");
        return appConfig.getRateLimitPerSecond();
    }

    private static void cleanUpBucketIfRateChanged(long oldRate, long newRate) {
        if (oldRate != newRate) {
            log.info("rate limit has changed from {} to {}", oldRate, newRate);
            TokenBucketsHolder.HOLDER.invalidateAll();
        }
    }
}
