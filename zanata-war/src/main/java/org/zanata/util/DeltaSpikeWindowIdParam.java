/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.util;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class DeltaSpikeWindowIdParam {
    /**
     * Returns a query string which, when appended to a URL without a query
     * string, will preserve the DeltaSpike window id (dswid) of the current
     * request, if any. eg "?dswid=1234". The string will be empty if there
     * is no current window context.
     * NB: This method assumes that the URL does not contain '?' yet.
     * @param windowContext
     * @return
     */
    @Dependent
    @Named("dswidQuery")
    @Produces
    String getDswidQuery(WindowContext windowContext) {
        String windowId = windowContext.getCurrentWindowId();
        if (windowId != null)
            return "?dswid=" + windowId;
        else
            return "";
    }

    /**
     * Returns a query string which, when appended to a URL with a query
     * string, will preserve the DeltaSpike window id (dswid) of the current
     * request, if any. eg "&dswid=1234". The string will be empty if there
     * is no current window context.
     * NB: This method assumes that the URL already contains '?'.
     * @param windowContext
     * @return
     */
    @Dependent
    @Named("dswidParam")
    @Produces
    String getDswidParam(WindowContext windowContext) {
        String windowId = windowContext.getCurrentWindowId();
        if (windowId != null)
            return "&dswid=" + windowId;
        else
            return "";
    }
}
