package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.event.client.PageChangeEvent;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeEvent;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class PagerPresenter extends WidgetPresenter<PagerPresenter.Display> implements PageChangeHandler, PageCountChangeHandler{

	public static final Place PLACE = new Place("PagerPresenter");

	public interface Display extends WidgetDisplay {
		HasValue<String> getGotoPage();
		HasClickHandlers getNextPage();
		HasClickHandlers getPreviousPage();
		HasClickHandlers getFirstPage();
		HasClickHandlers getLastPage();
		HasPageCount getHasPageCount();
	}
	
	private final HasPageNavigation pageNavigation;
	
	@Inject
	public PagerPresenter(Display display, EventBus eventBus, HasPageNavigation pageNavigation) {
		super(display, eventBus);
		this.pageNavigation = pageNavigation;
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		this.display.getNextPage().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pageNavigation.gotoNextPage();
			}
		});
		this.display.getPreviousPage().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pageNavigation.gotoPreviousPage();
			}
		});
		this.display.getFirstPage().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pageNavigation.gotoFirstPage();
			}
		});
		this.display.getLastPage().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pageNavigation.gotoLastPage();
			}
		});
		this.display.getGotoPage().addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				try{
					Integer value = Integer.parseInt(event.getValue());
					pageNavigation.gotoPage(value, false);
				}
				catch(NumberFormatException e) {}
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
	}

	@Override
	public void revealDisplay() {
	}

	@Override
	public void onPageChange(PageChangeEvent event) {
		display.getGotoPage().setValue( String.valueOf(event.getNewPage()) );
	}

	@Override
	public void onPageCountChange(PageCountChangeEvent event) {
		display.getHasPageCount().setPageCount(event.getNewPageCount());
	}
	
	
}
