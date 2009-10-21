package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.TreeNode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
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
		loadDocsList(workspaceContext.getProjectContainerId());
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
				if (selectedDocName != null) // folders have null names
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
		tree.clear();

		if (value != null && value.length() != 0) {
			ArrayList<DocName> filteredNames = new ArrayList<DocName>();
			for (DocName docName : docNames) {
				if (docName.getName().contains(value)) {
					filteredNames.add(docName);
				}
			}
			docNameMapper.addToTree(tree, filteredNames, true);
		} else {
			docNameMapper.addToTree(tree, docNames, false);
		}
//		for (int i = 0; i < tree.getNodeCount(); i++) {
//			final TreeNode<DocName> node = tree.getNode(i);
//			node.addMouseOverHandler(new MouseOverHandler() {
//				@Override
//				public void onMouseOver(MouseOverEvent event) {
//					System.out.println("onMouseOver "+node.getObject().getName());
//				}
//			});
//		}
	}

	public void setDocNameList(ArrayList<DocName> docNames) {
		this.docNames.clear();
		this.docNames.addAll(docNames);
		
		Collections.sort(this.docNames, new Comparator<DocName>() {
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
		
		display.getFilterText().setText("");
		filterBy("");
	}

	private void loadDocsList(ProjectContainerId id) {
		// switch doc list to the new project
		dispatcher.execute(new GetDocsList(id), new AsyncCallback<GetDocsListResult>() {
			@Override
			public void onFailure(Throwable caught) {
				eventBus.fireEvent( new NotificationEvent(Severity.Error, "Failed to load data from Server"));
			}
			@Override
			public void onSuccess(GetDocsListResult result) {
				setDocNameList(result.getDocNames());
			}
		});
	}

}
