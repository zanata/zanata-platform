/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Fired to trigger a new validation event from an object that does not have
 * access to appropriate details to generate a {@link RunValidationEvent}.
 *
 * @author David Mason <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 **/
public class RequestValidationEvent extends
        GwtEvent<RequestValidationEventHandler> {
    public static final RequestValidationEvent EVENT =
            new RequestValidationEvent();
    /**
     * Handler type.
     */
    private static final Type<RequestValidationEventHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<RequestValidationEventHandler> getType() {
        return TYPE;
    }

    private RequestValidationEvent() {
    }

    @Override
    public Type<RequestValidationEventHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(RequestValidationEventHandler handler) {
        handler.onRequestValidation(this);
    }
}
