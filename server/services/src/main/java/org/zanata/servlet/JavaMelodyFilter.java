/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.servlet;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zanata.security.ZanataIdentity;

import net.bull.javamelody.MonitoringFilter;

/**
 * Modifies JavaMelody's MonitoringFilter so that only admin users can
 * access the JavaMelody console.
 */
public class JavaMelodyFilter extends MonitoringFilter {
    @Inject
    private ZanataIdentity identity;

    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getRequestURI().equals(getMonitoringUrl(httpRequest))) {
            if (identity == null || !identity.isLoggedIn()) {
                String signInUrl =
                        httpRequest.getContextPath()
                                + "/account/sign_in";
                httpResponse.sendRedirect(signInUrl);
            } else if (!identity.hasRole("admin")) {
                httpResponse.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Only admin can access monitoring!");
            }

            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

}
