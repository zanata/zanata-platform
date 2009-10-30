package org.fedorahosted.flies.webtrans.editor.table;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.apache.catalina.loader.Reloader;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.editor.DocumentEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.HasPageNavigation;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.gen2.event.shared.HandlerRegistration;
import com.google.gwt.gen2.table.client.CachedTableModel;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class TableEditorPresenter extends DocumentEditorPresenter<TableEditorPresenter.Display> 
	implements HasPageNavigation, HasPageChangeHandlers, HasPageCountChangeHandlers {
	
	public static final Place PLACE = new Place("TransUnitList");
	
	private DocumentId documentId;
	
	private final TableEditorCachedTableModel tableModel;
	
	public interface Display extends WidgetDisplay {
		HasSelectionHandlers<TransUnit> getSelectionHandlers();
		HasPageNavigation getPageNavigation();
		HasPageChangeHandlers getPageChangeHandlers();
		HasPageCountChangeHandlers getPageCountChangeHandlers();
		void reloadPage();
	}

	@Inject
	public TableEditorPresenter(final Display display, final EventBus eventBus, final TableEditorCachedTableModel tableModel) {
		super(display, eventBus);
		this.tableModel = tableModel;
	}
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	private TransUnit currentSelection;
	
	@Override
	protected void onBind() {
		
		registerHandler(display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				if(event.getSelectedItem() != currentSelection) {
					currentSelection = event.getSelectedItem();
					eventBus.fireEvent(event);
				}
			}
		}));

		registerHandler(
				eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler() {
					@Override
					public void onDocumentSelected(DocumentSelectionEvent event) {
						if(!event.getDocumentId().equals(documentId)) {
							tableModel.setDocumentId(event.getDocumentId());
							display.getPageNavigation().gotoPage(0, true);
							
						}
					}
				})
			);
		
		display.getPageNavigation().gotoFirstPage();
		
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

	@Override
	public void gotoFirstPage() {
		display.getPageNavigation().gotoFirstPage();
	}

	@Override
	public void gotoLastPage() {
		display.getPageNavigation().gotoLastPage();
	}

	@Override
	public void gotoNextPage() {
		display.getPageNavigation().gotoNextPage();
	}

	@Override
	public void gotoPage(int page, boolean forced) {
		display.getPageNavigation().gotoPage(page, forced);
	}

	@Override
	public void gotoPreviousPage() {
		display.getPageNavigation().gotoPreviousPage();
	}

	@Override
	public HandlerRegistration addPageChangeHandler(PageChangeHandler handler) {
		return display.getPageChangeHandlers().addPageChangeHandler(handler);
	}

	@Override
	public HandlerRegistration addPageCountChangeHandler(
			PageCountChangeHandler handler) {
		return display.getPageCountChangeHandlers().addPageCountChangeHandler(handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addSelectionHandler(
			SelectionHandler<TransUnit> handler) {
		return display.getSelectionHandlers().addSelectionHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		display.getSelectionHandlers().fireEvent(event);
	}
}
