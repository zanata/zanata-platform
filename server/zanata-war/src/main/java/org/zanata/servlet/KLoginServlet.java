/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import org.zanata.security.AuthenticationManager;
import org.zanata.security.UserRedirectBean;
import org.zanata.util.UrlUtil;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet which serves as a landing place to perform kerberos ticket
 * based authentication.
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@WebServlet(
        urlPatterns = {"/account/klogin"}
)
public class KLoginServlet extends HttpServlet {

    @Inject
    private UserRedirectBean userRedirect;

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private UrlUtil urlUtil;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Set the continue parameter
        String continueUrl = req.getParameter("continue");
        if (continueUrl != null) {
            userRedirect.setEncodedUrl(continueUrl);
        }
        // perform the authentication
        authenticationManager.kerberosLogin();
        performRedirection(resp, continueUrl);
    }

    /**
     * Performs the redirection based on the results from the authentication
     * process.
     * This is logic that would normally be in faces-config.xml, but as this is
     * a servlet, it cannot take advantage of that.
     */
    private void performRedirection(HttpServletResponse resp,
            String continueUrl) throws IOException {
        String authRedirectResult =
                authenticationManager.getAuthenticationRedirect();
        switch (authRedirectResult) {
            case "login":
                resp.sendRedirect(urlUtil.signInPage());
                break;

            case "edit":
                resp.sendRedirect(urlUtil.createUserPage());
                break;

            case "inactive":
                resp.sendRedirect(urlUtil.inactiveAccountPage());
                break;

            case "dashboard":
                resp.sendRedirect(urlUtil.dashboardUrl());
                break;

            case "home":
                resp.sendRedirect(urlUtil.home());
                break;

            case "redirect":
                resp.sendRedirect(continueUrl);
                break;

            default:
                throw new RuntimeException(
                        "Unexpected authentication manager result: " +
                                authRedirectResult);
        }
    }
}
