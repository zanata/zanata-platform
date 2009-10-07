package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.model.TransUnit;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.inject.Inject;

public class TransUnitListPresenter  extends WidgetPresenter<TransUnitListPresenter.Display> {
	
	public static final Place PLACE = new Place("TransUnitList");
	
	//private final DispatchAsync dispatcher;	
	
	public interface Display extends WidgetDisplay {
		HasSelectionHandlers<TransUnit> getSelectionHandlers();
	}

	@Inject
	public TransUnitListPresenter(final Display display, final EventBus eventBus) {
		super(display, eventBus);
		//this.dispatcher = dispatcher;
		bind();
	}
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	private TransUnit currentSelection;
	
	@Override
	protected void onBind() {
		display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				if(event.getSelectedItem() != currentSelection) {
					currentSelection = event.getSelectedItem();
					eventBus.fireEvent(event);
				}
			}
		});
	}

	public TransUnit getCurrentSelection() {
		return currentSelection;
	}
	
	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	@Override
	protected void onUnbind() {
	}

	@Override
	public void refreshDisplay() {
	}

	@Override
	public void revealDisplay() {
	}
	

}
