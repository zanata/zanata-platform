package org.fedorahosted.flies.webtrans.client.mvp;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.inject.Inject;

public class TransUnitListPresenter  extends WidgetPresenter<TransUnitListPresenter.Display> {
	
	public static final Place PLACE = new Place("TransUnitList");
	
	//private final DispatchAsync dispatcher;	
	
	public interface Display extends WidgetDisplay {
		
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
