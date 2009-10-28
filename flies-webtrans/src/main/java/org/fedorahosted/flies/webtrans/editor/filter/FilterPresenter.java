package org.fedorahosted.flies.webtrans.editor.filter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;

public class FilterPresenter extends WidgetPresenter<FilterPresenter.Display> {
	
	public static final Place PLACE = new Place("TransUnitInfoPresenter");
	
	public interface Display extends WidgetDisplay{
		void setFilter(PhraseFilterWidget filter);
		Button getFilterButton();
	}
	
	private final PhraseFilterPresenter phraseFilterPresenter;
	@Inject
	public FilterPresenter(final Display display, final EventBus eventBus, PhraseFilterPresenter phraseFilterPresenter) {
		super(display, eventBus);
		this.phraseFilterPresenter = phraseFilterPresenter;
	}
	
	
	@Override
	public Place getPlace() {
		return PLACE;
	}


	@Override
	protected void onBind() {
		phraseFilterPresenter.bind(PhraseFilter.from(""));
		display.setFilter((PhraseFilterWidget) phraseFilterPresenter.getDisplay().asWidget());
		display.getFilterButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.info("filter");
				eventBus.fireEvent( new FilterEnabledEvent(phraseFilterPresenter.getFilter()));
			}
		});
	}


	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void onUnbind() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void refreshDisplay() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void revealDisplay() {
		// TODO Auto-generated method stub
		
	}

}
