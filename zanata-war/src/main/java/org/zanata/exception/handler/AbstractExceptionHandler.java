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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.FacesNavigationUtil;
import org.zanata.util.UrlUtil;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class AbstractExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractExceptionHandler.class);
    @Inject
    protected FacesMessages messages;
    @Inject
    protected UrlUtil urlUtil;

    protected final <T extends Throwable> void handle(ExceptionEvent<T> event,
                                                      LogLevel logLevel,
                                                      FacesMessage.Severity severity,
                                                      String messageKey,
                                                      Object... messageArgs) {
        handle(event, logLevel, urlUtil.genericErrorPage(), severity, messageKey, messageArgs);
    }

    protected final <T extends Throwable> void handle(ExceptionEvent<T> event,
                                                      LogLevel logLevel,
                                                      String redirectUrl, FacesMessage.Severity severity,
                                                      String messageKey,
                                                      Object... messageArgs) {
        logException(logLevel, event.getException());

        messages.clear();
        messages.addFromResourceBundle(severity, messageKey, messageArgs);
        urlUtil.redirectTo(redirectUrl);

        // required - "stops" the JSF lifecycle
        FacesContext.getCurrentInstance().responseComplete();
        // no other JSF ExceptionHandler should handle this exception...
        event.handled();
    }

    protected static <T extends Throwable> void logException(LogLevel logLevel, T exception) {
        String currentViewId = FacesNavigationUtil.getCurrentViewId();

        switch (logLevel) {
            case Trace:
                log.trace("exception happened in view: {}", currentViewId, exception);
                break;
            case Debug:
                log.debug("exception happened in view: {}", currentViewId, exception);
                break;
            case Warn:
                log.warn("exception happened in view: {}", currentViewId, exception);
                break;
            case Error:
                log.error("exception happened in view: {}", currentViewId, exception);
                break;
        }
    }

    static enum LogLevel {
        Trace, Debug, Warn, Error
    }
}
