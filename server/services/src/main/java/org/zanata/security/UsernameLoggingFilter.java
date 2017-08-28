// Adapted from org.jboss.seam.web.LoggingFilter in Seam 2.3.1

package org.zanata.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.MDC;
import org.zanata.servlet.MDCInsertingServletFilter;

/**
 * This filter adds the authenticated username to the slf4j
 * mapped diagnostic context so that it can be included in
 * formatted log output if desired, by adding %X{username}
 * to the pattern.
 *
 * @author Eric Trautman
 */
@WebFilter
public class UsernameLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest) servletRequest).getSession(false);
        if (session != null) {
            ZanataIdentity identity =
                    BeanProvider.getContextualReference(ZanataIdentity.class);
            ZanataCredentials credentials = identity.getCredentials();
            String username = credentials != null ? credentials.getUsername() : null;
            if (username != null) {
                MDC.put(MDCInsertingServletFilter.USERNAME, username);
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(MDCInsertingServletFilter.USERNAME);
        }
    }

    @Override
    public void destroy() {
    }
}
