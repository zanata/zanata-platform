package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display> implements HasValue<String> {

	@Inject
	public DocumentListPresenter(Display display, EventBus eventBus) {
		super(display, eventBus);
		
	}

	public static final Place PLACE = new Place("DocumentListList");
	
	public interface Display extends WidgetDisplay {
		HasSelectionHandlers<TreeItem> getTree();
		
	}
	
	private String currentDoc;
	private HandlerRegistration handlerRegistration;
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		handlerRegistration = getDisplay().getTree().addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
//				event.getSelectedItem().getUserObject();
				setValue(event.getSelectedItem().getText(), true);
			}
		});
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnbind() {
		handlerRegistration.removeHandler();
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
}
