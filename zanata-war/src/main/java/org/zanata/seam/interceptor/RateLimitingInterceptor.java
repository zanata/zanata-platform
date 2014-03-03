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
    private static int rateLimit;

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

        int oldRateLimit = rateLimit;
        rateLimit = readRateLimitConfig();

        cleanUpBucketIfRateChanged(oldRateLimit, rateLimit);

        if (rateLimit == 0) {
            return ic.proceed();
        }

        RateLimiter rateLimiter = new RateLimiter();
        InvocationContextMeasurer measurer =
                InvocationContextMeasurer.of(ic, apiKey, rateLimit);
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

    private static void cleanUpBucketIfRateChanged(int oldRate, int newRate) {
        if (oldRate != newRate) {
            log.info("rate limit has changed from {} to {}", oldRate, newRate);
            TokenBucketsHolder.HOLDER.invalidateAll();
        }
    }
}
