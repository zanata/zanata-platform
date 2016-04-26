/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.exception.handler;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.zanata.exception.LockTimeoutException;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ExceptionHandler
public class LockTimeoutExceptionHandler extends AbstractExceptionHandler {
    @Inject
    @DeltaSpike
    private HttpServletResponse response;

    public void handleException(@Handles ExceptionEvent<LockTimeoutException> event) {
        logException(LogLevel.Trace, event.getException());
        try {
            response.sendError(503);
        } catch (IOException e) {
            logException(LogLevel.Warn, e);
        }
        // required - "stops" the JSF lifecycle
        FacesContext.getCurrentInstance().responseComplete();
        // no other JSF ExceptionHandler should handle this exception...
        event.handled();
    }

}
