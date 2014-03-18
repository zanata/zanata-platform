package org.zanata.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.web.AbstractFilter;
import org.zanata.rest.HeaderHelper;

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
                request.getHeader(HeaderHelper.X_AUTH_TOKEN_HEADER);

        // we are not validating api key but will rate limit any api key
        if (Strings.isNullOrEmpty(apiKey)) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            PrintWriter writer = response.getWriter();
            writer.append("You must have a valid API key. You can create one by logging in to Zanata and visiting the settings page.");
            writer.close();
            return;
        }

        RateLimitingProcessor processor =
                createRateLimitingRequest(apiKey, servletRequest,
                        servletResponse, filterChain);
        // RateLimitingProcessor extends ContextualHttpServletRequest. Calling
        // run() will set up all seam context environment
        processor.run();

    }

    /**
     * Test override-able.
     */
    protected RateLimitingProcessor createRateLimitingRequest(String apiKey,
            ServletRequest request, ServletResponse response,
            FilterChain filterChain) {
        return new RateLimitingProcessor(apiKey, request, response, filterChain);
    }

}
