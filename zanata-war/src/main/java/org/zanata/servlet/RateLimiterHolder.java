package org.zanata.servlet;

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
public class RateLimiterHolder implements Introspectable {

    @Delegate
    private final Cache<String, RestRateLimiter> activeCallers = CacheBuilder
            .newBuilder().maximumSize(100).build();

    @Getter
    private RestRateLimiter.RateLimitConfig limitConfig;

    public static RateLimiterHolder getInstance() {
        return (RateLimiterHolder) Component
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
                new RestRateLimiter.RateLimitConfig(maxConcurrent, maxActive,
                        rateLimitPerSecond);
    }

    @Observer({ ApplicationConfiguration.EVENT_CONFIGURATION_CHANGED })
    public void configurationChanged() {
        RestRateLimiter.RateLimitConfig old = limitConfig;
        readRateLimitState();
        if (!Objects.equal(old, limitConfig)) {
            RateLimiterHolder.log.info("application configuration changed. Old: {}, New: {}",
                    old, limitConfig);
            for (RestRateLimiter restRateLimiter : activeCallers.asMap().values()) {
                restRateLimiter.changeConfig(limitConfig);
            }
        }
    }

    // below are all monitoring stuff
    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public Collection<String> getFieldNames() {
        return Lists.newArrayList(IntrospectableFields.RateLimiters.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public String get(String fieldName) {
        IntrospectableFields field = IntrospectableFields.valueOf(fieldName);
        switch (field) {
        case RateLimiters:
            return Iterables.toString(peekCurrentBuckets());
        default:
            throw new IllegalArgumentException("unknown field:" + fieldName);
        }
    }

    private Iterable<String> peekCurrentBuckets() {
        ConcurrentMap<String, RestRateLimiter> map = activeCallers.asMap();
        return Iterables.transform(map.entrySet(),
                new Function<Map.Entry<String, RestRateLimiter>, String>() {

                    @Override
                    public String
                            apply(Map.Entry<String, RestRateLimiter> input) {

                        RestRateLimiter rateLimiter = input.getValue();
                        // TODO pahuang may want to show more things
                        return input.getKey() + ":" + rateLimiter;
                    }
                });
    }

    private enum IntrospectableFields {
        RateLimiters
    }
}
