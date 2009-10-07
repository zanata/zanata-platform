package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class PagerPresenter extends WidgetPresenter<PagerPresenter.Display>{

	public static final Place PLACE = new Place("PagerPresenter");

	public interface Display extends WidgetDisplay {
		HasValue<String> getGotoPage();
		HasClickHandlers getNextPage();
		HasClickHandlers getPreviousPage();
		HasClickHandlers getFirstPage();
		HasClickHandlers getLastPage();
		int getPageCount();
		void setPageCount(int count);
	}
	
	@Inject
	public PagerPresenter(Display display, EventBus eventBus) {
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
