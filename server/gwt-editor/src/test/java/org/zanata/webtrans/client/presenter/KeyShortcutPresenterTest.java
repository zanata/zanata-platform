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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.AttentionModeActivationEvent;
import org.zanata.webtrans.client.keys.EventWrapper;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.TimedAction;
import org.zanata.webtrans.client.keys.Timer;
import org.zanata.webtrans.client.keys.TimerFactory;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.view.KeyShortcutDisplay;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.view.client.ListDataProvider;

/**
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public class KeyShortcutPresenterTest {
    static final String TEST_MESSAGE_CLOSE_SHORTCUT_VIEW =
            "close shortcut view";
    static final String TEST_MESSAGE_SHOW_AVAILABLE_KEY_SHORTCUTS =
            "show available key shortcuts";

    // These could be any ints for the purpose of this test, as long as key
    // codes don't over
    static final int KEY_DOWN_TYPE = 0x80;
    static final int KEY_UP_TYPE = 0x200;

    static final int KEY_CODE_X = 'X';
    static final int KEY_CODE_Y = 'Y';
    static final String TEST_MESSAGE_APPLICATION_SCOPE = "Application";

    // object under test
    KeyShortcutPresenter keyShortcutPresenter;

    @Mock
    private KeyShortcutDisplay mockDisplay;
    @Mock
    private EventWrapper mockEventWrapper;
    @Mock
    private EventBus mockEventBus;
    @Mock
    private TimerFactory mockTimerFactory;
    @Mock
    private WebTransMessages mockMessages;

    @Captor
    private ArgumentCaptor<NativePreviewHandler> capturedNativePreviewHandler;
    @Mock
    private HandlerRegistration handlerRegistration;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        keyShortcutPresenter =
                new KeyShortcutPresenter(mockDisplay, mockEventBus,
                        mockMessages, mockEventWrapper, mockTimerFactory);

        when(mockMessages.closeShortcutView()).thenReturn(
                TEST_MESSAGE_CLOSE_SHORTCUT_VIEW);
        when(mockMessages.showAvailableKeyShortcuts()).thenReturn(
                TEST_MESSAGE_SHOW_AVAILABLE_KEY_SHORTCUTS);
        when(mockMessages.applicationScope()).thenReturn(
                TEST_MESSAGE_APPLICATION_SCOPE);
        when(
                mockEventWrapper
                        .addNativePreviewHandler(capturedNativePreviewHandler
                                .capture())).thenReturn(handlerRegistration);
        when(mockEventWrapper.keyDownEvent()).thenReturn(KEY_DOWN_TYPE);
        when(mockEventWrapper.keyUpEvent()).thenReturn(KEY_UP_TYPE);
    }

    @Test
    public void testExpectedActionsOnBind() {
        keyShortcutPresenter.bind();

        verify(mockEventWrapper).addNativePreviewHandler(
                capturedNativePreviewHandler.capture());
    }

    @Test
    public void displaysRegisteredShortcutsInOrder() {
        List<KeyShortcut> shortcutList = mockShortcutDisplayListInteractions();
        mockDisplay.showPanel();

        keyShortcutPresenter.bind();
        keyShortcutPresenter.showShortcuts();

        // Shortcut list should contain Alt+Y and Esc shortcuts
        assertThat(shortcutList.size()).isEqualTo(3)
                .as("KeyShortcutPresenter should register 2 global shortcuts");

        // esc should be first as it has no modifiers
        Set<Keys> firstShortcutKeys = shortcutList.get(0).getAllKeys();
        String firstShortcut = "first shortcut should be Esc with no modifiers";
        assertThat(firstShortcutKeys.size()).isEqualTo(1).as(firstShortcut);
        Keys firstShortcutFirstKeys = firstShortcutKeys.iterator().next();
        assertThat(firstShortcutFirstKeys.getModifiers()).isEqualTo(0)
                .as(firstShortcut);
        assertThat(firstShortcutFirstKeys.getKeyCode())
                .isEqualTo(KeyCodes.KEY_ESCAPE).as(firstShortcut);

        // Alt+X should be next
        Set<Keys> secondShortcutKeys = shortcutList.get(1).getAllKeys();
        String secondShortcut = "second shortcut should be Alt+X";
        assertThat(secondShortcutKeys.size()).isEqualTo(1).as(secondShortcut);
        Keys secondShortcutFirstKeys = secondShortcutKeys.iterator().next();
        assertThat(secondShortcutFirstKeys.getModifiers())
                .isEqualTo(Keys.ALT_KEY).as(secondShortcut);
        assertThat(secondShortcutFirstKeys.getKeyCode()).isEqualTo((int) 'X')
                .as(secondShortcut);

        // Alt+Y should be last
        Set<Keys> thirdShortcutKeys = shortcutList.get(2).getAllKeys();
        String thirdShortcut = "third shortcut should be Alt+Y";
        assertThat(thirdShortcutKeys.size()).isEqualTo(1).as(thirdShortcut);
        Keys thirdShortcutFirstKeys = thirdShortcutKeys.iterator().next();
        assertThat(thirdShortcutFirstKeys.getModifiers())
                .isEqualTo(Keys.ALT_KEY).as(thirdShortcut);
        assertThat(thirdShortcutFirstKeys.getKeyCode()).isEqualTo((int) 'Y')
                .as(thirdShortcut);
    }

    @Test
    public void testRespondsToAltY() {
        NativePreviewEvent altYPressed =
                buildNativeKeyDownEvent(new Keys(Keys.ALT_KEY, KEY_CODE_Y));
        mockShortcutDisplayListInteractions();
        keyShortcutPresenter.bind();
        capturedNativePreviewHandler.getValue().onPreviewNativeEvent(
                altYPressed);
        verify(mockDisplay).showPanel();
    }

    @Test
    public void testAttentionModeTimesOut() {
        NativePreviewEvent altXPressed =
                buildNativeKeyDownEvent(new Keys(Keys.ALT_KEY, KEY_CODE_X));
        mockShortcutDisplayListInteractions();
        ArgumentCaptor<AttentionModeActivationEvent> eventBusCapture =
                ArgumentCaptor.forClass(AttentionModeActivationEvent.class);

        Timer mockTimer = mock(Timer.class);
        // capture timer
        ArgumentCaptor<TimedAction> capturedTimedAction =
                ArgumentCaptor.forClass(TimedAction.class);
        when(mockTimerFactory.create(capturedTimedAction.capture()))
                .thenReturn(mockTimer);

        keyShortcutPresenter.bind();
        capturedNativePreviewHandler.getValue().onPreviewNativeEvent(
                altXPressed);
        verify(mockTimer).schedule(5000);
        capturedTimedAction.getValue().run();
        verify(mockEventBus, times(2)).fireEvent(eventBusCapture.capture());
        assertThat(eventBusCapture.getAllValues().get(0).isActive()).isTrue();
        assertThat(eventBusCapture.getAllValues().get(1).isActive()).isFalse();
        assertThat(eventBusCapture.getAllValues().size()).isEqualTo(2);
    }

    private NativePreviewEvent buildNativeKeyDownEvent(Keys keys) {
        NativePreviewEvent mockNativePreviewEvent =
                mock(NativePreviewEvent.class);
        NativeEvent mockNativeEvent = mock(NativeEvent.class);
        when(mockEventWrapper.getTypeInt(mockNativePreviewEvent)).thenReturn(
                KEY_DOWN_TYPE);
        when(mockNativePreviewEvent.getNativeEvent()).thenReturn(
                mockNativeEvent);
        when(mockEventWrapper.createKeys(mockNativeEvent)).thenReturn(keys);
        when(mockEventWrapper.getType(mockNativeEvent)).thenReturn(
                KeyShortcut.KeyEvent.KEY_DOWN.nativeEventType);
        return mockNativePreviewEvent;
    }

    private List<KeyShortcut> mockShortcutDisplayListInteractions() {
        @SuppressWarnings("unchecked")
        ListDataProvider<KeyShortcut> mockDataProvider =
                mock(ListDataProvider.class);
        List<KeyShortcut> shortcutList = new ArrayList<KeyShortcut>();
        when(mockDataProvider.getList()).thenReturn(shortcutList);
        mockDisplay.clearPanel();
        when(mockDisplay.addContext(TEST_MESSAGE_APPLICATION_SCOPE))
                .thenReturn(mockDataProvider);
        return shortcutList;
    }

}
