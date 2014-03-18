package org.zanata.limits;

import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.seam.Component;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.zanata.ApplicationConfiguration;
import com.google.common.base.Objects;
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
public class RateLimitingProcessor extends ContextualHttpServletRequest {

    // http://tools.ietf.org/html/rfc6585
    public static final int TOO_MANY_REQUEST = 429;
    private final String apiKey;
    private final FilterChain filterChain;
    private final ServletRequest servletRequest;
    private final ServletResponse servletResponse;
    private final HttpServletResponse httpResponse;

    public RateLimitingProcessor(String apiKey, ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain) {
        super(HttpServletRequest.class.cast(servletRequest));
        this.apiKey = apiKey;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.filterChain = filterChain;
        httpResponse = HttpServletResponse.class.cast(this.servletResponse);
    }

    @Override
    public void process() throws Exception {
        ApplicationConfiguration appConfig = getApplicationConfiguration();

        if (!appConfig.getRateLimitSwitch()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        RateLimitManager rateLimitManager = getRateLimiterHolder();
        final RestCallLimiter.RateLimitConfig limitConfig =
                rateLimitManager.getLimitConfig();

        RestCallLimiter rateLimiter;
        try {
            rateLimiter =
                    rateLimitManager.get(apiKey, new RestRateLimiterCallable(
                            limitConfig, apiKey));
        } catch (ExecutionException e) {
            throw new WebApplicationException(e,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        log.debug("check semaphore for {}", this);

        if (rateLimiter.tryAcquire()) {
            try {
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                log.debug("releasing semaphore for {}", apiKey);
                rateLimiter.release();
            }
        } else {
            // TODO pahuang rate limit the logging otherwise it may become excessive
            log.warn(
                    "{} has too many concurrent requests. Returning status 429",
                    apiKey);
            httpResponse.setStatus(TOO_MANY_REQUEST);
            PrintWriter writer = httpResponse.getWriter();
            writer.append(String.format(
                    "Too many concurrent request for this API key (maximum is %d)",
                    appConfig.getMaxConcurrentRequestsPerApiKey()));
            writer.close();
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

    private static class RestRateLimiterCallable implements
            Callable<RestCallLimiter> {
        private final RestCallLimiter.RateLimitConfig limitConfig;
        private final String apiKey;

        public RestRateLimiterCallable(
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
