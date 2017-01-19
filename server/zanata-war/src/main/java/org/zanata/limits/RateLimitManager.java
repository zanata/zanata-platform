package org.zanata.limits;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.ApplicationConfiguration;
import org.zanata.async.Async;
import org.zanata.events.ConfigurationChanged;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.util.Introspectable;
import org.zanata.util.ServiceLocator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("rateLimitManager")
@javax.enterprise.context.ApplicationScoped
public class RateLimitManager implements Introspectable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RateLimitManager.class);

    private final Cache<RateLimiterToken, RestCallLimiter> activeCallers =
            CacheBuilder.newBuilder().maximumSize(100).build();
    @VisibleForTesting
    private int maxConcurrent;
    @VisibleForTesting
    private int maxActive;
    @Inject
    private ApplicationConfiguration appConfig;

    public static RateLimitManager getInstance() {
        return ServiceLocator.instance().getInstance(RateLimitManager.class);
    }

    @PostConstruct
    public void loadConfig() {
        readRateLimitState();
    }

    private void readRateLimitState() {
        maxConcurrent = appConfig.getMaxConcurrentRequestsPerApiKey();
        maxActive = appConfig.getMaxActiveRequestsPerApiKey();
    }

    @Async
    @Transactional
    public void configurationChanged(@Observes(
            during = TransactionPhase.AFTER_SUCCESS) ConfigurationChanged payload) {
        int oldConcurrent = maxConcurrent;
        int oldActive = maxActive;
        boolean changed = false;
        readRateLimitState();
        if (oldConcurrent != maxConcurrent) {
            log.info(
                    "application configuration changed. Old concurrent: {}, New concurrent: {}",
                    oldConcurrent, maxConcurrent);
            changed = true;
        }
        if (oldActive != maxActive) {
            log.info(
                    "application configuration changed. Old active: {}, New active: {}",
                    oldActive, maxActive);
            changed = true;
        }
        if (changed) {
            for (RestCallLimiter restCallLimiter : activeCallers.asMap()
                    .values()) {
                restCallLimiter.changeConfig(maxConcurrent, maxActive);
            }
        }
    }
    // below are all monitoring stuff

    @Override
    public String getIntrospectableId() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getFieldValuesAsJSON() {
        return DTOUtil.toJSON(peekCurrentBuckets());
    }

    private Map<String, String> peekCurrentBuckets() {
        ConcurrentMap<RateLimiterToken, RestCallLimiter> map =
                activeCallers.asMap();
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        map.forEach((key, value) -> {
            builder.put(key.toString(), value.toString());
        });
        return builder.build();
    }

    /**
     * @param key
     *            - {@link RateLimiterToken.TYPE )
     */
    public RestCallLimiter getLimiter(final RateLimiterToken key) {
        if (getMaxConcurrent() == 0 && getMaxActive() == 0) {
            if (activeCallers.size() > 0) {
                activeCallers.invalidateAll();
            }
            // short circuit if we don't want limiting
            return NoLimitLimiter.INSTANCE;
        }
        try {
            return activeCallers.get(key, () -> {
                log.debug("creating rate limiter for key: {}", key);
                return new RestCallLimiter(getMaxConcurrent(), getMaxActive());
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class NoLimitLimiter extends RestCallLimiter {
        private static final NoLimitLimiter INSTANCE = new NoLimitLimiter();

        private NoLimitLimiter() {
            super(0, 0);
        }
    }

    protected int getMaxConcurrent() {
        return this.maxConcurrent;
    }

    protected int getMaxActive() {
        return this.maxActive;
    }
}
