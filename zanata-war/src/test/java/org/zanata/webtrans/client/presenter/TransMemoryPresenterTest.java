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
import org.zanata.webtrans.client.events.TransMemoryShorcutCopyHandler;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.TransMemoryPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.ListDataProvider;

@Test(groups = { "unit-tests" })
public class TransMemoryPresenterTest extends PresenterTest
{
   // object under test
   TransMemoryPresenter transMemoryPresenter;

   // injected mocks
   Display mockDisplay;
   EventBus mockEventBus;
   Identity mockIdentity;
   UserWorkspaceContext mockUserWorkspaceContext;
   CachingDispatchAsync mockDispatcher;
   TransMemoryDetailsPresenter mockTransMemoryDetailsPresenter;
   WebTransMessages mockMessages;
   KeyShortcutPresenter mockKeyShortcutPresenter;
   TransMemoryMergePresenter mockTransMemoryMergePresenter;

   HasAllFocusHandlers mockFocusTextBox;
   HasValue<SearchType> mockSearchType;
   HasClickHandlers mockSearchButton;
   HasClickHandlers mockClearButton;

   private Capture<TransUnitSelectionHandler> capturedTransUnitSelectionEventHandler;
   private Capture<FocusHandler> capturedFocusHandler;
   private Capture<BlurHandler> capturedBlurHandler;
   private Capture<ClickHandler> capturedClearButtonClickHandler;
   private Capture<ClickHandler> capturedSearchButtonClickHandler;
   private Capture<TransMemoryShorcutCopyHandler> capturedTransMemoryShortcutCopyEventHandler;
   private Capture<KeyShortcut> capturedKeyShortcuts;

   @BeforeClass
   public void createMocks()
   {
      mockDisplay = createAndAddMock(Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockIdentity = createAndAddMock(Identity.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);
      mockDispatcher = createAndAddMock(CachingDispatchAsync.class);
      mockTransMemoryDetailsPresenter = createAndAddMock(TransMemoryDetailsPresenter.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
      mockKeyShortcutPresenter = createAndAddMock(KeyShortcutPresenter.class);
      mockTransMemoryMergePresenter = createAndAddMock(TransMemoryMergePresenter.class);

      mockFocusTextBox = createAndAddMock(HasAllFocusHandlers.class);
      mockClearButton = createAndAddMock(HasClickHandlers.class);
      mockSearchType = createAndAddMock(HasValue.class);
      mockSearchButton = createAndAddMock(HasClickHandlers.class);

      capturedTransUnitSelectionEventHandler = addCapture(new Capture<TransUnitSelectionHandler>());
      capturedFocusHandler = addCapture(new Capture<FocusHandler>());
      capturedBlurHandler = addCapture(new Capture<BlurHandler>());
      capturedClearButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedSearchButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedTransMemoryShortcutCopyEventHandler = addCapture(new Capture<TransMemoryShorcutCopyHandler>());
      capturedKeyShortcuts = addCapture(new Capture<KeyShortcut>());
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
      transMemoryPresenter = new TransMemoryPresenter(mockDisplay, mockEventBus, mockDispatcher, mockMessages, mockTransMemoryDetailsPresenter, mockUserWorkspaceContext, mockTransMemoryMergePresenter, mockKeyShortcutPresenter);
      transMemoryPresenter.bind();
      verifyAllMocks();
   }

   @Override
   protected void setDefaultBindExpectations()
   {
      expect(mockDisplay.getSearchType()).andReturn(mockSearchType).anyTimes();

      mockSearchType.setValue(SearchType.FUZZY);
      expectLastCall().once();

      expect(mockMessages.searchTM()).andReturn("Search TM");
      expect(mockKeyShortcutPresenter.register(and(capture(capturedKeyShortcuts), isA(KeyShortcut.class)))).andReturn(null).once();

      expect(mockDisplay.getSearchButton()).andReturn(mockSearchButton).anyTimes();
      expect(mockSearchButton.addClickHandler(capture(capturedSearchButtonClickHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getClearButton()).andReturn(mockClearButton).anyTimes();
      expect(mockClearButton.addClickHandler(capture(capturedClearButtonClickHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getFocusTmTextBox()).andReturn(mockFocusTextBox).times(2);
      expect(mockFocusTextBox.addFocusHandler(capture(capturedFocusHandler))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockFocusTextBox.addBlurHandler(capture(capturedBlurHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockEventBus.addHandler(eq(TransUnitSelectionEvent.getType()), and(capture(capturedTransUnitSelectionEventHandler), isA(TransUnitSelectionHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEventBus.addHandler(eq(TransMemoryShortcutCopyEvent.getType()), and(capture(capturedTransMemoryShortcutCopyEventHandler), isA(TransMemoryShorcutCopyHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getMergeButton()).andReturn(createMock(HasClickHandlers.class)).once();
      
      mockDisplay.setListener(isA(HasTMEvent.class));
      expectLastCall().once();
   }
}
