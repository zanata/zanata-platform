package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.webtrans.client.TopMenuPresenter.Display;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class TransUnitNavigationPresenter 
	extends WidgetPresenter<TransUnitNavigationPresenter.Display> implements HasNavTransUnitHandlers {

	
	public interface Display extends WidgetDisplay {
		HasClickHandlers getPrevEntryButton();

		HasClickHandlers getNextEntryButton();

		HasClickHandlers getPrevFuzzyButton();

		HasClickHandlers getNextFuzzyButton();

		HasClickHandlers getPrevUntranslatedButton();

		HasClickHandlers getNextUntranslatedButton();
	}

	@Inject
	public TransUnitNavigationPresenter(Display display, EventBus eventBus) {
		super(display, eventBus);
	}

	@Override
	protected void onBind() {
		display.getPrevEntryButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(null, -1));
			}
		});

		display.getNextEntryButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(null, +1));
			}
		});

		display.getPrevFuzzyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.NeedReview, -1));
			}
		});

		display.getNextFuzzyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.NeedReview, +1));
			}
		});

		display.getPrevUntranslatedButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.New, -1));
			}
		});

		display.getNextUntranslatedButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.New, +1));
			}
		});
	}
	
	@Override
	public HandlerRegistration addNavTransUnitHandler(
			NavTransUnitHandler handler) {
		// TODO Auto-generated method stub
		return eventBus.addHandler(NavTransUnitEvent.getType(), handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEvent(event);
	}

	@Override
	public Place getPlace() {
		return null;
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
