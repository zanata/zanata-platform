package org.fedorahosted.flies.webtrans.editor;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
import com.google.gwt.gen2.table.event.client.PageChangeEvent;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeEvent;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class TransUnitListPresenter  extends WidgetPresenter<TransUnitListPresenter.Display> {
	
	public static final Place PLACE = new Place("TransUnitList");
	
	//private final DispatchAsync dispatcher;	
	
	public interface Display extends WidgetDisplay {
		HasSelectionHandlers<TransUnit> getSelectionHandlers();
		HasWidgets getToolbar();
		HasPageNavigation getPageNavigation();
		HasPageChangeHandlers getPageChangeHandlers();
		HasPageCountChangeHandlers getPageCountChangeHandlers();
	}

	@Inject
	public TransUnitListPresenter(final Display display, final EventBus eventBus) {
		super(display, eventBus);
		//this.dispatcher = dispatcher;
	}
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	private TransUnit currentSelection;
	
	@Override
	protected void onBind() {
		final Pager pager = new Pager();
		
		pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				display.getPageNavigation().gotoPage(event.getValue(), false);
			}
		});
		
		display.getPageCountChangeHandlers().addPageCountChangeHandler(new PageCountChangeHandler() {
			@Override
			public void onPageCountChange(PageCountChangeEvent event) {
				pager.setPageCount(event.getNewPageCount());
			}
		});
		
		display.getPageChangeHandlers().addPageChangeHandler(new PageChangeHandler() {
			@Override
			public void onPageChange(PageChangeEvent event) {
				pager.setValue(event.getNewPage());
			}
		});
		
		//display.getToolbar().add(pager);
		
		display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				if(event.getSelectedItem() != currentSelection) {
					currentSelection = event.getSelectedItem();
					eventBus.fireEvent(event);
				}
			}
		});
		
	}

	public TransUnit getCurrentSelection() {
		return currentSelection;
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
