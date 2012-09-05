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
import static org.easymock.EasyMock.expectLastCall;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.presenter.GlossaryDetailsPresenter.Display;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class GlossaryDetailsPresenterTest extends PresenterTest
{
   // object under test
   private GlossaryDetailsPresenter glossaryDetailsPresenter;

   private Display mockDisplay;
   private EventBus mockEventBus;
   private CachingDispatchAsync mockDispatcher;
   private UiMessages mockMessages;
   private UserWorkspaceContext mockUserWorkspaceContext;
   
   private HasText mockTargetCommentText;

   HasClickHandlers mockDismissButton;
   HasClickHandlers mockSaveButton;
   HasClickHandlers mockAddNewCommentButton;

   HasChangeHandlers mockEntryListBox;

   private Capture<ClickHandler> capturedDismissButtonClickHandler;
   private Capture<ClickHandler> capturedSaveButtonClickHandler;
   private Capture<ClickHandler> capturedAddNewCommentButtonClickHandler;

   private Capture<ChangeHandler> capturedEntryListBoxChangeHandler;

   @BeforeClass
   public void createMocks()
   {
      mockDisplay = createAndAddMock(GlossaryDetailsPresenter.Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockDispatcher = createAndAddMock(CachingDispatchAsync.class);
      mockMessages = createAndAddMock(UiMessages.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);

      mockTargetCommentText = createAndAddMock(HasText.class);

      mockDismissButton = createAndAddMock(HasClickHandlers.class);
      mockSaveButton = createAndAddMock(HasClickHandlers.class);
      mockAddNewCommentButton = createAndAddMock(HasClickHandlers.class);

      mockEntryListBox = createAndAddMock(HasChangeHandlers.class);

      capturedDismissButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedSaveButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedAddNewCommentButtonClickHandler = addCapture(new Capture<ClickHandler>());

      capturedEntryListBoxChangeHandler = addCapture(new Capture<ChangeHandler>());
   }

   @BeforeMethod
   public void beforeMethod()
   {
      resetAll();
   }

   @Test
   public void canBind()
   {
      expect(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).andReturn(true).once();

      mockDisplay.setHasUpdateAccess(true);
      expectLastCall().once();

      replayAllMocks();
      glossaryDetailsPresenter = new GlossaryDetailsPresenter(mockDisplay, mockEventBus, mockMessages, mockDispatcher, mockUserWorkspaceContext);
      glossaryDetailsPresenter.bind();

      verifyAllMocks();
   }

   @Test
   public void testAddNewCommentWithUpdateAccess()
   {
      int rowNum = 1;
      String newComment = "new comment";
      boolean hasAccess = true;
      
      expect(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).andReturn(hasAccess).once();

      mockDisplay.setHasUpdateAccess(hasAccess);
      expectLastCall().once();

      expect(mockDisplay.getNewCommentText()).andReturn(mockTargetCommentText).times(3);
      expect(mockTargetCommentText.getText()).andReturn(newComment).times(2);

      expect(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).andReturn(true).once();

      expect(mockDisplay.getTargetCommentRowCount()).andReturn(rowNum);

      mockDisplay.addRowIntoTargetComment(rowNum, newComment);
      expectLastCall().once();
      
      mockTargetCommentText.setText("");
      expectLastCall().once();

      replayAllMocks();

      glossaryDetailsPresenter = new GlossaryDetailsPresenter(mockDisplay, mockEventBus, mockMessages, mockDispatcher, mockUserWorkspaceContext);
      glossaryDetailsPresenter.bind();

      ClickEvent clickEvent = createMock(ClickEvent.class);
      capturedAddNewCommentButtonClickHandler.getValue().onClick(clickEvent);

      verifyAllMocks();
   }

   @Test
   public void testAddNewCommentWithNoUpdateAccess()
   {
      String newComment = "new comment";
      boolean hasAccess = false;

      expect(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).andReturn(hasAccess).times(2);

      mockDisplay.setHasUpdateAccess(hasAccess);
      expectLastCall().once();

      expect(mockDisplay.getNewCommentText()).andReturn(mockTargetCommentText).once();
      expect(mockTargetCommentText.getText()).andReturn(newComment).once();

      replayAllMocks();

      glossaryDetailsPresenter = new GlossaryDetailsPresenter(mockDisplay, mockEventBus, mockMessages, mockDispatcher, mockUserWorkspaceContext);
      glossaryDetailsPresenter.bind();

      ClickEvent clickEvent = createMock(ClickEvent.class);
      capturedAddNewCommentButtonClickHandler.getValue().onClick(clickEvent);

      verifyAllMocks();
   }

   @Test
   public void testAddNewCommentWithEmptyString()
   {
      String newComment = "";
      boolean hasAccess = false;

      expect(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).andReturn(hasAccess).once();

      mockDisplay.setHasUpdateAccess(hasAccess);
      expectLastCall().once();

      expect(mockDisplay.getNewCommentText()).andReturn(mockTargetCommentText).once();
      expect(mockTargetCommentText.getText()).andReturn(newComment).once();

      replayAllMocks();

      glossaryDetailsPresenter = new GlossaryDetailsPresenter(mockDisplay, mockEventBus, mockMessages, mockDispatcher, mockUserWorkspaceContext);
      glossaryDetailsPresenter.bind();

      ClickEvent clickEvent = createMock(ClickEvent.class);
      capturedAddNewCommentButtonClickHandler.getValue().onClick(clickEvent);

      verifyAllMocks();
   }

   @Override
   protected void setDefaultBindExpectations()
   {
      expect(mockDisplay.getDismissButton()).andReturn(mockDismissButton).once();
      expect(mockDismissButton.addClickHandler(capture(capturedDismissButtonClickHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getSaveButton()).andReturn(mockSaveButton).once();
      expect(mockSaveButton.addClickHandler(capture(capturedSaveButtonClickHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getAddNewCommentButton()).andReturn(mockAddNewCommentButton).anyTimes();
      expect(mockAddNewCommentButton.addClickHandler(capture(capturedAddNewCommentButtonClickHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getEntryListBox()).andReturn(mockEntryListBox).once();
      expect(mockEntryListBox.addChangeHandler(capture(capturedEntryListBoxChangeHandler))).andReturn(createMock(HandlerRegistration.class)).once();
   }

}
