package org.fedorahosted.flies.webtrans.editor.filter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class PhraseFilterPresenter extends FilterPresenter<PhraseFilter,PhraseFilterPresenter.Display> {
	
	@Inject
	public PhraseFilterPresenter(Display display, EventBus eventBus) {
		super(display, eventBus);
	}

	public interface Display extends WidgetDisplay {
		HasValue<String> getFilterText();
	}
	
	@Override
	public Place getPlace() {
		return null;
	}
	
	@Override
	protected void onBind() {
		display.getFilterText().addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				getFilter().setPhrase(event.getValue());
				eventBus.fireEvent( new FilterEnabledEvent(getFilter()));
			}
		});
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	@Override
	protected void onUnbind() {
	}

	@Override
	public void refreshDisplay() {
//		display.getFilterText().setValue(filter.getPhrase());
	}

	@Override
	public void revealDisplay() {
	}
	
}
