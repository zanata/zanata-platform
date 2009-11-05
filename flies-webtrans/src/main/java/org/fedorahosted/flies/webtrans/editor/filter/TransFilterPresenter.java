package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.editor.filter.OperatorFilter.Operator;

import net.customware.gwt.presenter.client.BasicPresenter;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.Presenter;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransFilterPresenter extends WidgetPresenter<TransFilterPresenter.Display> {
	
	public static final Place PLACE = new Place("TransUnitInfoPresenter");
	
	public interface Display extends WidgetDisplay{
		Button getApplyButton();
		void setFilterUnitPanel(Widget widget);
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
		OperatorFilter<TransUnit> filter = new OperatorFilter<TransUnit>(Operator.And);
		operatorFilterPresenter.bind(filter);
		
		display.setFilterUnitPanel(operatorFilterPresenter.getDisplay().asWidget());
		
		display.getApplyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				eventBus.fireEvent( new FilterEnabledEvent(operatorFilterPresenter.getFilter()));
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
