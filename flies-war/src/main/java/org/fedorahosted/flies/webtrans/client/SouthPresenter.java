package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.TransUnit;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class SouthPresenter extends WidgetPresenter<SouthPresenter.Display> {
	private final TransMemoryPresenter transMemorypresenter;
	
	public interface Display extends WidgetDisplay {
		HasWidgets getWidgets();
		HasText getGlossary();
		HasText getRelated();
	}
	
	@Inject
	public SouthPresenter(Display display, EventBus eventBus, TransMemoryPresenter transMemorypresenter) {
		super(display, eventBus);
		this.transMemorypresenter = transMemorypresenter;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		transMemorypresenter.bind();
		display.getWidgets().add(transMemorypresenter.getDisplay().asWidget());
		refreshDisplay();
		
		registerHandler(eventBus.addHandler(SelectionEvent.getType(), new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				//Check if the TransMemory Tab is visible
			
			}
				
		})); 
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
