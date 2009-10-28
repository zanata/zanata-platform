package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.event.client.PageChangeEvent;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeEvent;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class WebTransEditorPresenter extends WidgetPresenter<WebTransEditorPresenter.Display>{

	public static final Place PLACE = new Place("WebTransEditor");
	private final StatusBarPresenter statusbarpresenter;

	public interface Display extends WidgetDisplay{
		Pager getPager();
		StatusBar getStatusBar();
		WebTransScrollTable getScrollTable();
		CachedWebTransTableModel getCachedTableModel();
	}

	@Inject
	public WebTransEditorPresenter(Display display, EventBus eventBus, final StatusBarPresenter statusbarpresenter) {
		super(display, eventBus);
		this.statusbarpresenter = statusbarpresenter;
		bind();
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
        statusbarpresenter.bind();
		display.getPager().setVisible(false);
		
		eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler() {
			
			@Override
			public void onDocumentSelected(DocumentSelectionEvent event) {
				if(!event.getDocumentId().equals(display.getCachedTableModel().getTableModel().getCurrentDocumentId())) {
					Log.info("Requested document "+ event.getDocumentId());
					display.getCachedTableModel().getTableModel().setCurrentDocumentId(event.getDocumentId());
					display.getCachedTableModel().clearCache();
					display.getScrollTable().gotoPage(0, true);
				}
			}
		});
		
		display.getPager().addValueChangeHandler( new ValueChangeHandler<Integer>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				display.getScrollTable().gotoPage(event.getValue()-1, false);
			}
		});
		
		display.getScrollTable().addPageChangeHandler( new PageChangeHandler() {
			@Override
			public void onPageChange(PageChangeEvent event) {
				display.getPager().setValue(event.getNewPage()+1);
			}
		});
		display.getScrollTable().addPageCountChangeHandler(new PageCountChangeHandler() {
			
			@Override
			public void onPageCountChange(PageCountChangeEvent event) {
				display.getPager().setPageCount(event.getNewPageCount());
				display.getPager().setVisible(true);
			}
		});
		
		display.getScrollTable().gotoFirstPage();

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
