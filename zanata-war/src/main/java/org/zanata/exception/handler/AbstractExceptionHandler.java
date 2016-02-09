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

import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.deltaspike.core.util.ContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.FacesNavigationUtil;
import org.zanata.util.UrlUtil;

import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 * Base handler for all exceptions. N.B. this will handle exceptions from both
 * JSF and REST. For REST exceptions, this happens before REST ExceptionMapper
 * is called so we need to be careful not to use any jsf specific beans. e.g.
 * FacesMessages which is in WindowScoped.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class AbstractExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(AbstractExceptionHandler.class);
    @Inject
    protected FacesMessages messages;
    @Inject
    protected UrlUtil urlUtil;

    protected final <T extends Throwable> void handle(ExceptionEvent<T> event,
            LogLevel logLevel,
            FacesMessage.Severity severity,
            String messageKey,
            Object... messageArgs) {
        handle(event, logLevel, urlUtil.genericErrorPage(), severity,
                messageKey, messageArgs);
    }

    protected final <T extends Throwable> void handle(ExceptionEvent<T> event,
            LogLevel logLevel,
            String redirectUrl, FacesMessage.Severity severity,
            String messageKey,
            Object... messageArgs) {
        logException(logLevel, event.getException());

        if (ContextUtils.isContextActive(WindowScoped.class)) {
            messages.clear();
            messages.addFromResourceBundle(severity, messageKey, messageArgs);
            urlUtil.redirectTo(redirectUrl);
//            TODO urlUtil.forwardTo(redirectPath);

            // required - "stops" the JSF lifecycle
            FacesContext.getCurrentInstance().responseComplete();
        }
        // no other ExceptionHandler should handle this exception...
        event.handled();
    }

    protected static <T extends Throwable> void logException(LogLevel logLevel,
            T exception) {
        @Nullable
        String currentViewId = FacesNavigationUtil.getCurrentViewId();
        String msg;
        msg = currentViewId != null ?
                "exception happened in view: " + currentViewId :
                "exception caught";
        switch (logLevel) {
            case Trace:
                log.trace(msg, exception);
                break;
            case Debug:
                log.debug(msg, exception);
                break;
            case Warn:
                log.warn(msg, exception);
                break;
            case Error:
                log.error(msg, exception);
                break;
        }
    }

    enum LogLevel {
        Trace, Debug, Warn, Error
    }
}
