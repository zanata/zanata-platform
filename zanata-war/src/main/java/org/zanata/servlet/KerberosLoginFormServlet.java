/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a servlet that serves content to automatically redirect the browser
 * to a JSF login page. This is needed because the SPNEGO valve used by JBossWeb
 * does not support providing a facelet as an alternative login page.
 * Furthermore, to keep any parameters on the facelet page (i.e. the 'continue'
 * paramater), there needs to be a programatic step.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class KerberosLoginFormServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String queryStr = req.getQueryString(); // keep the original request's
                                                // query string
        if (queryStr != null && !queryStr.isEmpty()) {
            queryStr = "?" + queryStr;
        }

        // Content is serverd statically as RequestDispatcher.include doesn't
        // work
        // for the same reason that forced the creation of this servlet.
        // Response.sendRedirect
        // doesn't work either, as the already inserted 404 code prevents a
        // redirect operation.
        resp.getWriter()
                .println(
                        "<html>\n"
                                + "<head>"
                                + "<meta http-equiv=\"refresh\" content=\"0; url=sign_in"
                                + queryStr + "\">" + "</head>" + "</html>");
        resp.flushBuffer();
    }
}
