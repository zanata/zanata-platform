package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.Collections;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import net.customware.gwt.presenter.client.EventBus;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.WindowLocation;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.ListDataProvider;

public class DocumentListPresenterTest
{
   DocumentListPresenter dlp;

   @SuppressWarnings("rawtypes")
   ListDataProvider mockDataProvider;

   CachingDispatchAsync mockDispatcher;

   DocumentListPresenter.Display mockDisplay;

   @SuppressWarnings("rawtypes")
   HasSelectionHandlers mockDocList;

   EventBus mockEventBus;

   @SuppressWarnings("rawtypes")
   HasValue mockExactSearchCheckbox; // Boolean

   @SuppressWarnings("rawtypes")
   HasValue mockFilterTextbox; // String

   History mockHistory;

   WebTransMessages mockMessages;

   WindowLocation mockWindowLocation;

   WorkspaceContext mockWorkspaceContext;


   @BeforeClass
   public void setupMocks()
   {
      System.out.println("running before class");

      mockDataProvider = createMock(ListDataProvider.class);
      mockDispatcher = createMock(CachingDispatchAsync.class);
      mockDisplay = createMock(DocumentListPresenter.Display.class);
      mockDocList = createMock(HasSelectionHandlers.class);
      mockEventBus = createMock(EventBus.class);
      mockExactSearchCheckbox = createMock(HasValue.class);
      mockFilterTextbox = createMock(HasValue.class);
      mockHistory = createMock(History.class);
      mockMessages = createMock(WebTransMessages.class);
      mockWindowLocation = createMock(WindowLocation.class);
      mockWorkspaceContext = createMock(WorkspaceContext.class);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @BeforeMethod
   public void ConstructDocumentListPresenter()
   {
      System.out.println("running before");

      // TODO reset all
      reset(mockDataProvider);
      reset(mockDispatcher);
      reset(mockDisplay);
      reset(mockDocList);
      reset(mockEventBus);
      reset(mockExactSearchCheckbox);
      reset(mockFilterTextbox);
      reset(mockHistory);
      reset(mockMessages);
      reset(mockWindowLocation);
      reset(mockWorkspaceContext);

      // expect(mockEventBus.addHandler((GwtEvent.Type<DocumentSelectionHandler>)
      // notNull(), (DocumentSelectionHandler)
      // notNull())).andReturn(createMock(HandlerRegistration.class)).anyTimes();

      expect(mockDataProvider.getList()).andReturn(Collections.EMPTY_LIST).anyTimes();
      mockDataProvider.addDataDisplay(null);
      expectLastCall().once();

      mockDispatcher.execute((Action) notNull(), (AsyncCallback) notNull());
      expectLastCall().andAnswer(new DoclistSuccessAnswer());

      expect(mockDisplay.getDataProvider()).andReturn(mockDataProvider).anyTimes();
      expect(mockDisplay.getDocumentList()).andReturn(mockDocList).anyTimes();
      expect(mockDisplay.getFilterTextBox()).andReturn(mockFilterTextbox).anyTimes();
      expect(mockDisplay.getExactSearchCheckbox()).andReturn(mockExactSearchCheckbox).anyTimes();

      mockDisplay.setPageSize(0);
      expectLastCall().anyTimes();

      expect(mockDisplay.getDocumentListTable()).andReturn(null).anyTimes();

      // TODO may need to mock handler registrations here
      expect(mockDocList.addSelectionHandler((SelectionHandler) notNull())).andReturn(createMock(HandlerRegistration.class));
      expect(mockEventBus.addHandler((GwtEvent.Type<EventHandler>) notNull(), (EventHandler) notNull())).andReturn(createMock(HandlerRegistration.class)).anyTimes();
      mockEventBus.fireEvent((GwtEvent) notNull());
      expectLastCall().anyTimes();
      expect(mockFilterTextbox.addValueChangeHandler((ValueChangeHandler<String>) notNull())).andReturn(createMock(HandlerRegistration.class));
      expect(mockExactSearchCheckbox.addValueChangeHandler((ValueChangeHandler<Boolean>) notNull())).andReturn(createMock(HandlerRegistration.class));
      expect(mockHistory.addValueChangeHandler((ValueChangeHandler<String>) notNull())).andReturn(createMock(HandlerRegistration.class));
      mockHistory.fireCurrentHistoryState();
      expectLastCall().anyTimes();
      expect(mockWindowLocation.getParameterMap()).andReturn(Collections.EMPTY_MAP).anyTimes();
      expect(mockWorkspaceContext.getWorkspaceId()).andReturn(new WorkspaceId(new ProjectIterationId("mockProjectSlug", "mockIterationSlug"), new LocaleId("es"))).anyTimes();

      replay(mockDataProvider);
      replay(mockDispatcher);
      replay(mockDisplay);
      replay(mockDocList);
      replay(mockEventBus);
      replay(mockExactSearchCheckbox);
      replay(mockFilterTextbox);
      replay(mockHistory);
      replay(mockMessages);
      replay(mockWindowLocation);
      replay(mockWorkspaceContext);

      dlp = new DocumentListPresenter(mockDisplay, mockEventBus, mockWorkspaceContext, mockDispatcher, mockMessages, mockHistory, mockWindowLocation);

      // TODO loadDocumentList() called from onBind() looks for query parameters
      // on window objects.
      // OPTION A: add Display.getXXX for these parameters
      // OPTION B: add testable WindowLocation interface
      // going with Option B

      dlp.bind();
   }

   // TODO test triggering each of the handlers
   // TODO call onBind with appropriate mocks to load document list

   private class DoclistSuccessAnswer implements IAnswer<GetDocumentListResult>
   {

      // private GetDocumentListResult result;

      // public DoclistSuccessAnswer(GetDocumentListResult result)
      // {
      // this.result = result;
      // }

      @Override
      public GetDocumentListResult answer() throws Throwable
      {
         // TODO make a useful result object or pass it in the constructor
         GetDocumentListResult result = new GetDocumentListResult(new ProjectIterationId("mock-slug", "mock-iteration-slug"), new ArrayList<DocumentInfo>());

         // get the most recent argument before this call - should be the
         // callback function as this is the last parameter for the execute
         // method
         Object[] arguments = EasyMock.getCurrentArguments();
         @SuppressWarnings("unchecked")
         AsyncCallback<GetDocumentListResult> callback = (AsyncCallback<GetDocumentListResult>) arguments[arguments.length - 1];
         callback.onSuccess(result);
         return null;
      }
   }


   @Test
   public void getDocumentId()
   {
      // TODO mock dispatcher executing a GetDocumentList
      // GetDocumentListResult gdlr = new GetDocumentListResult(new
      // ProjectIterationId("projslug", "iterslug"), new
      // ArrayList<DocumentInfo>());
      //
      // AsyncCallback<Result> async = null;
      // Action action = null;
      //
      //
      // mockDispatcher.execute(action, async);
      //
      // DocumentId docId = dlp.getDocumentId("my/document");
      //
      // assertEquals(1234L, docId.getId());
   }

   @Test
   public void getDocumentInfo()
   {
      // throw new RuntimeException("Test not implemented");
   }
}
