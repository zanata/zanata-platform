package org.zanata.servlet;

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
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class RateLimitingProcessor extends ContextualHttpServletRequest {

    private final String apiKey;
    private final FilterChain filterChain;
    private final ServletRequest servletRequest;
    private final ServletResponse servletResponse;
    private final HttpServletResponse httpResponse;

    RateLimitingProcessor(String apiKey, ServletRequest servletRequest,
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

        RateLimiterHolder rateLimiterHolder = getRateLimiterHolder();
        final RestRateLimiter.RateLimitConfig limitConfig =
                rateLimiterHolder.getLimitConfig();

        RestRateLimiter rateLimiter;
        try {
            rateLimiter =
                    rateLimiterHolder.get(apiKey, new RestRateLimiterCallable(
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
            log.warn(
                    "{} has too many concurrent requests. Returning status 429",
                    apiKey);
            httpResponse.setStatus(429);
            PrintWriter writer = httpResponse.getWriter();
            writer.append("Too many concurrent request");
            writer.close();
        }
    }

    // test override-able
    protected RateLimiterHolder getRateLimiterHolder() {
        return RateLimiterHolder.getInstance();
    }

    // test override-able
    protected ApplicationConfiguration getApplicationConfiguration() {
        return (ApplicationConfiguration) Component
                .getInstance("applicationConfiguration");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", super.toString())
                .add("apiKey", apiKey)
                .toString();
    }

    private static class RestRateLimiterCallable implements
            Callable<RestRateLimiter> {
        private final RestRateLimiter.RateLimitConfig limitConfig;
        private final String apiKey;

        public RestRateLimiterCallable(
                RestRateLimiter.RateLimitConfig limitConfig, String apiKey) {
            this.limitConfig = limitConfig;
            this.apiKey = apiKey;
        }

        @Override
        public RestRateLimiter call() throws Exception {
            log.info("creating rate limiter for api key: {}", apiKey);
            return new RestRateLimiter(limitConfig);
        }
    }
}
