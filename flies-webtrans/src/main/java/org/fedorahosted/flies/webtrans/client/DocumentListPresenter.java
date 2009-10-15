package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.ProjectIterationId;
import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display> 
		implements HasDocumentSelectionHandlers {

	private final DocNameMapper docNameMapper;
	private final DispatchAsync dispatcher;

	@Inject
	public DocumentListPresenter(Display display, EventBus eventBus,
			WorkspaceContext workspaceContext,
			DocNameMapper docNameMapper, DispatchAsync dispatcher) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
		GWT.log("DocumentListPresenter()", null);
		this.docNameMapper = docNameMapper;
		loadDocsList(workspaceContext.getProjectIterationId());
	}

	public static final Place PLACE = new Place("DocumentListList");
	
	public interface Display extends WidgetDisplay {
		HasValueChangeHandlers<String> getFilterChangeSource();
		HasKeyUpHandlers getFilterKeyUpSource();
		HasText getFilterText();
		HasTreeNodes<DocName> getTree();
	}
	
	private DocumentId currentDoc;
	private ArrayList<DocName> docNames = new ArrayList<DocName>();
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		registerHandler(display.getFilterKeyUpSource().addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				filterBy(display.getFilterText().getText());
			}
		}));
		registerHandler(getDisplay().getTree().addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				DocName selectedDocName = (DocName) event.getSelectedItem().getUserObject();
				setValue(selectedDocName.getId(), true);
			}
		}));
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
		// TODO Auto-generated method stub
		
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

	public void filterBy(String value) {
		HasTreeNodes<DocName> tree = display.getTree();

		ArrayList<DocName> filteredNames = new ArrayList<DocName>();
		for (DocName docName : docNames) {
			if (docName.getName().contains(value)) {
				filteredNames.add(docName);
			}
		}
		
		tree.clear();
		docNameMapper.addToTree(tree, filteredNames);

	}

	public void setDocNameList(ArrayList<DocName> docNames) {
		this.docNames = docNames;
		display.getTree().clear();
		docNameMapper.addToTree(display.getTree(), docNames);
	}

	private void loadDocsList(ProjectIterationId id) {
		// switch doc list to the new project
		dispatcher.execute(new GetDocsList(id), new AsyncCallback<GetDocsListResult>() {
			@Override
			public void onFailure(Throwable caught) {
				Log.error(caught.toString());
			}
			@Override
			public void onSuccess(GetDocsListResult result) {
				setDocNameList(result.getDocNames());
			}
		});
	}

}
