/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.keys;

import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut.KeyEvent;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;

/**
 * Represents a catcher for key events that do not trigger key shortcuts, for
 * registration with {@link KeyShortcutPresenter}
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public class SurplusKeyListener {

    private final KeyEvent keyEvent;
    private final ShortcutContext context;
    private final boolean stopPropagation;
    private final boolean preventDefault;
    private final KeyShortcutEventHandler handler;

    /**
     * Create a surplus key listener, optionally preventing propagation or
     * default actions of native events.
     *
     * @param keyEvent
     *            which key event to respond to
     * @param context
     *            part of the application in which this listener should be
     *            active
     * @param stopPropagation
     * @param preventDefault
     * @param handler
     */
    public SurplusKeyListener(KeyEvent keyEvent, ShortcutContext context,
            boolean stopPropagation, boolean preventDefault,
            KeyShortcutEventHandler handler) {
        this.keyEvent = keyEvent;
        this.context = context;
        this.stopPropagation = stopPropagation;
        this.preventDefault = preventDefault;
        this.handler = handler;
    }

    /**
     * Create a surplus key listener that does not prevent propagation or
     * default actions of native events.
     *
     * @param keyEvent
     * @param context
     * @param handler
     *
     * @see #SurplusKeyListener(KeyEvent, ShortcutContext, boolean, boolean,
     *      KeyShortcutEventHandler)
     */
    public SurplusKeyListener(KeyEvent keyEvent, ShortcutContext context,
            KeyShortcutEventHandler handler) {
        this(keyEvent, context, false, false, handler);
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    public ShortcutContext getContext() {
        return context;
    }

    public boolean isStopPropagation() {
        return stopPropagation;
    }

    public boolean isPreventDefault() {
        return preventDefault;
    }

    public KeyShortcutEventHandler getHandler() {
        return handler;
    }

    @Override
    public int hashCode() {
        return keyEvent.ordinal() + context.ordinal() * 8;
    }

    /**
     * Two {@link SurplusKeyListener} objects are equal if they have the same
     * key event type and context.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SurplusKeyListener)) {
            return false;
        }
        SurplusKeyListener other = (SurplusKeyListener) obj;
        return keyEvent == other.keyEvent && context == other.context;
    }
}
