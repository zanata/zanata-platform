package org.zanata.seam.interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.intercept.JavaBeanInterceptor;
import org.jboss.seam.intercept.OptimizedInterceptor;
import org.zanata.ApplicationConfiguration;
import org.zanata.annotation.RateLimiting;
import org.zanata.security.ZanataIdentity;
import org.zanata.util.Introspectable;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Interceptor(stateless = true, around = JavaBeanInterceptor.class)
@Slf4j
public class RateLimitingInterceptor implements OptimizedInterceptor,
        Introspectable {
    private static final Cache<String, TokenBucket> ACTIVE_CALLERS =
            CacheBuilder.newBuilder().maximumSize(100).build();
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
                        return TokenBuckets.newFixedIntervalRefill(rateLimit,
                                rateLimit, PERIOD, TimeUnit.SECONDS);
                    }
                });
        if (log.isDebugEnabled()) {
            Class<?> target = ic.getTarget().getClass();
            // current bucket size is 0 doesn't mean request can't be served.
            // org.isomorphism.util.TokenBucket.tryConsume()() will always try refill before checking.
            // @see org.isomorphism.util.TokenBucket
            log.debug(
                    "accessing {}#{} will be rate limited to {} per second. Current size {}",
                    target, method.getName(), rateLimit, getSize(tokenBucket));
        }
        if (!tokenBucket.tryConsume()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity("requests for this API key has exceeded allowed rate limit per second:"
                                    + rateLimit).build());
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

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public Collection<String> getFieldNames() {

        return Lists.newArrayList(IntrospectableFields.Buckets.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public String get(String fieldName) {
        IntrospectableFields field = IntrospectableFields.valueOf(fieldName);
        switch (field) {
        case Buckets:
            return Iterables.toString(peekCurrentBuckets());
        default:
            throw new IllegalArgumentException("unknown field:" + fieldName);
        }
    }

    private static Iterable<String> peekCurrentBuckets() {
        ConcurrentMap<String, TokenBucket> map = ACTIVE_CALLERS.asMap();
        return Iterables.transform(map.entrySet(),
                new Function<Map.Entry<String, TokenBucket>, String>() {

                    @Override
                    public String apply(Map.Entry<String, TokenBucket> input) {

                        TokenBucket bucket = input.getValue();
                        try {
                            Object size = getSize(bucket);
                            return input.getKey() + ":" + size;
                        } catch (Exception e) {
                            String msg = "can not peek bucket size";
                            log.warn(msg);
                            return msg;
                        }
                    }
                });
    }

    private static Object getSize(TokenBucket bucket) {
        try {
            Field field = TokenBucket.class.getDeclaredField("size");
            field.setAccessible(true);
            return field.get(bucket);
        }
        catch (NoSuchFieldException e) {
            throw Throwables.propagate(e);
        }
        catch (IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    private enum IntrospectableFields {
        Buckets
    }
}
