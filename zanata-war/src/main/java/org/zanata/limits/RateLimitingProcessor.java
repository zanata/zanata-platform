package org.zanata.limits;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.seam.Component;
import org.zanata.ApplicationConfiguration;
import com.google.common.base.Objects;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used by RateLimitingFilter and have access to seam environment.
 *
 * @see org.jboss.seam.servlet.ContextualHttpServletRequest
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class RateLimitingProcessor {
    // http://tools.ietf.org/html/rfc6585
    public static final int TOO_MANY_REQUEST = 429;

    private static final RateLimiter logLimiter = RateLimiter.create(1);
    private final String apiKey;
    private final HttpResponse response;
    private final Runnable taskToRun;

    public RateLimitingProcessor(String apiKey,
            HttpResponse response, Runnable taskToRun) {
        this.apiKey = apiKey;

        this.response = response;
        this.taskToRun = taskToRun;
    }

    public void process() throws Exception {
        ApplicationConfiguration appConfig = getApplicationConfiguration();

        if (appConfig.getMaxConcurrentRequestsPerApiKey() == 0
                && appConfig.getMaxActiveRequestsPerApiKey() == 0
                && appConfig.getRateLimitPerSecond() == 0) {
            // short circuit if we don't want limiting
            taskToRun.run();
            return;
        }

        RateLimitManager rateLimitManager = getRateLimiterHolder();
        final RestCallLimiter.RateLimitConfig limitConfig =
                rateLimitManager.getLimitConfig();

        RestCallLimiter rateLimiter;
        try {
            rateLimiter =
                    rateLimitManager.get(apiKey, new RestRateLimiterLoader(
                            limitConfig, apiKey));
        } catch (ExecutionException e) {
            throw new WebApplicationException(e,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        log.debug("check semaphore for {}", this);

        if (!rateLimiter.tryAcquireAndRun(taskToRun)) {
            if (logLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                log.warn(
                        "{} has too many concurrent requests. Returning status 429",
                        apiKey);
            }
            String errorMessage = String.format(
                    "Too many concurrent request for this API key (maximum is %d)",
                    appConfig.getMaxConcurrentRequestsPerApiKey());
            response.sendError(TOO_MANY_REQUEST, errorMessage);
        }
    }

    // test override-able
    protected RateLimitManager getRateLimiterHolder() {
        return RateLimitManager.getInstance();
    }

    // test override-able
    protected ApplicationConfiguration getApplicationConfiguration() {
        return (ApplicationConfiguration) Component
                .getInstance("applicationConfiguration");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", super.toString())
                .add("apiKey", apiKey).toString();
    }

    private static class RestRateLimiterLoader implements
            Callable<RestCallLimiter> {
        private final RestCallLimiter.RateLimitConfig limitConfig;
        private final String apiKey;

        public RestRateLimiterLoader(
                RestCallLimiter.RateLimitConfig limitConfig, String apiKey) {
            this.limitConfig = limitConfig;
            this.apiKey = apiKey;
        }

        @Override
        public RestCallLimiter call() throws Exception {
            log.debug("creating rate limiter for api key: {}", apiKey);
            return new RestCallLimiter(limitConfig);
        }
    }
}
