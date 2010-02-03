package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class SouthPresenter extends WidgetPresenter<SouthPresenter.Display> {

	public interface Display extends WidgetDisplay {
		HasText getGlossary();
		HasText getRelated();
	}
	
	@Inject
	public SouthPresenter(Display display, EventBus eventBus) {
		super(display, eventBus);
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		refreshDisplay();
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
