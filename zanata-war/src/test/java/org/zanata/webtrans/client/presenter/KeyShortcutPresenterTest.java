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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.keys.EventWrapper;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.view.client.ListDataProvider;


/**
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class KeyShortcutPresenterTest extends PresenterTest
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


   Display mockDisplay;
   EventWrapper mockEventWrapper;
   EventBus mockEventBus;
   WebTransMessages mockMessages;

   Capture<NativePreviewHandler> capturedNativePreviewHandler;


   @BeforeClass
   public void createMocksAndCaptures()
   {
      createMocks();
      createCaptures();
   }

   protected void createMocks()
   {
      mockDisplay = createAndAddMock(Display.class);
      mockEventWrapper = createAndAddMock(EventWrapper.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
   }

   protected void createCaptures()
   {
      capturedNativePreviewHandler = addCapture(new Capture<NativePreviewHandler>());
   }

   @BeforeMethod
   public void beforeMethod()
   {
      resetAll();
      keyShortcutPresenter = new KeyShortcutPresenter(mockDisplay, mockEventBus, mockMessages, mockEventWrapper);
   }

   public void testExpectedActionsOnBind()
   {
      replayAllMocks();
      keyShortcutPresenter.bind();
      verifyAllMocks();
   }

   public void displaysRegisteredShortcutsInOrder()
   {
      @SuppressWarnings("unchecked")
      ListDataProvider<KeyShortcut> mockDataProvider = createMock(ListDataProvider.class);
      List<KeyShortcut> shortcutList = expectShowDefaultShortcuts(mockDataProvider);

      replay(mockDataProvider);
      replayAllMocks();

      keyShortcutPresenter.bind();
      keyShortcutPresenter.showShortcuts();

      verifyAllMocks();
      verify(mockDataProvider);

      assertDefaultShortcutList(shortcutList);
   }

   public void testRespondsToAltY()
   {
      NativePreviewEvent mockNativePreviewEvent = createMock(NativePreviewEvent.class);
      NativeEvent mockNativeEvent = createMock(NativeEvent.class);

      expect(mockEventWrapper.getTypeInt(mockNativePreviewEvent)).andReturn(KEY_DOWN_TYPE).once();
      expect(mockNativePreviewEvent.getNativeEvent()).andReturn(mockNativeEvent).anyTimes();
      expect(mockEventWrapper.createKeys(mockNativeEvent)).andReturn(new Keys(Keys.ALT_KEY, KEY_CODE_Y)).once();
      expect(mockEventWrapper.getType(mockNativeEvent)).andReturn(KeyShortcut.KeyEvent.KEY_DOWN.nativeEventType).once();

      @SuppressWarnings("unchecked")
      ListDataProvider<KeyShortcut> mockDataProvider = createMock(ListDataProvider.class);
      List<KeyShortcut> shortcutList = expectShowDefaultShortcuts(mockDataProvider);

      replay(mockNativePreviewEvent, mockNativeEvent, mockDataProvider);
      replayAllMocks();

      keyShortcutPresenter.bind();
      capturedNativePreviewHandler.getValue().onPreviewNativeEvent(mockNativePreviewEvent);

      verifyAllMocks();
      verify(mockNativePreviewEvent, mockNativeEvent, mockDataProvider);

      assertDefaultShortcutList(shortcutList);
   }

   private List<KeyShortcut> expectShowDefaultShortcuts(ListDataProvider<KeyShortcut> mockDataProvider)
   {
      List<KeyShortcut> shortcutList = new ArrayList<KeyShortcut>();
      expect(mockDataProvider.getList()).andReturn(shortcutList).anyTimes();
      mockDisplay.clearPanel();
      expect(mockDisplay.addContext(TEST_MESSAGE_APPLICATION_SCOPE)).andReturn(mockDataProvider);
      mockDisplay.showPanel();
      return shortcutList;
   }

   /**
    * Checks that a list is the default list of 2 shortcuts to show and hide the shortcut summary.
    * 
    * @param shortcutList list to check
    */
   private void assertDefaultShortcutList(List<KeyShortcut> shortcutList)
   {
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

   @Override
   protected void setDefaultBindExpectations()
   {
      expectUiMessages();
      expect(mockEventWrapper.addNativePreviewHandler(capture(capturedNativePreviewHandler))).andReturn(mockHandlerRegistration()).once();
      expect(mockEventWrapper.keyDownEvent()).andReturn(KEY_DOWN_TYPE).anyTimes();
      expect(mockEventWrapper.keyUpEvent()).andReturn(KEY_UP_TYPE).anyTimes();
   }

   private void expectUiMessages()
   {
      expect(mockMessages.closeShortcutView()).andReturn(TEST_MESSAGE_CLOSE_SHORTCUT_VIEW).anyTimes();
      expect(mockMessages.showAvailableKeyShortcuts()).andReturn(TEST_MESSAGE_SHOW_AVAILABLE_KEY_SHORTCUTS).anyTimes();
      expect(mockMessages.applicationScope()).andReturn(TEST_MESSAGE_APPLICATION_SCOPE).anyTimes();
   }

}

