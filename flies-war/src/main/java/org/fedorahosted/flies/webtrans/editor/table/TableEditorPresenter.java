package org.fedorahosted.flies.webtrans.editor.table;

import static org.fedorahosted.flies.webtrans.editor.table.TableConstants.MAX_PAGE_ROW;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.gwt.auth.AuthenticationError;
import org.fedorahosted.flies.gwt.auth.AuthorizationError;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.EditingTranslationAction;
import org.fedorahosted.flies.gwt.rpc.EditingTranslationResult;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnit;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnitResult;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.NavTransUnitEvent;
import org.fedorahosted.flies.webtrans.client.NavTransUnitHandler;
import org.fedorahosted.flies.webtrans.client.NotificationEvent;
import org.fedorahosted.flies.webtrans.client.WorkspaceContext;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.client.events.TransUnitEditEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitEditEventHandler;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.editor.DocumentEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.HasPageNavigation;
import org.fedorahosted.flies.webtrans.editor.filter.ContentFilter;
import org.fedorahosted.flies.webtrans.editor.filter.FilterDisabledEvent;
import org.fedorahosted.flies.webtrans.editor.filter.FilterDisabledEventHandler;
import org.fedorahosted.flies.webtrans.editor.filter.FilterEnabledEvent;
import org.fedorahosted.flies.webtrans.editor.filter.FilterEnabledEventHandler;

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
	private int latestStatusCountOffset = -1;
	
	public interface Display extends WidgetDisplay, HasPageNavigation {
		HasSelectionHandlers<TransUnit> getSelectionHandlers();
		HasPageChangeHandlers getPageChangeHandlers();
		HasPageCountChangeHandlers getPageCountChangeHandlers();
		RedirectingCachedTableModel<TransUnit> getTableModel();
		void setTableModelHandler(TableModelHandler<TransUnit> hadler);
		void reloadPage();
		void setPageSize(int size);
		void setContentFilter(ContentFilter<TransUnit> filter);
		void clearContentFilter();
		void gotoRow(int row);
		int getCurrentPageNumber();
		TransUnit getTransUnitValue(int row);
	}

	private DocumentId documentId;

	private final DispatchAsync dispatcher;
	private final WorkspaceContext workspaceContext;
	private final Identity identity;

	@Inject
	public TableEditorPresenter(final Display display, final EventBus eventBus, final CachingDispatchAsync dispatcher,final Identity identity, final WorkspaceContext workspaceContext) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
		this.workspaceContext = workspaceContext;
		this.identity = identity;
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	private TransUnit currentSelection;
	
	@Override
	protected void onBind() {
		display.setTableModelHandler(tableModelHandler);
		display.setPageSize(TableConstants.PAGE_SIZE);
		registerHandler(display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				if(event.getSelectedItem() != currentSelection) {
					currentSelection = event.getSelectedItem();
					//startEditing(currentSelection);
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
		
		registerHandler(eventBus.addHandler(FilterEnabledEvent.getType(), new FilterEnabledEventHandler() {
			@Override
			public void onFilterEnabled(FilterEnabledEvent event) {
				display.setContentFilter(event.getContentFilter());
			}
		}));
		
		registerHandler(eventBus.addHandler(FilterDisabledEvent.getType(), new FilterDisabledEventHandler() {
			
			@Override
			public void onFilterDisabled(FilterDisabledEvent event) {
				display.clearContentFilter();
			}
		}));
		
		registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler() {
			@Override
			public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
				if(documentId != null && documentId.equals(event.getDocumentId())) {
					if(currentSelection != null && currentSelection.getId().equals(event.getTransUnitId())) {
						// handle change in current selection
						//eventBus.fireEvent(new NotificationEvent(Severity.Warning, "Someone else updated this translation unit. you're in trouble..."));
						//display.getTableModel().setRowValue(row, rowValue);
					}
					// TODO add model with methods such as
					//currentSelection.getRowIndex(TransUnitId); 
					// - add TU index to model
					//display.getTableModel().setRowValue(rowIndex, currentSelection.getTarget());
					display.getTableModel().clearCache();
					display.reloadPage();
					//dispatcher.execute(new GetTransUnits(documentId, localeId, page*pageSize+rowOffset, 1, count), callback)
				}
			}
		}));
		
		registerHandler(eventBus.addHandler(TransUnitEditEvent.getType(), new TransUnitEditEventHandler() {
			@Override
			public void onTransUnitEdit(TransUnitEditEvent event) {
				if(documentId != null && documentId.equals(event.getDocumentId())) {
					if(currentSelection != null && currentSelection.getId().equals(event.getTransUnitId())) {
						// handle change in current selection
						if(!event.getSessionId().equals(identity.getSessionId())) 
							eventBus.fireEvent(new NotificationEvent(Severity.Warning, "Warning: This Translation Unit is being edited by someone else."));
					}
					//display.getTableModel().clearCache();
					//display.reloadPage();
				}
			}
		}));
		
		registerHandler(eventBus.addHandler(NavTransUnitEvent.getType(), new NavTransUnitHandler() {
			@Override
			public void onNavTransUnit(NavTransUnitEvent event) {
				if(currentSelection != null) {
					int currow = getCurrentRow();
					int step = event.getStep();
					//Send message to server to stop editing current selection
					//stopEditing(currentSelection);
					
					//If goto Next or Prev Trans Unit
					if (event.getRowType() == null ) {
						if (event.getStep() > 0 && currow < MAX_PAGE_ROW) {
							currentSelection = display.getTransUnitValue(currow+step);
							display.gotoRow(currow+step);
						}
						if (event.getStep() < 0 && currow > 0) {
							currentSelection = display.getTransUnitValue(currow+step);
							display.gotoRow(currow+step);
						}
					}
					
					//If goto Next or Prev Fuzzy Trans Unit
					if (event.getRowType() == ContentState.NeedReview) {
						if (event.getStep() > 0)
							nextFuzzy(currow);
						if (event.getStep() < 0)
							prevFuzzy(currow);
					}
				}
			}
		}));
		
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
						Log.error("GetTransUnits failure " + caught);
						eventBus.fireEvent( new NotificationEvent(Severity.Error, "An unknown error occurred"));
					}
				}
			});
		}
		
		@Override
		public boolean onSetRowValue(int row, TransUnit rowValue) {
			if(rowValue.getTarget().isEmpty())
				rowValue.setStatus(ContentState.New);
			dispatcher.execute(
					new UpdateTransUnit(rowValue.getId(), workspaceContext.getLocaleId(), rowValue.getTarget(),rowValue.getStatus()), 
					new AsyncCallback<UpdateTransUnitResult>() {
						@Override
						public void onFailure(Throwable caught) {
							Log.error("UpdateTransUnit failure " + caught);
							eventBus.fireEvent(new NotificationEvent(Severity.Error, "Failed to update Translation Unit"));
						}
						
						@Override
						public void onSuccess(UpdateTransUnitResult result) {
							eventBus.fireEvent(new NotificationEvent(Severity.Info, "Saved change to Translation Unit"));
						}
					});
			
			dispatcher.execute(
					new EditingTranslationAction(rowValue.getId(), 
							workspaceContext.getLocaleId(), 
							identity.getSessionId(), 
							EditState.StopEditing), 
					new AsyncCallback<EditingTranslationResult>() {
						@Override
						public void onFailure(Throwable caught) {
							eventBus.fireEvent(new NotificationEvent(Severity.Error, "Failed to Stop Editing TransUnit"));
						}
						
						@Override
						public void onSuccess(EditingTranslationResult result) {
							//eventBus.fireEvent(new NotificationEvent(Severity.Warning, "TransUnit Editing is finished"));
						}
			});
			
			return true;
		}
		
		public void onCancel(TransUnit rowValue) {
			//stopEditing(rowValue);
		}

		@Override
		public void gotoRow(int row) {
			currentSelection = display.getTransUnitValue(row);
			display.gotoRow(row);
		}

		@Override
		public void gotoNextFuzzy(int row) {
			nextFuzzy(row);
		}

		@Override
		public void gotoPrevFuzzy(int row) {
			prevFuzzy(row);
		}
	};
	
	private void stopEditing(TransUnit rowValue) {
		dispatcher.execute(
				new EditingTranslationAction(rowValue.getId(), 
						workspaceContext.getLocaleId(), 
						identity.getSessionId(),
						EditState.StopEditing), 
				new AsyncCallback<EditingTranslationResult>() {
					@Override
					public void onSuccess(EditingTranslationResult result) {
						//eventBus.fireEvent(new NotificationEvent(Severity.Warning, "TransUnit Editing is finished"));
					}
					
					@Override
					public void onFailure(Throwable caught) {
						eventBus.fireEvent(new NotificationEvent(Severity.Error, "Failed to Stop Editing TransUnit"));
					}
					
		});
	}
	
	private void startEditing(TransUnit rowValue) {
		//Send a START_EDIT event
		dispatcher.execute(
				new EditingTranslationAction(
						rowValue.getId(), 
						workspaceContext.getLocaleId(), 
						identity.getSessionId(), 
						EditState.StartEditing), 
				new AsyncCallback<EditingTranslationResult>() {
					@Override
					public void onFailure(Throwable caught) {
						eventBus.fireEvent(new NotificationEvent(Severity.Error, "Failed to Lock TransUnit"));
					}
					
					@Override
					public void onSuccess(EditingTranslationResult result) {
					}
		});
	}
	
	
	private void prevFuzzy(int row) { 
		int currow  = row;
		row=row-1;
		while(row > 0) {
			if(display.getTransUnitValue(row).getStatus()==ContentState.NeedReview) {
				display.gotoRow(row);
				currentSelection = display.getTransUnitValue(row);
				break;
			}
			else {
				row = row - 1;
			}
		}
		//If the last row is not fuzzy, we will keep the editor in current row open
		if(row == 0 && display.getTransUnitValue(row).getStatus() !=ContentState.NeedReview) {
			display.gotoRow(currow);
			currentSelection = display.getTransUnitValue(currow);
		}
		else if(row == 0 && display.getTransUnitValue(row).getStatus() ==ContentState.NeedReview) {
			display.gotoRow(row);
			currentSelection = display.getTransUnitValue(row);
		}
		
	}
	
	private void nextFuzzy(int row) { 
		int currow  = row;
		row=row+1;
		while(row < MAX_PAGE_ROW) {
			if(display.getTransUnitValue(row).getStatus()==ContentState.NeedReview) {
				currentSelection = display.getTransUnitValue(row);
				display.gotoRow(row);
				break;
			}
			else {
				row = row + 1;
			}
		}
		//If the last row is not fuzzy, we will keep the editor in current row open
		if(row == MAX_PAGE_ROW && display.getTransUnitValue(row).getStatus() !=ContentState.NeedReview) {
			display.gotoRow(currow);
			currentSelection = display.getTransUnitValue(currow);
		}
		else if(row == MAX_PAGE_ROW && display.getTransUnitValue(row).getStatus() ==ContentState.NeedReview) {
			display.gotoRow(row);
			currentSelection = display.getTransUnitValue(row);
		}
		
	}
	
	
	public TransUnit getCurrentSelection() {
		return currentSelection;
	}
	
	public int getCurrentRow() {
		Long id = currentSelection.getId().getValue();
		int currentPage = display.getCurrentPageNumber();
		return (int)(id-currentPage*TableConstants.PAGE_SIZE-1);
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
	
	public DocumentId getDocumentId() {
		return documentId;
	}
}
