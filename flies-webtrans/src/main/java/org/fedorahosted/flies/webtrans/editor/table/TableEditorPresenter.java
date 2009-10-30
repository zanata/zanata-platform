package org.fedorahosted.flies.webtrans.editor.table;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.fedorahosted.flies.gwt.auth.AuthenticationError;
import org.fedorahosted.flies.gwt.auth.AuthorizationError;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnit;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnitResult;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.NotificationEvent;
import org.fedorahosted.flies.webtrans.client.WorkspaceContext;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.editor.DocumentEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.HasPageNavigation;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.gen2.event.shared.HandlerRegistration;
import com.google.gwt.gen2.table.client.TableModel;
import com.google.gwt.gen2.table.client.TableModel.Callback;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;
import com.google.gwt.gen2.table.client.TableModelHelper.SerializableResponse;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TableEditorPresenter extends DocumentEditorPresenter<TableEditorPresenter.Display> 
	implements HasPageNavigation, HasPageChangeHandlers, HasPageCountChangeHandlers {
	
	public static final Place PLACE = new Place("TableEditor");
	
	public interface Display extends WidgetDisplay, HasPageNavigation {
		HasSelectionHandlers<TransUnit> getSelectionHandlers();
		HasPageChangeHandlers getPageChangeHandlers();
		HasPageCountChangeHandlers getPageCountChangeHandlers();
		RedirectingCachedTableModel<TransUnit> getTableModel();
		void setTableModelHandler(TableModelHandler<TransUnit> hadler);
		void reloadPage();
		void setPageSize(int size);
	}

	private DocumentId documentId;

	private final DispatchAsync dispatcher;
	private final WorkspaceContext workspaceContext;
	

	@Inject
	public TableEditorPresenter(final Display display, final EventBus eventBus, final DispatchAsync dispatcher, final WorkspaceContext workspaceContext) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
		this.workspaceContext = workspaceContext;
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	private TransUnit currentSelection;
	
	@Override
	protected void onBind() {
		display.setTableModelHandler(tableModelHandler);
		display.setPageSize(50);
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
							documentId = event.getDocumentId();
							display.getTableModel().clearCache();
							display.getTableModel().setRowCount(TableModel.UNKNOWN_ROW_COUNT);
							display.gotoPage(0, true);
						}
					}
				})
			);
		
		display.gotoFirstPage();

	}

	private final TableModelHandler<TransUnit> tableModelHandler = new TableModelHandler<TransUnit>() {
		
		@Override
		public void requestRows(final Request request, final Callback<TransUnit> callback) {
			
			int numRows = request.getNumRows();
			int startRow = request.getStartRow();
			Log.info("Table requesting" + numRows + " starting from "+ startRow);
			
			if(documentId == null){
				callback.onFailure(new RuntimeException("No DocumentId"));
				return;
			}
			
			dispatcher.execute(new GetTransUnits(documentId, workspaceContext.getLocaleId(), startRow, numRows), new AsyncCallback<GetTransUnitsResult>() {
				@Override
				public void onSuccess(GetTransUnitsResult result) {
					SerializableResponse<TransUnit> response = new SerializableResponse<TransUnit>(
							result.getUnits());
					Log.debug("Got " + result.getUnits().size() +" rows back");
					callback.onRowsReady(request, response);
					Log.info("Total of " + result.getTotalCount() + " rows available");
					display.getTableModel().setRowCount(result.getTotalCount());
				}
				@Override
				public void onFailure(Throwable caught) {
					if(caught instanceof AuthenticationError) {
						eventBus.fireEvent( new NotificationEvent(Severity.Error, "Not logged in!"));
					}
					else if(caught instanceof AuthorizationError) {
						eventBus.fireEvent( new NotificationEvent(Severity.Error, "Failed to load data from Server"));
					}
					else {
						eventBus.fireEvent( new NotificationEvent(Severity.Error, "An unknown error occured"));
					}
				}
			});
		}
		
		@Override
		public boolean onSetRowValue(int row, TransUnit rowValue) {
			dispatcher.execute(
					new UpdateTransUnit(rowValue.getId(), workspaceContext.getLocaleId(), rowValue.getTarget()), 
					new AsyncCallback<UpdateTransUnitResult>() {
						@Override
						public void onFailure(Throwable caught) {
							eventBus.fireEvent(new NotificationEvent(Severity.Error, "Failed to update TransUnit"));
						}
						
						@Override
						public void onSuccess(UpdateTransUnitResult result) {
						}
					});
			return true;
		}
	};
	
	
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
		display.gotoFirstPage();
	}

	@Override
	public void gotoLastPage() {
		display.gotoLastPage();
	}

	@Override
	public void gotoNextPage() {
		display.gotoNextPage();
	}

	@Override
	public void gotoPage(int page, boolean forced) {
		display.gotoPage(page, forced);
	}

	@Override
	public void gotoPreviousPage() {
		display.gotoPreviousPage();
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
