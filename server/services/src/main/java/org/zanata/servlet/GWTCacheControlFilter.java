package org.zanata.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Filter} to add cache control headers for GWT generated files to ensure
 * that the correct files get cached.
 * <p>
 * From
 * http://seewah.blogspot.com.au/2009/02/gwt-tips-2-nocachejs-getting-cached
 * -in.html
 * <p>
 * Modified to follow GWT recommended headers more closely:
 * https://developers.google
 * .com/web-toolkit/doc/latest/DevGuideCompilingAndDebugging#perfect_caching See
 * also http://palizine.plynt.com/issues/2008Jul/cache-control-attributes/
 *
 * @author See Wah Cheng
 * @author Sean Flanigan <sflaniga@redhat.com>
 * @created 24 Feb 2009
 */
public class GWTCacheControlFilter extends CacheControlFilter {

    public void destroy() {
    }

    public void init(FilterConfig config) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        if (requestURI.contains(".cache.")) {
            addCacheHeader(httpResponse);
        } else if (requestURI.contains(".nocache.")) {
            addNoCacheHeader(httpResponse);
        }

        filterChain.doFilter(request, response);
    }
}
