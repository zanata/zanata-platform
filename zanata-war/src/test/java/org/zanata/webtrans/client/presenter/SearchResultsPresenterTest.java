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

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.history.Window.Location;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class SearchResultsPresenterTest
{

   private static final int TOTAL_KEY_SHORTCUTS = 5;

   private static final String TEST_MESSAGE_NO_TEXT_FLOWS_SELECTED = "No text flows selected";
   private static final String TEST_MESSAGE_SELECT_ALL_TEXT_FLOWS_KEY_SHORTCUT = "Select all text flows";
   private static final String TEST_MESSAGE_FOCUS_SEARCH_PHRASE_KEY_SHORTCUT = "Focus search phrase";
   private static final String TEST_MESSAGE_FOCUS_REPLACEMENT_PHRASE_KEY_SHORTCUT = "Focus replacement phrase";
   private static final String TEST_MESSAGE_REPLACE_SELECTED_KEY_SHORTCUT = "Replace selected text flows";
   private static final String TEST_MESSAGE_TOGGLE_ROW_ACTION_BUTTONS = "Toggle row action buttons";


   //object under test
   SearchResultsPresenter searchResultsPresenter;


   ArrayList<Object> createdMocks;
   Object[] allMocks;

   CachingDispatchAsync mockDispatcher;
   Display mockDisplay;
   EventBus mockEventBus;
   History mockHistory;
   KeyShortcutPresenter mockKeyShortcutPresenter;
   WebTransMessages mockMessages;
   Location mockWindowLocation;
   WorkspaceContext mockWorkspaceContext;

   HasClickHandlers mockReplaceAllButton;
   HasValue<String> mockReplacementTextBox;
   HasValue<Boolean> mockRequirePreviewChk;
   HasClickHandlers mockSearchButton;
   HasClickHandlers mockSelectAllButton;
   HasValue<Boolean> mockSelectAllChk;
   HasText mockSelectionInfoLabel;


   Capture<ValueChangeHandler<String>> capturedHistoryValueChangeHandler;
   Capture<ClickHandler> capturedReplaceAllButtonClickHandler;
   Capture<ValueChangeHandler<String>> capturedReplacementTextBoxValueChangeHandler;
   Capture<ValueChangeHandler<Boolean>> capturedRequirePreviewChkValueChangeHandler;
   Capture<ClickHandler> capturedSearchButtonClickHandler;
   Capture<ClickHandler> capturedSelectAllButtonClickHandler;
   Capture<ValueChangeHandler<Boolean>> capturedSelectAllChkValueChangeHandler;

   Capture<TransUnitUpdatedEventHandler> capturedTransUnitUpdatedEventHandler;
   Capture<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdatedEventHandler;
   private Capture<KeyShortcut> capturedKeyShortcuts;



   @BeforeClass
   public void createMocks()
   {
      createAllMocks();
      createAllCaptures();
   }

   @SuppressWarnings("unchecked")
   private void createAllMocks()
   {
      createdMocks = new ArrayList<Object>();

      mockDispatcher = createAndAddMock(CachingDispatchAsync.class);
      mockDisplay = createAndAddMock(Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockHistory = createAndAddMock(History.class);
      mockKeyShortcutPresenter = createAndAddMock(KeyShortcutPresenter.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
      mockWindowLocation = createAndAddMock(Window.Location.class);
      mockWorkspaceContext = createAndAddMock(WorkspaceContext.class);

      mockReplaceAllButton = createAndAddMock(HasClickHandlers.class);
      mockReplacementTextBox = createAndAddMock(HasValue.class);
      mockRequirePreviewChk = createAndAddMock(HasValue.class);
      mockSearchButton = createAndAddMock(HasClickHandlers.class);
      mockSelectAllButton = createAndAddMock(HasClickHandlers.class);
      mockSelectAllChk = createAndAddMock(HasValue.class);
      mockSelectionInfoLabel = createAndAddMock(HasText.class);

      allMocks = createdMocks.toArray();
   }

   private void createAllCaptures()
   {
      capturedHistoryValueChangeHandler = new Capture<ValueChangeHandler<String>>();
      capturedReplaceAllButtonClickHandler = new Capture<ClickHandler>();
      capturedReplacementTextBoxValueChangeHandler = new Capture<ValueChangeHandler<String>>();
      capturedRequirePreviewChkValueChangeHandler = new Capture<ValueChangeHandler<Boolean>>();
      capturedSearchButtonClickHandler = new Capture<ClickHandler>();
      capturedSelectAllButtonClickHandler = new Capture<ClickHandler>();
      capturedSelectAllChkValueChangeHandler = new Capture<ValueChangeHandler<Boolean>>();

      capturedTransUnitUpdatedEventHandler = new Capture<TransUnitUpdatedEventHandler>();
      capturedWorkspaceContextUpdatedEventHandler = new Capture<WorkspaceContextUpdateEventHandler>();

      capturedKeyShortcuts = new Capture<KeyShortcut>();
   }

   private <T> T createAndAddMock(Class<T> clazz)
   {
      T mock = createMock(clazz);
      createdMocks.add(mock);
      return mock;
   }

   @BeforeMethod
   public void beforeMethod()
   {
      resetAllMocks();
      resetAllCaptures();

      setupDefaultMockExpectations();

      searchResultsPresenter = new SearchResultsPresenter(mockDisplay, mockEventBus,
            mockDispatcher, mockHistory, mockMessages, mockWorkspaceContext,
            mockKeyShortcutPresenter, mockWindowLocation);

   }

   public void testExpectedActionsOnBind()
   {
      replayAllMocks();
      searchResultsPresenter.bind();
      verifyAllMocks();
   }

   private void setupDefaultMockExpectations()
   {
      boolean workspaceIsReadOnly = false;

      expectUiMessages();

      expect(mockWorkspaceContext.isReadOnly()).andReturn(workspaceIsReadOnly).anyTimes();

      expectDisplayComponentGetters();

      mockSelectionInfoLabel.setText(TEST_MESSAGE_NO_TEXT_FLOWS_SELECTED);
      expectLastCall().once();

      mockDisplay.setReplaceAllButtonEnabled(false);
      expectLastCall().once();

      mockDisplay.setReplaceAllButtonVisible(!workspaceIsReadOnly);
      expectLastCall().once();

      expectHandlerRegistrations();

      expect(mockKeyShortcutPresenter.registerKeyShortcut(capture(capturedKeyShortcuts))).andReturn(createMock(HandlerRegistration.class)).times(TOTAL_KEY_SHORTCUTS);
   }

   private void expectUiMessages()
   {
      expect(mockMessages.noTextFlowsSelected()).andReturn(TEST_MESSAGE_NO_TEXT_FLOWS_SELECTED).anyTimes();
      expect(mockMessages.selectAllTextFlowsKeyShortcut()).andReturn(TEST_MESSAGE_SELECT_ALL_TEXT_FLOWS_KEY_SHORTCUT).anyTimes();
      expect(mockMessages.focusSearchPhraseKeyShortcut()).andReturn(TEST_MESSAGE_FOCUS_SEARCH_PHRASE_KEY_SHORTCUT).anyTimes();
      expect(mockMessages.focusReplacementPhraseKeyShortcut()).andReturn(TEST_MESSAGE_FOCUS_REPLACEMENT_PHRASE_KEY_SHORTCUT).anyTimes();
      expect(mockMessages.replaceSelectedKeyShortcut()).andReturn(TEST_MESSAGE_REPLACE_SELECTED_KEY_SHORTCUT).anyTimes();
      expect(mockMessages.toggleRowActionButtons()).andReturn(TEST_MESSAGE_TOGGLE_ROW_ACTION_BUTTONS).anyTimes();
   }

   private void expectDisplayComponentGetters()
   {
      expect(mockDisplay.getSelectionInfoLabel()).andReturn(mockSelectionInfoLabel).anyTimes();
      expect(mockDisplay.getSearchButton()).andReturn(mockSearchButton).anyTimes();
      expect(mockDisplay.getReplacementTextBox()).andReturn(mockReplacementTextBox).anyTimes();
      expect(mockDisplay.getSelectAllChk()).andReturn(mockSelectAllChk).anyTimes();
      expect(mockDisplay.getRequirePreviewChk()).andReturn(mockRequirePreviewChk).anyTimes();
      expect(mockDisplay.getReplaceAllButton()).andReturn(mockReplaceAllButton).anyTimes();
      expect(mockDisplay.getSelectAllButton()).andReturn(mockSelectAllButton).anyTimes();
   }

   private void expectHandlerRegistrations()
   {
      expectClickHandlerRegistration(mockSearchButton, capturedSearchButtonClickHandler);
      expectValueChangeHandlerRegistration(mockReplacementTextBox, capturedReplacementTextBoxValueChangeHandler);
      expectValueChangeHandlerRegistration(mockSelectAllChk, capturedSelectAllChkValueChangeHandler);
      expectValueChangeHandlerRegistration(mockRequirePreviewChk, capturedRequirePreviewChkValueChangeHandler);
      expectClickHandlerRegistration(mockReplaceAllButton, capturedReplaceAllButtonClickHandler);
      expectClickHandlerRegistration(mockSelectAllButton, capturedSelectAllButtonClickHandler);

      expect(mockHistory.addValueChangeHandler(capture(capturedHistoryValueChangeHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expectEventHandlerRegistration(TransUnitUpdatedEvent.getType(), TransUnitUpdatedEventHandler.class, capturedTransUnitUpdatedEventHandler);
      expectEventHandlerRegistration(WorkspaceContextUpdateEvent.getType(), WorkspaceContextUpdateEventHandler.class, capturedWorkspaceContextUpdatedEventHandler);
   }

   // copied from AppPresenterTest
   /**
    * Expect a single handler registration on a mock object, and capture the
    * click handler in the given {@link Capture}
    * 
    * @param mockObjectToClick
    * @param captureForHandler
    */
   private void expectClickHandlerRegistration(HasClickHandlers mockObjectToClick, Capture<ClickHandler> captureForHandler)
   {
      expect(mockObjectToClick.addClickHandler(and(capture(captureForHandler), isA(ClickHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private <T> void expectValueChangeHandlerRegistration(HasValue<T> mockObjectWithValue, Capture<ValueChangeHandler<T>> captureForHandler)
   {
      expect(mockObjectWithValue.addValueChangeHandler(capture(captureForHandler))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   //copied from AppPresenterTest
   private <H extends EventHandler> void expectEventHandlerRegistration(Type<H> expectedType, Class<H> expectedClass, Capture<H> handlerCapture)
   {
      expect(mockEventBus.addHandler(eq(expectedType), and(capture(handlerCapture), isA(expectedClass)))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private void resetAllCaptures()
   {
      capturedHistoryValueChangeHandler.reset();
      capturedReplaceAllButtonClickHandler.reset();
      capturedReplacementTextBoxValueChangeHandler.reset();
      capturedRequirePreviewChkValueChangeHandler.reset();
      capturedSearchButtonClickHandler.reset();
      capturedSelectAllButtonClickHandler.reset();
      capturedSelectAllChkValueChangeHandler.reset();

      capturedTransUnitUpdatedEventHandler.reset();
      capturedWorkspaceContextUpdatedEventHandler.reset();

      capturedKeyShortcuts.reset();
   }

   private void resetAllMocks()
   {
      reset(allMocks);
   }

   private void replayAllMocks()
   {
      replay(allMocks);
   }

   private void verifyAllMocks()
   {
      verify(allMocks);
   }
}
