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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.core.api.lifecycle.Destroyed;
import org.apache.deltaspike.core.api.lifecycle.Initialized;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class HttpRequestAndSessionHolder {
    private static final ThreadLocal<HttpServletRequest> REQUEST =
            new ThreadLocal<>();

    void setRequest(@Observes @Initialized HttpServletRequest request) {
        if (REQUEST.get() != null) {
            throw new IllegalStateException("There is already a request for this thread");
        }
        REQUEST.set(request);
    }

    void removeRequest(@Observes @Destroyed HttpServletRequest request) {
        REQUEST.remove();
    }

    public static Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(REQUEST.get());
    }

    public static Optional<HttpSession> getHttpSession() {
        Optional<HttpServletRequest> requestOpt = getRequest();
        if (requestOpt.isPresent()) {
            return Optional.of(requestOpt.get().getSession());
        }
        return Optional.empty();
    }

    public static Optional<HttpSession> getHttpSession(boolean create) {
        Optional<HttpServletRequest> requestOpt = getRequest();
        if (requestOpt.isPresent()) {
            return Optional.of(requestOpt.get().getSession(create));
        }
        return Optional.empty();
    }
}
