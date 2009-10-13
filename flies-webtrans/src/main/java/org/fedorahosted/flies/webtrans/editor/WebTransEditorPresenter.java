package org.fedorahosted.flies.webtrans.editor;

import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class WebTransEditorPresenter extends WidgetPresenter<WebTransEditorPresenter.Display>{

	public static final Place PLACE = new Place("WebTransEditor");

	public interface Display extends WidgetDisplay{
	}

	@Inject
	public WebTransEditorPresenter(Display display, EventBus eventBus) {
		super(display, eventBus);
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
