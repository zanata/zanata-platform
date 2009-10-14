package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display> implements HasValue<String> {

	private final DocNameMapper docNameMapper;

	@Inject
	public DocumentListPresenter(Display display, EventBus eventBus, DocNameMapper docNameMapper) {
		super(display, eventBus);
		GWT.log("DocumentListPresenter()", null);
		this.docNameMapper = docNameMapper;
		ArrayList<DocName> names = new ArrayList<DocName>();
		names.add(new DocName("id1", "path1name1", "path/1"));
		names.add(new DocName("id2", "path1name2", "path/1"));
		names.add(new DocName("id3", "path2name1", "path/2"));
		names.add(new DocName("id4", "path2name2", "path/2"));
		names.add(new DocName("id5", "name2", ""));
		names.add(new DocName("id6", "name1", null));
		setDocNameList(names);

	}

	public static final Place PLACE = new Place("DocumentListList");
	
	public interface Display extends WidgetDisplay {
		HasTreeNodes<DocName> getTree();
	}
	
	private String currentDoc;
	private ArrayList<DocName> docNames = new ArrayList<DocName>();
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		registerHandler(getDisplay().getTree().addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
//				event.getSelectedItem().getUserObject();
				setValue(event.getSelectedItem().getText(), true);
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
	
	@Override
	public String getValue() {
		return currentDoc;
	}

	@Override
	public void setValue(String value) {
		currentDoc = value;
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		String oldValue = currentDoc;
		currentDoc = value;
		ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return eventBus.addHandler(ValueChangeEvent.getType(), handler);
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

}
