package org.zanata.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.web.AbstractFilter;
import org.zanata.ApplicationConfiguration;
import org.zanata.rest.HeaderHelper;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

@Name("rateLimitingReleaseFilter")
@Filter(within = "org.jboss.seam.web.contextFilter")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Install
@Slf4j
public class RestRateLimitingFilter extends AbstractFilter {

    @Override
    public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request =
                HttpServletRequest.class.cast(servletRequest);
        final HttpServletResponse response =
                HttpServletResponse.class.cast(servletResponse);

        // we only target REST requests
        if (!request.getRequestURI().contains("/rest/")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final String apiKey =
                Strings.nullToEmpty(request
                        .getHeader(HeaderHelper.X_AUTH_TOKEN_HEADER));

        RateLimitingRequest rateLimitingRequest =
                createRateLimitingRequest(request, apiKey);
        try {
            rateLimitingRequest.run();
            if (!rateLimitingRequest.isTooManyConcurrentRequest()) {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } finally {
            if (rateLimitingRequest.isTooManyConcurrentRequest()) {
                log.warn(
                        "{} has too many concurrent requests. Returning status 429",
                        apiKey);
                response.setStatus(429);
                PrintWriter writer = response.getWriter();
                writer.append("Too many concurrent request");
                writer.close();
            } else if (rateLimitingRequest.isRateLimited()) {
                log.debug("releasing semaphore for {}", apiKey);
                rateLimitingRequest.getRateLimiter().release();
            }
        }

    }

    /**
     * Test override-able.
     */
    protected RateLimitingRequest createRateLimitingRequest(
            HttpServletRequest request, String apiKey) {
        return new RateLimitingRequest(apiKey, request);
    }

    protected static class RateLimitingRequest extends
            ContextualHttpServletRequest {

        private final String apiKey;
        @Getter
        private boolean tooManyConcurrentRequest = false;
        @Getter
        private RestRateLimiter rateLimiter;
        @Getter
        private boolean rateLimited = false;

        protected RateLimitingRequest(String apiKey, HttpServletRequest request) {
            super(request);
            this.apiKey = apiKey;
        }

        @Override
        public void process() throws Exception {
            ApplicationConfiguration appConfig =
                    (ApplicationConfiguration) Component
                            .getInstance("applicationConfiguration");

            if (!appConfig.getRateLimitSwitch()) {
                return;
            }

            RateLimiterHolder rateLimiterHolder =
                    RateLimiterHolder.getInstance();
            final RestRateLimiter.RateLimitConfig limitConfig =
                    rateLimiterHolder.getLimitConfig();

            try {
                rateLimiter =
                        rateLimiterHolder.get(apiKey,
                                new Callable<RestRateLimiter>() {
                                    @Override
                                    public RestRateLimiter call()
                                            throws Exception {
                                        log.info("creating rate limiter for api key: {}",
                                                apiKey);
                                        return new RestRateLimiter(limitConfig);
                                    }
                                });
            } catch (ExecutionException e) {
                throw new WebApplicationException(e,
                        Response.Status.INTERNAL_SERVER_ERROR);
            }

            rateLimited = true;
            log.debug("check semaphore for {}", this);
            if (!rateLimiter.tryAcquireConcurrentPermit()) {
                tooManyConcurrentRequest = true;
                return;
            }

            rateLimiter.acquire();
            log.debug("finish blocking for {}", this);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("id", super.toString())
                    .add("apiKey", apiKey)
                    .add("rateLimited", rateLimited)
                    .add("rateLimiter", rateLimiter)
                    .add("tooManyConcurrentRequest", tooManyConcurrentRequest)
                    .toString();
        }
    }
}
