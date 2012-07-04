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
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.TransMemoryShorcutCopyHandler;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.presenter.TransMemoryPresenter.Display;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.ListDataProvider;


@Test(groups = { "unit-tests" })
public class TransMemoryPresenterTest
{
   // object under test
   TransMemoryPresenter transMemoryPresenter;

   //injected mocks
   Display mockDisplay = createMock(Display.class);
   EventBus mockEventBus = createMock(EventBus.class);
   CachingDispatchAsync mockDispatcher = createMock(CachingDispatchAsync.class);
   TransMemoryDetailsPresenter mockTransMemoryDetailsPresenter = createMock(TransMemoryDetailsPresenter.class);
   WorkspaceContext mockWorkspaceContext = createMock(WorkspaceContext.class);

   @SuppressWarnings("unchecked")
   HasValue<SearchType> mockSearchType = createMock(HasValue.class);
   HasClickHandlers mockSearchButton = createMock(HasClickHandlers.class);
   @SuppressWarnings("unchecked")
   Column<TransMemoryResultItem, ImageResource> mockDetailsColumn = createMock(Column.class);
   @SuppressWarnings("unchecked")
   Column<TransMemoryResultItem, String> mockCopyColumn = createMock(Column.class);

   Capture<TransUnitSelectionHandler> capturedTransUnitSelectionEventHandler = new Capture<TransUnitSelectionHandler>();
   Capture<ClickHandler> capturedClearButtonClickHandler = new Capture<ClickHandler>();
   Capture<ClickHandler> capturedSearchButtonClickHandler = new Capture<ClickHandler>();
   Capture<TransMemoryShorcutCopyHandler> capturedTransMemoryShortcutCopyEventHandler = new Capture<TransMemoryShorcutCopyHandler>();

   HasClickHandlers mockClearButton = createMock(HasClickHandlers.class);
   TransMemoryMergePresenter mockTransMemoryMergePresenter = createMock(TransMemoryMergePresenter.class);


   @BeforeMethod
   public void resetMocks()
   {
      reset(mockDispatcher, mockDisplay, mockEventBus, mockTransMemoryDetailsPresenter, mockWorkspaceContext);
      reset(mockSearchType, mockSearchButton, mockClearButton);
      reset(mockDetailsColumn, mockCopyColumn);

      capturedTransUnitSelectionEventHandler.reset();
      capturedSearchButtonClickHandler.reset();
      capturedTransMemoryShortcutCopyEventHandler.reset();
   }

   @SuppressWarnings("unchecked")
   public void canBind()
   {
      expect(mockDisplay.getSearchType()).andReturn(mockSearchType).anyTimes();

      mockSearchType.setValue(SearchType.FUZZY);
      expectLastCall().once();

      expect(mockDisplay.getSearchButton()).andReturn(mockSearchButton).anyTimes();
      expect(mockSearchButton.addClickHandler(capture(capturedSearchButtonClickHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getClearButton()).andReturn(mockClearButton).anyTimes();
      expect(mockClearButton.addClickHandler(capture(capturedClearButtonClickHandler))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockEventBus.addHandler(eq(TransUnitSelectionEvent.getType()), and(capture(capturedTransUnitSelectionEventHandler), isA(TransUnitSelectionHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEventBus.addHandler(eq(TransMemoryShortcutCopyEvent.getType()), and(capture(capturedTransMemoryShortcutCopyEventHandler ), isA(TransMemoryShorcutCopyHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getDetailsColumn()).andReturn(mockDetailsColumn).anyTimes();

      expect(mockDisplay.getMergeButton()).andReturn(createMock(HasClickHandlers.class)).once();

      //TODO capture field updaters and data provider to check them?

      mockDetailsColumn.setFieldUpdater(isA(FieldUpdater.class));
      expectLastCall().once();

      expect(mockDisplay.getCopyColumn()).andReturn(mockCopyColumn).anyTimes();

      mockCopyColumn.setFieldUpdater(isA(FieldUpdater.class));
      expectLastCall().once();

      //multiple calls may be bad (rendering issues?). May want to inject into
      //view and presenter instead.
      mockDisplay.setDataProvider(isA(ListDataProvider.class));
      expectLastCall().once();

      replay(mockDispatcher, mockDisplay, mockEventBus, mockTransMemoryDetailsPresenter, mockWorkspaceContext);
      replay(mockSearchType, mockSearchButton, mockClearButton);
      replay(mockDetailsColumn, mockCopyColumn);

      transMemoryPresenter = new TransMemoryPresenter(mockDisplay, mockEventBus, mockDispatcher, mockTransMemoryDetailsPresenter , mockWorkspaceContext, mockTransMemoryMergePresenter);

      transMemoryPresenter.bind();

      verify(mockDispatcher, mockDisplay, mockEventBus, mockTransMemoryDetailsPresenter, mockWorkspaceContext);
      verify(mockSearchType, mockSearchButton, mockClearButton);
      verify(mockDetailsColumn, mockCopyColumn);
   }
}
