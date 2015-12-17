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
import org.apache.log4j.MDC;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import javax.inject.Named;
import org.zanata.servlet.MDCInsertingServletFilter;

/**
 * This filter adds the authenticated user name to the log4j
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
        filterChain.doFilter(servletRequest, servletResponse);
        MDC.remove(MDCInsertingServletFilter.USERNAME);
    }

    @Override
    public void destroy() {
    }
}
