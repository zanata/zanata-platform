package org.fedorahosted.flies.webtrans.client;

import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display> {

	@Inject
	public DocumentListPresenter(Display display, EventBus eventBus) {
		super(display, eventBus);
		
	}

	public static final Place PLACE = new Place("DocumentListList");
	
	public interface Display extends WidgetDisplay {
		
	}
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnbind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshDisplay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revealDisplay() {
		// TODO Auto-generated method stub
		
	}
}
