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

import java.util.ArrayList;

import org.zanata.webtrans.client.ui.HasUpdateValidationMessage;

import com.google.gwt.event.shared.GwtEvent;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class RunValidationEvent extends GwtEvent<RunValidationEventHandler> {
    /**
     * Handler type.
     */
    private static final Type<RunValidationEventHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<RunValidationEventHandler> getType() {
        return TYPE;
    }

    private String source, target;
    private boolean fireNotification = true;
    private ArrayList<HasUpdateValidationMessage> widgetList =
            new ArrayList<HasUpdateValidationMessage>();

    public RunValidationEvent(String source, String target,
            boolean fireNotification) {
        this.source = source;
        this.target = target;
        this.fireNotification = fireNotification;
    }

    @Override
    public Type<RunValidationEventHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(RunValidationEventHandler handler) {
        handler.onValidate(this);
    }

    public String getSourceContent() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public boolean isFireNotification() {
        return fireNotification;
    }

    public void addWidget(HasUpdateValidationMessage validationMessagePanel) {
        widgetList.add(validationMessagePanel);
    }

    public ArrayList<HasUpdateValidationMessage> getWidgetList() {
        return widgetList;
    }
}
