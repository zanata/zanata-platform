package org.fedorahosted.flies.webtrans.editor.filter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;

public class TransFilterPresenter extends WidgetPresenter<TransFilterPresenter.Display> {
	
	public static final Place PLACE = new Place("TransUnitInfoPresenter");
	
	public interface Display extends WidgetDisplay{
		void addFilterUnitView(FilterUnitView filterUnitView);
		Button getEnableFilterButton();
		Button getDisableFilterButton();
		Button getAddFilterButton();
	}
	
	private final OperatorFilterPresenter operatorFilterPresenter;
	
	@Inject
	public TransFilterPresenter(final Display display, final EventBus eventBus, OperatorFilterPresenter operatorFilterPresenter) {
		super(display, eventBus);
		this.operatorFilterPresenter = operatorFilterPresenter;
	}
	
	
	@Override
	public Place getPlace() {
		return PLACE;
	}


	@Override
	protected void onBind() {
		operatorFilterPresenter.bind(PhraseFilter.from(""));
		display.addFilterUnitView((FilterUnitView) operatorFilterPresenter.getDisplay().asWidget());
		
		display.getEnableFilterButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.info("FilterEnabledEvent");
				eventBus.fireEvent( new FilterEnabledEvent(operatorFilterPresenter.getFilter()));
			}
		});
		
		display.getDisableFilterButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.info("FilterDisableEvent");
				eventBus.fireEvent( new FilterDisabledEvent());
			}
		});
		
		display.getAddFilterButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.info("");
				operatorFilterPresenter.addFilterUnit();
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
