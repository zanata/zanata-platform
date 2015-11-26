/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.servlet;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.core.api.lifecycle.Destroyed;
import org.apache.deltaspike.core.api.lifecycle.Initialized;

/**
 * DeltaSpike will always inject a HttpServletRequest or HttpSession proxy
 * object even when you don't have an active request. Invoke any method on the
 * proxy object will result in an IllegalStateException. This way we will be
 * able to get an optional request or session object in the cases where it's
 * optional.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class HttpRequestAndSessionHolder {
    private static final ThreadLocal<HttpServletRequest> REQUEST =
            new ThreadLocal<>();

    private static String defaultServerPath;
    private static String scheme;
    private static String serverName;
    private static int serverPort;

    void setFacesRequest(@Observes @Initialized FacesContext facesRequest) {
        Object requestObj = facesRequest.getExternalContext().getRequest();
        if (requestObj instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) requestObj;
            REQUEST.set(request);
            // TODO if we have jms message in the queue when starting up server, there is no request coming yet and defaultServerPath can be null
            if (defaultServerPath == null) {
                scheme = request.getScheme();
                serverName = request.getServerName();
                serverPort = request.getServerPort();
                defaultServerPath = scheme + "://" + serverName
                        + ":" + serverPort
                        + request.getContextPath();
            }
        }
    }

    void removeRequest(@Observes @Destroyed FacesContext facesContext) {
        Object requestObj = facesContext.getExternalContext().getRequest();
        if (requestObj instanceof HttpServletRequest) {
            REQUEST.remove();
        }
    }

    public static Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(REQUEST.get());
    }

    public static Optional<HttpSession> getHttpSession(boolean create) {
        Optional<HttpServletRequest> requestOpt = getRequest();
        if (requestOpt.isPresent()) {
            return Optional.of(requestOpt.get().getSession(create));
        }
        return Optional.empty();
    }

    public static String getDefaultServerPath() {
        return defaultServerPath;
    }

    public static String getScheme() {
        return scheme;
    }

    public static String getServerName() {
        return serverName;
    }

    public static int getServerPort() {
        return serverPort;
    }
}
