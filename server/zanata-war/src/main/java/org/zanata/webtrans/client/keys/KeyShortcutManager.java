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
package org.zanata.webtrans.client.keys;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zanata.webtrans.client.events.KeyShortcutEvent;

import com.google.gwt.dom.client.NativeEvent;

/**
 * Responsible for maintaining a map of key shortcuts, including invocation and
 * registration handling.
 *
 * To use this class:
 * <ol>
 * <li>implement {@link #getKeys} to determine which type of shortcuts the
 * implementation responds to.</li>
 * <li>Implement {@link #handleIfMatchingShortcut} to define the behaviour when
 * a shortcut given to {@link #processKeyEvent} matches an added shortcut, as
 * well as any additional conditions that need to be checked before firing. Use
 * {@link #triggerShortcutEvent} to actually trigger the shortcut, or return
 * false to indicate that the shortcut was not triggered.</li>
 * <li>Optionally, override {@link #handleNonMatchedShortcut} to respond to
 * events that have not triggered a shortcut.</li>
 * </ol>
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public abstract class KeyShortcutManager {
    private final EventWrapper event;

    public KeyShortcutManager(final EventWrapper event) {
        this.event = event;
    }

    /**
     * Key uses {@link Keys#hashCode()}
     */
    private Map<Keys, Set<KeyShortcut>> shortcutMap;

    public Map<Keys, Set<KeyShortcut>> ensureShortcutMap() {
        if (shortcutMap == null) {
            shortcutMap = new HashMap<Keys, Set<KeyShortcut>>();
        }
        return shortcutMap;
    }

    public void add(KeyShortcut shortcut) {
        for (Keys keys : getKeys(shortcut)) {
            Set<KeyShortcut> shortcuts = ensureShortcutMap().get(keys);
            if (shortcuts == null) {
                shortcuts = new HashSet<KeyShortcut>();
                ensureShortcutMap().put(keys, shortcuts);
            }
            shortcuts.add(shortcut);
        }
    }

    public void remove(KeyShortcut shortcut) {
        for (Keys keys : getKeys(shortcut)) {
            Set<KeyShortcut> shortcuts = ensureShortcutMap().get(keys);
            if (shortcuts != null) {
                shortcuts.remove(shortcut);
            }
        }
    }

    public void processKeyEvent(NativeEvent evt) {
        Keys pressedKeys = event.createKeys(evt);
        Set<KeyShortcut> similarShortcuts =
                ensureShortcutMap().get(pressedKeys);
        boolean firedEvent = false;
        KeyShortcutEvent shortcutEvent = new KeyShortcutEvent(pressedKeys);
        if (similarShortcuts != null && !similarShortcuts.isEmpty()) {
            for (KeyShortcut shortcut : similarShortcuts) {
                boolean matchingEventType =
                        shortcut.getKeyEvent().nativeEventType.equals(event
                                .getType(evt));
                if (matchingEventType) {
                    firedEvent |=
                            handleIfMatchingShortcut(evt, shortcut,
                                    shortcutEvent);
                }
            }
        }

        if (!firedEvent) {
            handleNonMatchedShortcut(evt, shortcutEvent);
        }
    }

    /**
     * Generate an event for the relevant subscriber to this shortcut.
     *
     * @param evt
     * @param shortcut
     * @param shortcutEvent
     */
    public void triggerShortcutEvent(NativeEvent evt, KeyShortcut shortcut,
            KeyShortcutEvent shortcutEvent) {
        if (shortcut.isStopPropagation()) {
            evt.stopPropagation();
        }
        if (shortcut.isPreventDefault()) {
            evt.preventDefault();
        }
        shortcut.getHandler().onKeyShortcut(shortcutEvent);
    }

    /**
     * Implement to get the appropriate set of keys for a shortcut.
     *
     * e.g. only the attention keys
     */
    public abstract Set<Keys> getKeys(KeyShortcut shortcut);

    /**
     * Implement to respond when user input triggers a key shortcut
     *
     * @param evt
     *            representing user input
     * @param shortcut
     *            that matches user input
     * @param shortcutEvent
     *            to use in shortcut.getHandler().onKeyShortcut(shortcutEvent)
     * @return true if the key shortcut was triggered
     */
    protected abstract boolean handleIfMatchingShortcut(NativeEvent evt,
            KeyShortcut shortcut, KeyShortcutEvent shortcutEvent);

    /**
     * Implement to respond to key events that have not triggered any handler.
     * Default implementation does nothing.
     */
    protected void handleNonMatchedShortcut(NativeEvent evt,
            KeyShortcutEvent shortcutEvent) {
    }

}
