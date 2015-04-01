/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;

import java.lang.reflect.Field;

/**
 * Compatibility shim to help migrate from Seam events to CDI events.
 * <br/>
 * TODO: remove class after CDI switch; use javax.enterprise.event.Event
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @see javax.enterprise.event.Event
 */
@AutoCreate
@Name("event")
@Scope(ScopeType.STATELESS)
public class Event<P> {

    public void fire(P payload) {
        if (Events.exists()) {
            Events.instance().raiseEvent(payload.getClass().getName(), payload);
        }
    }

    public void fireAsync(P payload) {
        if (Events.exists()) {
            Events.instance().raiseAsynchronousEvent(payload.getClass().getName(), payload);
        }
    }

    public void fireAfterSuccess(P payload) {
        if (Events.exists()) {
            Events.instance().raiseTransactionSuccessEvent(payload.getClass().getName(), payload);
        }
    }

}
