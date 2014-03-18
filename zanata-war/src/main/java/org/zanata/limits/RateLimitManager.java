package org.zanata.limits;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.zanata.ApplicationConfiguration;
import org.zanata.util.Introspectable;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Delegate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("rateLimiterHolder")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Slf4j
public class RateLimitManager implements Introspectable {

    public static final String INTROSPECTABLE_FIELD_RATE_LIMITERS =
            "RateLimiters";
    @Delegate
    private final Cache<String, RestCallLimiter> activeCallers = CacheBuilder
            .newBuilder().maximumSize(100).build();

    @Getter
    private RestCallLimiter.RateLimitConfig limitConfig;

    public static RateLimitManager getInstance() {
        return (RateLimitManager) Component
                .getInstance("rateLimiterHolder");
    }

    @Create
    public void loadConfig() {
        readRateLimitState();
    }

    private void readRateLimitState() {
        ApplicationConfiguration appConfig =
                (ApplicationConfiguration) Component
                        .getInstance("applicationConfiguration");
        int maxConcurrent = appConfig.getMaxConcurrentRequestsPerApiKey();
        int maxActive = appConfig.getMaxActiveRequestsPerApiKey();
        double rateLimitPerSecond = appConfig.getRateLimitPerSecond();
        limitConfig =
                new RestCallLimiter.RateLimitConfig(maxConcurrent, maxActive,
                        rateLimitPerSecond);
    }

    @Observer({ ApplicationConfiguration.EVENT_CONFIGURATION_CHANGED })
    public void configurationChanged() {
        RestCallLimiter.RateLimitConfig old = limitConfig;
        readRateLimitState();
        if (!Objects.equal(old, limitConfig)) {
            log.info("application configuration changed. Old: {}, New: {}",
                    old, limitConfig);
            for (RestCallLimiter restCallLimiter : activeCallers.asMap().values()) {
                restCallLimiter.changeConfig(limitConfig);
            }
        }
    }

    // below are all monitoring stuff
    @Override
    public String getIntrospectableId() {
        return getClass().getCanonicalName();
    }

    @Override
    public Collection<String> getIntrospectableFieldNames() {
        return Lists.newArrayList(INTROSPECTABLE_FIELD_RATE_LIMITERS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getFieldValueAsString(String fieldName) {
        if (INTROSPECTABLE_FIELD_RATE_LIMITERS.equals(fieldName)) {
            return Iterables.toString(peekCurrentBuckets());
        }
        throw new IllegalArgumentException("unknown field:" + fieldName);
    }

    private Iterable<String> peekCurrentBuckets() {
        ConcurrentMap<String, RestCallLimiter> map = activeCallers.asMap();
        return Iterables.transform(map.entrySet(),
                new Function<Map.Entry<String, RestCallLimiter>, String>() {

                    @Override
                    public String
                            apply(Map.Entry<String, RestCallLimiter> input) {

                        RestCallLimiter rateLimiter = input.getValue();
                        return input.getKey() + ":" + rateLimiter;
                    }
                });
    }

}
