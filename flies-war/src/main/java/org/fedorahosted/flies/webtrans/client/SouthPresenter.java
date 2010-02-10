package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class SouthPresenter extends WidgetPresenter<SouthPresenter.Display> implements HasVisibilityEventHandlers {
	private final TransMemoryPresenter transMemorypresenter;
	
	public interface Display extends WidgetDisplay {
		HasWidgets getWidgets();
		HasText getGlossary();
		HasText getRelated();
		HasVisibilityEventHandlers getVisibilityHandlers();
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
		display.getVisibilityHandlers().addVisibilityHandler(new VisibilityHandler(){
			@Override
			public void onVisibilityChange(VisibilityEvent tabSelectionEvent) {
				transMemorypresenter.isTransMemoryVisible(true);
			}
		});
			
		registerHandler(eventBus.addHandler(SelectionEvent.getType(), new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				transMemorypresenter.isTransUnitSelected(true);
			}
		})); 
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
	
	@Override
	public void fireEvent(GwtEvent<?> event) {
		display.getVisibilityHandlers().fireEvent(event);
	}

	@Override
	public HandlerRegistration addVisibilityHandler(VisibilityHandler handler) {
		return display.getVisibilityHandlers().addVisibilityHandler(handler);
	}
}
