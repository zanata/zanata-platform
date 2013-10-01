/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;

/**
 * Allows client-side user scripts to register event callbacks.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class UserScriptCallbackHook implements TransUnitUpdatedEventHandler {

    public UserScriptCallbackHook() {
        attachCallbackMapToWindow();
    }

    private static native void attachCallbackMapToWindow()/*-{
    $wnd.zanata = {
      description : "Add callback functions to the arrays for the available event types.\n"
          + " Callbacks take a single argument.",
      events : {
        transUnitUpdated : {
          description : "Called whenever a translation is changed by any user.",
          callbacks : []
        }
      }
    };
    }-*/;

    @Override
    public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
        triggerAllTransUnitUpdatedCallbacksWith(event);
    }

    private static native void triggerAllTransUnitUpdatedCallbacksWith(
            TransUnitUpdatedEvent event)/*-{
    callbacks = $wnd.zanata.events.transUnitUpdated.callbacks;
    for ( var i = 0; i < callbacks.length; i++) {
      callbacks[i](event);
    }
    }-*/;

}
