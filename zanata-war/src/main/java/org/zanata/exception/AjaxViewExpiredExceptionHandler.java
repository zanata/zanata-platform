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
package org.zanata.exception;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION_TYPE;
import static javax.servlet.RequestDispatcher.ERROR_MESSAGE;
import static javax.servlet.RequestDispatcher.ERROR_REQUEST_URI;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;

import java.util.Iterator;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zanata.i18n.Messages;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ServiceLocator;

/**
 * Exception handler for JSF's ViewExpiredException. This only comes into action
 * for AJAX requets. For non-ajax handling see pages.xml
 *
 * This class is partially based on ideas from OmniFaces' Ajax exception handler.
 * (https://github.com/omnifaces/omnifaces/blob/master/src/main/java/org/omnifaces/exceptionhandler/FullAjaxExceptionHandler.java)
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AjaxViewExpiredExceptionHandler extends ExceptionHandlerWrapper {
    private ExceptionHandler wrapped;

    public AjaxViewExpiredExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handle() throws FacesException {
        handleAjaxException();
        wrapped.handle();
    }

    private void handleAjaxException() {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext == null
                || !facesContext.getPartialViewContext().isAjaxRequest()) {
            return; // Not an ajax request.
        }

        Iterator<ExceptionQueuedEvent> unhandledExceptionQueuedEvents =
                getUnhandledExceptionQueuedEvents().iterator();

        if (!unhandledExceptionQueuedEvents.hasNext()) {
            return; // There's no unhandled exception.
        }

        Throwable exception =
                unhandledExceptionQueuedEvents.next().getContext()
                        .getException();

        exception = unwrap(exception);

        if (exception instanceof ViewExpiredException) {
            Messages msgs =
                    ServiceLocator.instance().getInstance(Messages.class);
            FacesMessages jsfMessages =
                    ServiceLocator.instance().getInstance(
                            FacesMessages.class);
            HttpServletRequest request =
                    (HttpServletRequest) facesContext.getExternalContext()
                            .getRequest();
            jsfMessages.addGlobal(msgs.get("jsf.ViewExpiredException.AjaxError"));

            // Set the necessary servlet request attributes which a bit
            // decent error page may expect.
            request.setAttribute(ERROR_EXCEPTION, exception);
            request.setAttribute(ERROR_EXCEPTION_TYPE, exception.getClass());
            request.setAttribute(ERROR_MESSAGE, exception.getMessage());
            request.setAttribute(ERROR_REQUEST_URI, request.getRequestURI());
            request.setAttribute(ERROR_STATUS_CODE,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            unhandledExceptionQueuedEvents.remove();
        }
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    private static <T extends Throwable> Throwable unwrap(Throwable exception,
            Class<T> type) {
        Throwable unwrappedException = exception;
        while (type.isInstance(unwrappedException)
                && unwrappedException.getCause() != null) {
            unwrappedException = unwrappedException.getCause();
        }
        return unwrappedException;
    }

    /**
     * Unwraps exceptions fired as ELException or FacesException only.
     *
     * @param exception
     * @return
     */
    private static Throwable unwrap(Throwable exception) {
        return unwrap(unwrap(exception, FacesException.class),
                ELException.class);
    }
}
