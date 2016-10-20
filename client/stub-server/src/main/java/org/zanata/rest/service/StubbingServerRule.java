/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.rest.service;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.google.common.base.Throwables;

/**
 * This will start up a jetty server and host stubbed Zanata rest resources. All
 * the resource implementation will either return fixed response or, if not used
 * by client right now, throw exception.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class StubbingServerRule implements TestRule {
    private static Server server;

    public StubbingServerRule() {
        startServerIfRequired();
    }

    private static void startServerIfRequired() {
        if (server != null && server.isStarted()) {
            return;
        }
        server = new Server(0);
        ServletContextHandler context =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        ServletHolder holder =
                new ServletHolder(new HttpServlet30Dispatcher());
        holder.setInitParameter("javax.ws.rs.Application",
                MockResourcesApplication.class.getCanonicalName());
        context.addServlet(holder, "/*");
        server.setHandler(context);
        server.setStopAtShutdown(true);
        try {
            server.start();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return base;
    }

    public URI getServerBaseUri() {
        return server.getURI();
    }
}

