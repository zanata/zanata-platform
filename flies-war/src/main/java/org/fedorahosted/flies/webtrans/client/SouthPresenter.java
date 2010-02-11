package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class SouthPresenter extends WidgetPresenter<SouthPresenter.Display> {
	private final TransMemoryPresenter transMemorypresenter;
	
	public interface Display extends WidgetDisplay {
		HasWidgets getWidgets();
		HasText getGlossary();
		HasValueChangeHandlers<Boolean> getValueChangeHandlers();
		HandlerRegistration addValueChangeHandler(
				ValueChangeHandler<Boolean> handler);
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
		display.getValueChangeHandlers().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if(event.getValue())
						eventBus.fireEvent(new VisibilityEvent(true));
					else
						eventBus.fireEvent(new VisibilityEvent(false));
				}
			}
		);
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
