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
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.GlossaryPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.ListDataProvider;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class GlossaryPresenterTest extends PresenterTest
{
   // object under test
   private GlossaryPresenter glossaryPresenter;

   private Display mockDisplay;
   private EventBus mockEventBus;
   private CachingDispatchAsync mockDispatcher;
   private WebTransMessages mockMessages;
   
   private GlossaryDetailsPresenter mockGlossaryDetailsPresenter;
   private UserWorkspaceContext mockUserWorkspaceContext;
   private KeyShortcutPresenter mockKeyShortcutPresenter;
   
   HasAllFocusHandlers mockFocusTextBox;
   HasClickHandlers mockSearchButton;
   Column<GlossaryResultItem, ImageResource> mockDetailsColumn;
   Column<GlossaryResultItem, String> mockCopyColumn;

   private Capture<ClickHandler> capturedSearchButtonClickHandler;
   private Capture<KeyShortcut> capturedKeyShortcuts;
   private Capture<TransUnitSelectionHandler> capturedTransUnitSelectionEventHandler;
   private Capture<FocusHandler> capturedFocusHandler;
   private Capture<BlurHandler> capturedBlurHandler;

   @BeforeClass
   public void createMocks()
   {
      mockDisplay = createAndAddMock(GlossaryPresenter.Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockDispatcher = createAndAddMock(CachingDispatchAsync.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
      mockGlossaryDetailsPresenter = createAndAddMock(GlossaryDetailsPresenter.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);
      mockKeyShortcutPresenter = createAndAddMock(KeyShortcutPresenter.class);

      mockFocusTextBox = createAndAddMock(HasAllFocusHandlers.class);
      mockSearchButton = createAndAddMock(HasClickHandlers.class);
      mockDetailsColumn = createAndAddMock(Column.class);
      mockCopyColumn = createAndAddMock(Column.class);

      capturedSearchButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedKeyShortcuts = addCapture(new Capture<KeyShortcut>());
      capturedTransUnitSelectionEventHandler = addCapture(new Capture<TransUnitSelectionHandler>());
      capturedFocusHandler = addCapture(new Capture<FocusHandler>());
      capturedBlurHandler = addCapture(new Capture<BlurHandler>());

   }

   @BeforeMethod
   public void beforeMethod()
   {
      resetAll();
   }

   @Test
   public void canBind()
   {
      replayAllMocks();
      glossaryPresenter = new GlossaryPresenter(mockDisplay, mockEventBus, mockDispatcher, mockMessages, mockGlossaryDetailsPresenter, mockUserWorkspaceContext, mockKeyShortcutPresenter);
      glossaryPresenter.bind();
      verifyAllMocks();
   }

   @Override
   protected void setDefaultBindExpectations()
   {
      mockDisplay.setDataProvider(isA(ListDataProvider.class));
      expectLastCall().once();

      expect(mockDisplay.getSearchButton()).andReturn(mockSearchButton).anyTimes();
      expect(mockSearchButton.addClickHandler(capture(capturedSearchButtonClickHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockKeyShortcutPresenter.register(and(capture(capturedKeyShortcuts), isA(KeyShortcut.class)))).andReturn(null).once();
      expect(mockMessages.searchGlossary()).andReturn("Search glossary");

      expect(mockEventBus.addHandler(eq(TransUnitSelectionEvent.getType()), and(capture(capturedTransUnitSelectionEventHandler), isA(TransUnitSelectionHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getCopyColumn()).andReturn(mockCopyColumn).once();
      mockCopyColumn.setFieldUpdater(isA(FieldUpdater.class));
      expectLastCall().once();

      expect(mockDisplay.getDetailsColumn()).andReturn(mockDetailsColumn).once();
      mockDetailsColumn.setFieldUpdater(isA(FieldUpdater.class));
      expectLastCall().once();

      expect(mockDisplay.getFocusGlossaryTextBox()).andReturn(mockFocusTextBox).times(2);
      expect(mockFocusTextBox.addFocusHandler(capture(capturedFocusHandler))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockFocusTextBox.addBlurHandler(capture(capturedBlurHandler))).andReturn(createMock(HandlerRegistration.class)).once();

   }

}
