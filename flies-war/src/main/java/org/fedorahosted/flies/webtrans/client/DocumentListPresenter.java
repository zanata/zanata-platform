package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCountResult;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.ui.HasFilter;
import org.fedorahosted.flies.webtrans.editor.filter.ContentFilter;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display> 
		implements HasDocumentSelectionHandlers {

	public static final Place PLACE = new Place("DocumentListList");
	
	public interface Display extends WidgetDisplay {

		void setList(ArrayList<DocName> sortedList);
		void clearSelection();
		void setSelection(DocumentId documentId);
		void ensureSelectionVisible();
		HasDocumentSelectionHandlers getDocumentSelectionHandler();
		void setFilter(ContentFilter<DocName> filter);
		void removeFilter();
		HasValue<String> getFilterTextBox();
	}

	private final DispatchAsync dispatcher;
    private final WorkspaceContext workspaceContext;
    private final ProjectContainerId projectContainerId;
	private final Map<DocumentId, DocumentStatus> statuscache = new HashMap<DocumentId, DocumentStatus>();
	private DocumentId currentDoc;
    
	
	@Inject
	public DocumentListPresenter(Display display, EventBus eventBus,
			WorkspaceContext workspaceContext,
			CachingDispatchAsync dispatcher) {
		super(display, eventBus);
		this.workspaceContext = workspaceContext;
		this.projectContainerId = workspaceContext.getProjectContainerId();
		this.dispatcher = dispatcher;
		Log.info("DocumentListPresenter()");
		loadDocsList();
	}
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		
		registerHandler(display.getDocumentSelectionHandler().addDocumentSelectionHandler(new DocumentSelectionHandler() {
			
			@Override
			public void onDocumentSelected(DocumentSelectionEvent event) {
				display.setSelection(event.getDocumentId());
				setValue(event.getDocumentId(), true);
			}
		}));
		
		//display.setProjectStatusBar(prStatusPresenter.getDisplay().asWidget());
		
		registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler() {
			@Override
			public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
				DocumentStatus doc = statuscache.get(event.getDocumentId());
				if (doc == null)
					return;  // GetProjectStatusCount hasn't returned yet!
//				ContentState status = event.getPreviousStatus();
//				doc.setStatus(status, doc.getStatus(status)-1);
//				status = event.getNewStatus();
//				doc.setStatus(status, doc.getStatus(status)+1);
//				TreeNode<DocName> node = display.getTree().getNodeByKey(doc.getDocumentid());
//				node.setName(node.getObject().getName() + " ("+ calPercentage(doc.getUntranslated(), doc.getFuzzy(), doc.getTranslated()) +"%)");
			}


		}));
		
//		registerHandler(getDisplay().getTree().addSelectionHandler(new SelectionHandler<TreeItem>() {
//			@Override
//			public void onSelection(SelectionEvent<TreeItem> event) {
//				DocName selectedDocName = (DocName) event.getSelectedItem().getUserObject();
//				if (selectedDocName != null) // folders have null names
//					setValue(selectedDocName.getId(), true);
//			}
//		}));
//		
//		registerHandler(display.getReloadButton().addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				refreshDisplay();
//			}
//		}));
		
		registerHandler(display.getFilterTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if(event.getValue().isEmpty()) {
					display.removeFilter();
				}
				else {
					basicContentFilter.setPattern(event.getValue());
					display.setFilter(basicContentFilter);
				}
			}
		}));
		
	}

	final class BasicContentFilter implements ContentFilter<DocName> {
		private String pattern = "";
		
		@Override
		public boolean accept(DocName value) {
			return value.getName().contains(pattern);
		}
		
		public void setPattern(String pattern) {
			this.pattern = pattern;
		}
	}
	
	private final BasicContentFilter basicContentFilter = new BasicContentFilter();
	
	
	private long calPercentage (long untranslated, long fuzzy, long translated) {
		
		if (translated < 0 || untranslated < 0 || fuzzy < 0
				|| (translated + untranslated + fuzzy) == 0) {
			return 0;
		} else {
			long value =  (long)((translated  * 100) / (fuzzy + untranslated + translated));
			return value;
		}
		
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnbind() {
	}

	@Override
	public void refreshDisplay() {
		loadDocsList();
	}

	@Override
	public void revealDisplay() {
		// TODO Auto-generated method stub
		
	}


	public void setValue(DocumentId value, boolean fireEvents) {
		DocumentId oldValue = currentDoc;
		currentDoc = value;
		if (oldValue != currentDoc) {
			fireEvent(new DocumentSelectionEvent(currentDoc));
		}
	}

	@Override
	public HandlerRegistration addDocumentSelectionHandler(
			DocumentSelectionHandler handler) {
		return eventBus.addHandler(DocumentSelectionEvent.getType(), handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEvent(event);
	}

	public void setDocNameList(ArrayList<DocName> docNames) {
		ArrayList<DocName> sortedList = new ArrayList<DocName>(docNames);
		
		Collections.sort(sortedList, new Comparator<DocName>() {
			@Override
			public int compare(DocName o1, DocName o2) {
				String path1 = o1.getPath();
				if(path1 == null)
					path1 = "";
				String path2 = o2.getPath();
				if(path2 == null)
					path2 = "";
				int pathCompare = path1.compareTo(path2);
				if(pathCompare == 0)
					return o1.getName().compareTo(o2.getName());
				return pathCompare;
			}});
		display.setList(sortedList);
	}

	private void loadDocsList() {
		loadDocsStatus();
		// switch doc list to the new project
		dispatcher.execute(
				new GetDocsList(projectContainerId), 
				new AsyncCallback<GetDocsListResult>() {
			@Override
			public void onFailure(Throwable caught) {
				eventBus.fireEvent( new NotificationEvent(Severity.Error, "Failed to load data from Server"));
			}
			@Override
			public void onSuccess(GetDocsListResult result) {
				final ArrayList<DocName> docNames = result.getDocNames();
				Log.info("Received doc list for "+result.getProjectContainerId()+": "+docNames.size()+" elements");
				setDocNameList(docNames);
			}
		});
	}
	
	private void loadDocsStatus() {
		dispatcher.execute(
				new GetProjectStatusCount(projectContainerId , workspaceContext.getLocaleId()), 
				new AsyncCallback<GetProjectStatusCountResult>() {
			@Override
			public void onFailure(Throwable caught) {
				Log.info("load Doc Status failure "+caught.getMessage());
			}
			@Override
			public void onSuccess(GetProjectStatusCountResult result) {
				ArrayList<DocumentStatus> liststatus = result.getStatus();
				Log.info("Received project status for "+result.getProjectContainerId()+": "+liststatus.size()+" elements");
				statuscache.clear();
				for(DocumentStatus doc : liststatus) {
					statuscache.put(doc.getDocumentid(), doc);
					//TreeNode<DocName> node = display.getTree().getNodeByKey(doc.getDocumentid());
					//node.setName(node.getObject().getName() + " ("+ calPercentage(doc.getUntranslated(), doc.getFuzzy(), doc.getTranslated()) +"%)");
				}
			}
		});
	}

}
