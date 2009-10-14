package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.DocName;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class FilterDocListPresenter extends
		WidgetPresenter<FilterDocListPresenter.Display> implements
		HasValue<String> {
	
	private final DocumentListPresenter docListPresenter;

	@Inject
	public FilterDocListPresenter(Display display, EventBus eventBus, DocumentListPresenter docListPresenter) {
		super(display, eventBus);
		this.docListPresenter = docListPresenter;
	}

	public interface Display extends WidgetDisplay {
		HasClickHandlers getClearButton();
		HasClickHandlers getFilterButton();
		HasValueChangeHandlers<String> getFilterChangeSource();
		HasText getFilterText();
	}

	
	public static final Place PLACE = new Place("FilterDocumentList");
	
	public void setDocNameList(ArrayList<DocName> docNames) {
		docListPresenter.setDocNameList(docNames);
		docListPresenter.filterBy("");
	}


	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	public String getValue() {
		return docListPresenter.getValue();
	}

	@Override
	public void setValue(String value) {
		docListPresenter.setValue(value);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		docListPresenter.setValue(value, fireEvents);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return docListPresenter.addValueChangeHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		docListPresenter.fireEvent(event);
	}

	@Override
	protected void onBind() {
		registerHandler(display.getFilterChangeSource().addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				docListPresenter.filterBy(event.getValue());
			}
		}));
		registerHandler(display.getFilterButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				docListPresenter.filterBy(display.getFilterText().getText());
//				GWT.log("click", null);
			}
		}));
		registerHandler(display.getClearButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				display.getFilterText().setText("");
			}
		}));
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onUnbind() {
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
