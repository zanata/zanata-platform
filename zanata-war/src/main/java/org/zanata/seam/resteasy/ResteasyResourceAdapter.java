// Implementation copied from Seam 2.3.1, commit f3077fe

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.zanata.seam.resteasy;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.ThreadLocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletInputMessage;
import org.jboss.resteasy.plugins.server.servlet.HttpServletResponseWrapper;
import org.jboss.resteasy.plugins.server.servlet.ServletSecurityContext;
import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.jboss.seam.Component;
import javax.annotation.PostConstruct;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.web.AbstractResource;
import org.jboss.seam.web.Session;

/**
 * Accepts incoming HTTP requests through the <tt>SeamResourceServlet</tt> and
 * dispatches the call to RESTEasy. Wraps the call in Seam contexts.
 *
 * @author Christian Bauer
 */
@javax.enterprise.context.ApplicationScoped
@Named("org.jboss.seam.resteasy.resourceAdapter")
@BypassInterceptors
@Install(precedence = BUILT_IN)
public class ResteasyResourceAdapter extends AbstractResource {

    @Logger
    Log log;

    protected Dispatcher dispatcher;
    protected Application application;

    @PostConstruct
    public void init() {
        // No injection, so lookup on first request
        dispatcher =
                (Dispatcher) Component
                        .getInstance("org.jboss.seam.resteasy.dispatcher");
        application = (Application) Component.getInstance(Application.class);
        if (dispatcher == null) {
            throw new IllegalStateException(
                    "ReasteasyDispatcher not available, make sure RESTEasy and all required JARs are on your classpath");
        }
    }

    @Override
    public String getResourcePath() {
        return application.getResourcePathPrefix();
    }

    @Override
    public void getResource(final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException, IOException {

        try {
            log.debug("processing REST request");

            // TODO: As far as I can tell from tracing RE code: All this
            // thread-local stuff has no effect because
            // the "default" provider factory is always used. But we do it
            // anyway, just to mimic the servlet handler
            // in RE...

            // Wrap in RESTEasy thread-local factory handling
            ThreadLocalResteasyProviderFactory.push(dispatcher
                    .getProviderFactory());

            // Wrap in RESTEasy contexts (this also puts stuff in a
            // thread-local)
            SeamResteasyProviderFactory
                    .pushContext(HttpServletRequest.class, request);
            SeamResteasyProviderFactory.pushContext(HttpServletResponse.class,
                    response);
            SeamResteasyProviderFactory.pushContext(SecurityContext.class,
                    new ServletSecurityContext(request));

            // Wrap in Seam contexts
            new ContextualHttpServletRequest(request) {
                @Override
                public void process() throws ServletException, IOException {
                    try {
                        ResteasyHttpHeaders headers =
                                ServletUtil.extractHttpHeaders(request);
                        ResteasyUriInfo uriInfo =
                                extractUriInfo(request,
                                        application.getResourcePathPrefix());

                        HttpResponse theResponse =
                                new HttpServletResponseWrapper(
                                        response,
                                        dispatcher.getProviderFactory()
                                );

                        // TODO: This requires a SynchronousDispatcher
                        HttpRequest in = new HttpServletInputMessage(
                                request,
                                response,
                                null,
                                null,
                                headers,
                                uriInfo,
                                request.getMethod().toUpperCase(),
                                (SynchronousDispatcher) dispatcher
                                );
                        // HttpRequest in = new HttpServletInputMessage(
                        // request,
                        // theResponse,
                        // headers,
                        // uriInfo,
                        // request.getMethod().toUpperCase(),
                        // (SynchronousDispatcher) dispatcher
                        // );

                        dispatcher.invoke(in, theResponse);
                    } finally {
                        /*
                         * Prevent anemic sessions clog up the server
                         *
                         * session.isNew() check - do not close non-anemic
                         * sessions established by the view layer (JSF) which
                         * are reused by the JAX-RS requests (so that the
                         * requests do not have to be re-authorized)
                         */
                        if (application.isDestroySessionAfterRequest()
                                && request.getSession().isNew()) {
                            log.debug(
                                    "Destroying HttpSession after REST request");
                            Session.instance().invalidate();
                        }
                    }
                }
            }.run();

        } finally {
            // Clean up the thread-locals
            SeamResteasyProviderFactory.clearContextData();
            ThreadLocalResteasyProviderFactory.pop();
            log.debug("completed processing of REST request");
        }
    }

    protected ResteasyUriInfo extractUriInfo(HttpServletRequest request,
            String pathPrefix) {
        try {
            // Append a slash if there isn't one
            if (!pathPrefix.startsWith("/")) {
                pathPrefix = "/" + pathPrefix;
            }

            // Get the full path of the current request
            URL requestURL = new URL(request.getRequestURL().toString());
            String requestPath = requestURL.getPath();

            // Find the 'servlet mapping prefix' for RESTEasy (in our case:
            // /seam/resource/rest)
            String mappingPrefix =
                    requestPath.substring(0, requestPath.indexOf(pathPrefix)
                            + pathPrefix.length());

            // Still is /<context>/seam/resource/rest, so cut off the context
            mappingPrefix =
                    mappingPrefix.substring(request.getContextPath().length());

            log.debug("Using request mapping prefix: " + mappingPrefix);

            // This is the prefix used by RESTEasy to resolve resources and
            // generate URIs with
            return ServletUtil.extractUriInfo(request, mappingPrefix);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
