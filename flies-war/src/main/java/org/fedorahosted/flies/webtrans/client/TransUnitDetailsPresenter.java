package org.fedorahosted.flies.webtrans.client;

import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class TransUnitDetailsPresenter extends WidgetPresenter<TransUnitDetailsPresenter.Display>{

	public interface Display extends WidgetDisplay{
		
	}


	@Inject
	public TransUnitDetailsPresenter(Display display, EventBus eventBus) {
		super(display, eventBus);
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
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
