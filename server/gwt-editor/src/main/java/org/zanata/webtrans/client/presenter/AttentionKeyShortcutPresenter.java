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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.AttentionModeActivationEvent;
import org.zanata.webtrans.client.events.AttentionModeActivationEventHandler;
import org.zanata.webtrans.client.view.AttentionKeyShortcutDisplay;

import com.google.inject.Inject;

/**
 * Responsible for getting the necessary model data to show when attention mode
 * is active in response to relevant events.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class AttentionKeyShortcutPresenter extends
        WidgetPresenter<AttentionKeyShortcutDisplay> {

    @Inject
    public AttentionKeyShortcutPresenter(AttentionKeyShortcutDisplay display,
            EventBus eventBus) {
        super(display, eventBus);
    }

    @Override
    protected void onBind() {
        registerHandler(eventBus.addHandler(
                AttentionModeActivationEvent.getType(),
                this::respondToAttentionModeEvent));
    }

    @Override
    protected void onUnbind() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onRevealDisplay() {
        // TODO Auto-generated method stub

    }

    private void
            respondToAttentionModeEvent(AttentionModeActivationEvent event) {
        // if (event.isActive())
        // {
        // display.clearShortcuts();
        // for (KeyShortcut sc : event.getShortcuts())
        // {
        // Keys firstKeys = sc.getAllAttentionKeys().iterator().next();
        // addShortcutToView(firstKeys, sc.getDescription());
        // }
        // }
        display.showOrHide(event.isActive());
    }

}
