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

import java.util.Map;
import java.util.Set;

import org.zanata.webtrans.client.keys.EventWrapper;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.view.AttentionKeyShortcutDisplay;

import com.google.gwt.dom.client.NativeEvent;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class AttentionKeyShortcutPresenter extends WidgetPresenter<AttentionKeyShortcutDisplay>
{

   private final EventWrapper event;

   // hold a map of shortcuts similar to KeyShortcutPresenter, accessible by KeyShortcutPresenter
   private Map<Keys, Set<KeyShortcut>> shortcutMap;


   // hold awareness of the current shortcut key (KeyShortcutPresenter can look it up from here)
   // (Note: this will come from user config, this is just the definitive place that looks it up from there)

   // rely on KeyShortcutPresenter to invoke event handling at an appropriate time

   @Inject
   public AttentionKeyShortcutPresenter(AttentionKeyShortcutDisplay display,
                                        EventBus eventBus,
                                        final EventWrapper event)
   {
      super(display, eventBus);
      this.event = event;
   }

   @Override
   protected void onBind()
   {

      // TODO look up attention key setting from user settings

      // TODO register attention shortcut
      // Note: keep the registration handle so it can be unregistered if the user changes the setting.
   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void onRevealDisplay()
   {
      // TODO Auto-generated method stub

   }

   public boolean isAttentionMode()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public void processKeyEvent(NativeEvent evt)
   {
      // TODO Auto-generated method stub

      Keys pressedKeys = event.createKeys(evt);
   }

}
