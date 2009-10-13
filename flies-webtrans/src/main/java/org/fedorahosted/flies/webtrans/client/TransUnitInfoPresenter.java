package org.fedorahosted.flies.webtrans.client;

import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class TransUnitInfoPresenter extends WidgetPresenter<TransUnitInfoPresenter.Display> {
	
	public static final Place PLACE = new Place("TransUnitInfoPresenter");
	
	public interface Display extends WidgetDisplay{
		
	}
	
	@Inject
	public TransUnitInfoPresenter(final Display display, final EventBus eventBus) {
		super(display, eventBus);
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
