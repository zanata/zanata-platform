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
package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.keys.EventWrapper;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.view.KeyShortcutDisplay;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.view.client.ListDataProvider;


/**
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class KeyShortcutPresenterTest
{
   static final String TEST_MESSAGE_CLOSE_SHORTCUT_VIEW = "close shortcut view";
   static final String TEST_MESSAGE_SHOW_AVAILABLE_KEY_SHORTCUTS = "show available key shortcuts";

   // These could be any ints for the purpose of this test, as long as key codes don't over
   static final int KEY_DOWN_TYPE = 0x80;
   static final int KEY_UP_TYPE = 0x200;

   static final int KEY_CODE_Y = 'Y';
   static final String TEST_MESSAGE_APPLICATION_SCOPE = "Application";

   //object under test
   KeyShortcutPresenter keyShortcutPresenter;


   @Mock
   private KeyShortcutDisplay mockDisplay;
   @Mock
   private EventWrapper mockEventWrapper;
   @Mock
   private EventBus mockEventBus;
   @Mock
   private WebTransMessages mockMessages;

   @Captor
   private ArgumentCaptor<NativePreviewHandler> capturedNativePreviewHandler;
   @Mock
   private HandlerRegistration handlerRegistration;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);

      keyShortcutPresenter = new KeyShortcutPresenter(mockDisplay,
            mockEventBus, mockMessages, mockEventWrapper);

      when(mockMessages.closeShortcutView()).thenReturn(TEST_MESSAGE_CLOSE_SHORTCUT_VIEW);
      when(mockMessages.showAvailableKeyShortcuts()).thenReturn(TEST_MESSAGE_SHOW_AVAILABLE_KEY_SHORTCUTS);
      when(mockMessages.applicationScope()).thenReturn(TEST_MESSAGE_APPLICATION_SCOPE);
      when(mockEventWrapper.addNativePreviewHandler(capturedNativePreviewHandler.capture())).thenReturn(handlerRegistration);
      when(mockEventWrapper.keyDownEvent()).thenReturn(KEY_DOWN_TYPE);
      when(mockEventWrapper.keyUpEvent()).thenReturn(KEY_UP_TYPE);
   }

   @Test
   public void testExpectedActionsOnBind()
   {
      keyShortcutPresenter.bind();

      verify(mockEventWrapper).addNativePreviewHandler(capturedNativePreviewHandler.capture());
   }

   @Test
   public void displaysRegisteredShortcutsInOrder()
   {
      @SuppressWarnings("unchecked")
      ListDataProvider<KeyShortcut> mockDataProvider = mock(ListDataProvider.class);
      List<KeyShortcut> shortcutList = new ArrayList<KeyShortcut>();
      when(mockDataProvider.getList()).thenReturn(shortcutList);
      mockDisplay.clearPanel();
      when(mockDisplay.addContext(TEST_MESSAGE_APPLICATION_SCOPE)).thenReturn(mockDataProvider);
      mockDisplay.showPanel();

      keyShortcutPresenter.bind();
      keyShortcutPresenter.showShortcuts();

      //Shortcut list should contain Alt+Y and Esc shortcuts
      assertThat("KeyShortcutPresenter should register 2 global shortcuts", shortcutList.size(), is(3));

      // esc should be first as it has no modifiers
      Set<Keys> firstShortcutKeys = shortcutList.get(0).getAllKeys();
      String firstShortcut = "first shortcut should be Esc with no modifiers";
      assertThat(firstShortcut, firstShortcutKeys.size(), is(1));
      Keys firstShortcutFirstKeys = firstShortcutKeys.iterator().next();
      assertThat(firstShortcut, firstShortcutFirstKeys.getModifiers(), is(0));
      assertThat(firstShortcut, firstShortcutFirstKeys.getKeyCode(), is(KeyCodes.KEY_ESCAPE));

      // Alt+X should be next
      Set<Keys> secondShortcutKeys = shortcutList.get(1).getAllKeys();
      String secondShortcut = "second shortcut should be Alt+X";
      assertThat(secondShortcut, secondShortcutKeys.size(), is(1));
      Keys secondShortcutFirstKeys = secondShortcutKeys.iterator().next();
      assertThat(secondShortcut, secondShortcutFirstKeys.getModifiers(), is(Keys.ALT_KEY));
      assertThat(secondShortcut, secondShortcutFirstKeys.getKeyCode(), is((int) 'X'));

      // Alt+Y should be last
      Set<Keys> thirdShortcutKeys = shortcutList.get(2).getAllKeys();
      String thirdShortcut = "third shortcut should be Alt+Y";
      assertThat(thirdShortcut, thirdShortcutKeys.size(), is(1));
      Keys thirdShortcutFirstKeys = thirdShortcutKeys.iterator().next();
      assertThat(thirdShortcut, thirdShortcutFirstKeys.getModifiers(), is(Keys.ALT_KEY));
      System.out.println(thirdShortcutFirstKeys.getKeyCode() + " int Y:" + ((int) 'Y') + " int X:" + ((int) 'X'));
      assertThat(thirdShortcut, thirdShortcutFirstKeys.getKeyCode(), is((int) 'Y'));
   }

   @Test(enabled = false, description = "pending attention key rewrite")
   public void testRespondsToAltY()
   {
      NativePreviewEvent mockNativePreviewEvent = mock(NativePreviewEvent.class);
      NativeEvent mockNativeEvent = mock(NativeEvent.class);

      when(mockEventWrapper.getTypeInt(mockNativePreviewEvent)).thenReturn(KEY_DOWN_TYPE);
      when(mockNativePreviewEvent.getNativeEvent()).thenReturn(mockNativeEvent);
      when(mockEventWrapper.createKeys(mockNativeEvent)).thenReturn(new Keys(Keys.ALT_KEY, KEY_CODE_Y));
      when(mockEventWrapper.getType(mockNativeEvent)).thenReturn(KeyShortcut.KeyEvent.KEY_DOWN.nativeEventType);

      @SuppressWarnings("unchecked")
      ListDataProvider<KeyShortcut> mockDataProvider = mock(ListDataProvider.class);
      List<KeyShortcut> shortcutList = new ArrayList<KeyShortcut>();
      when(mockDataProvider.getList()).thenReturn(shortcutList);
      mockDisplay.clearPanel();
      when(mockDisplay.addContext(TEST_MESSAGE_APPLICATION_SCOPE)).thenReturn(mockDataProvider);
      mockDisplay.showPanel();

      keyShortcutPresenter.bind();
      capturedNativePreviewHandler.getValue().onPreviewNativeEvent(mockNativePreviewEvent);

      //Shortcut list should contain Alt+Y and Esc shortcuts
      assertThat("KeyShortcutPresenter should register 2 global shortcuts", shortcutList.size(), is(2));

      // esc should be first as it has no modifiers
      Set<Keys> firstShortcutKeys = shortcutList.get(0).getAllKeys();
      String firstShortcut = "first shortcut should be Esc with no modifiers and no aliases";
      assertThat(firstShortcut, firstShortcutKeys.size(), is(1));
      Keys firstShortcutFirstKeys = firstShortcutKeys.iterator().next();
      assertThat(firstShortcut, firstShortcutFirstKeys.getModifiers(), is(0));
      assertThat(firstShortcut, firstShortcutFirstKeys.getKeyCode(), is(KeyCodes.KEY_ESCAPE));

      // Alt+Y should be the other
      Set<Keys> secondShortcutKeys = shortcutList.get(1).getAllKeys();
      String secondShortcut = "second shortcut should be Alt+Y with no aliases";
      assertThat(secondShortcut, secondShortcutKeys.size(), is(1));
      Keys secondShortcutFirstKeys = secondShortcutKeys.iterator().next();
      assertThat(secondShortcut, secondShortcutFirstKeys.getModifiers(), is(Keys.ALT_KEY));
      assertThat(secondShortcut, secondShortcutFirstKeys.getKeyCode(), is((int) 'Y'));
   }

}

