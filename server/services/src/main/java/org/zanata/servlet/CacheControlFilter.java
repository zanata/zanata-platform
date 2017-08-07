package org.zanata.servlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CacheControlFilter implements Filter {
    private static final long ONE_DAY_MS = 86400000L;
    private static final long ONE_YEAR_MS = ONE_DAY_MS * 365;
    private boolean shouldCache;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        shouldCache = Boolean.valueOf(
                filterConfig.getInitParameter("shouldCache"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (shouldCache) {
            addCacheHeader(httpResponse);
        } else {
            addNoCacheHeader(httpResponse);
        }
        chain.doFilter(request, response);
    }

    protected void addNoCacheHeader(HttpServletResponse httpResponse) {
        long now = System.currentTimeMillis();
        httpResponse.setDateHeader("Date", now);
        httpResponse.setDateHeader("Expires", now - ONE_DAY_MS);
        httpResponse.setHeader("Cache-control",
                "public, no-cache, no-store, max-age=0, must-revalidate");
    }

    protected void addCacheHeader(HttpServletResponse httpResponse) {
        long now = System.currentTimeMillis();
        httpResponse.setDateHeader("Date", now);
        httpResponse.setDateHeader("Expires", now + ONE_YEAR_MS);
    }

    @Override
    public void destroy() {
    }
}
