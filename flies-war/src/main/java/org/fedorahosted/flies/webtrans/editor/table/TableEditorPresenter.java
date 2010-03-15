package org.fedorahosted.flies.webtrans.editor.table;

import static org.fedorahosted.flies.webtrans.editor.table.TableConstants.MAX_PAGE_ROW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.fedorahosted.flies.gwt.model.TransUnitId;
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
import org.fedorahosted.flies.webtrans.client.TransMemoryCopyEvent;
import org.fedorahosted.flies.webtrans.client.TransMemoryCopyHandler;
import org.fedorahosted.flies.webtrans.client.WorkspaceContext;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.client.events.TransUnitEditEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitEditEventHandler;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.editor.DocumentEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.HasPageNavigation;
import org.fedorahosted.flies.webtrans.editor.filter.ContentFilter;
import org.fedorahosted.flies.webtrans.editor.filter.FilterDisabledEvent;
import org.fedorahosted.flies.webtrans.editor.filter.FilterDisabledEventHandler;
import org.fedorahosted.flies.webtrans.editor.filter.FilterEnabledEvent;
import org.fedorahosted.flies.webtrans.editor.filter.FilterEnabledEventHandler;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.event.shared.HandlerRegistration;
import com.google.gwt.gen2.table.client.TableModel;
import com.google.gwt.gen2.table.client.TableModel.Callback;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;
import com.google.gwt.gen2.table.client.TableModelHelper.SerializableResponse;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
import com.google.gwt.gen2.table.event.client.PageChangeEvent;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeEvent;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
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
		void setContentFilter(ContentFilter<TransUnit> filter);
		void clearContentFilter();
		void gotoRow(int row);
		int getCurrentPageNumber();
		TransUnit getTransUnitValue(int row);
		InlineTargetCellEditor getTargetCellEditor();
		List<TransUnit> getRowValues();
		boolean isFirstPage();
		boolean isLastPage();
		int getCurrentPage();
		int getPageSize();
	}

	private DocumentId documentId;

	private final DispatchAsync dispatcher;
	private final WorkspaceContext workspaceContext;
	private final Identity identity;
	private TransUnit selectedTransUnit;
	private int lastRowNum;
	
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

	@Override
	protected void onBind() {
		display.setTableModelHandler(tableModelHandler);
		display.setPageSize(TableConstants.PAGE_SIZE);
		registerHandler(display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				if(event.getSelectedItem() != selectedTransUnit) {
					selectedTransUnit = event.getSelectedItem();
					//startEditing(selectedTransUnit);
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
							display.asWidget().setVisible(true);
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
					// TODO this test never succeeds
					if(selectedTransUnit != null && selectedTransUnit.getId().equals(event.getTransUnitId())) {
						// handle change in current selection
						//eventBus.fireEvent(new NotificationEvent(Severity.Warning, "Someone else updated this translation unit. you're in trouble..."));
						//display.getTableModel().setRowValue(row, rowValue);
						Log.info("selected TU updated; cancelling edit");
						display.getTargetCellEditor().cancelEdit();
						
						// TODO reload page and return
					}
					
					boolean reloadPage = false;
					if (reloadPage) {
						display.getTargetCellEditor().cancelEdit();
						display.getTableModel().clearCache();
						display.reloadPage();
					} else {
						final Integer rowOffset = getRowOffset(event.getTransUnitId());
						// - add TU index to model
						if (rowOffset != null) {
							final int row = display.getCurrentPage() * display.getPageSize() + rowOffset;
							Log.info("row calculated as "+row);
							dispatcher.execute(new GetTransUnits(
								documentId, 
								workspaceContext.getLocaleId(), 
								row, 
								1), new AsyncCallback<GetTransUnitsResult>() {
									@Override
									public void onFailure(Throwable e) {
										Log.error(e.getMessage(), e);
									}
	
									@Override
									public void onSuccess(GetTransUnitsResult result) {
										// FIXME should this be row, rowOffset, or something else?
										display.getTableModel().setRowValueOverride(row, result.getUnits().get(0));
									}
								});
						} else {
							display.getTableModel().clearCache();
						}
					}
				}
			}
		}));
		
		registerHandler(eventBus.addHandler(TransUnitEditEvent.getType(), new TransUnitEditEventHandler() {
			@Override
			public void onTransUnitEdit(TransUnitEditEvent event) {
				if(documentId != null && documentId.equals(event.getDocumentId())) {
					if(selectedTransUnit != null && selectedTransUnit.getId().equals(event.getTransUnitId())) {
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
				if(selectedTransUnit != null) {
					int step = event.getStep();
					//Send message to server to stop editing current selection
					//stopEditing(selectedTransUnit);
					
					InlineTargetCellEditor editor = display.getTargetCellEditor();
					
					//If goto Next or Prev Trans Unit
					if (event.getRowType() == null ) {
						if (step > 0)
							editor.handleNext();
						else
							editor.handlePrev();
					}
					
					//If goto Next or Prev Fuzzy/New Trans Unit
					if (event.getRowType() == ContentState.NeedReview ||
							event.getRowType() == ContentState.New) {
						if (step > 0)
							editor.handleNextState(event.getRowType());
						else
							editor.handlePrevState(event.getRowType());
					}
				}
			}
		}));
		
		registerHandler(eventBus.addHandler(TransMemoryCopyEvent.getType(), new TransMemoryCopyHandler() {
			@Override
			public void onTransMemoryCopy(TransMemoryCopyEvent event) {
				// When user clicked on copy-to-target anchor, it checks
				// if user is editing any target. Notifies if not.
				 if (display.getTargetCellEditor().isEditing()) {
					 display.getTargetCellEditor().setText(event.getTargetResult());
					 eventBus.fireEvent( new NotificationEvent(Severity.Info, "Message has been copied to the target."));
				 }
				 else
					 eventBus.fireEvent( new NotificationEvent(Severity.Error, "Please open the target in the editor first."));
			}
		}));
	
		Event.addNativePreviewHandler(new NativePreviewHandler() {
			@Override
			public void onPreviewNativeEvent(NativePreviewEvent event) {
		    	 //Only when the Table is showed and editor is closed, the keyboard event will be processed. 
				 if(display.asWidget().isVisible() && !display.getTargetCellEditor().isFocused()) {
					 NativeEvent nativeEvent =  event.getNativeEvent();
					 String nativeEventType = nativeEvent.getType();
					 int keyCode = nativeEvent.getKeyCode();
					 boolean shiftKey = nativeEvent.getShiftKey();
					 boolean altKey = nativeEvent.getAltKey();
					 boolean ctrlKey = nativeEvent.getCtrlKey();
					 if(nativeEventType.equals("keypress") && !shiftKey && !altKey && !ctrlKey) {
						  //PageDown key
						  switch(keyCode) {
						  	  case KeyCodes.KEY_PAGEDOWN: 
						  		  Log.info("fired event of type " + event.getAssociatedType().getClass().getName());
						  		  if(!display.isLastPage())
						  			  gotoNextPage();
						  		  event.cancel();
							  break;
							  //PageUp key
							  case KeyCodes.KEY_PAGEUP:
								  Log.info("fired event of type " + event.getAssociatedType().getClass().getName());
								  if(!display.isFirstPage())
									  gotoPreviousPage();
								  event.cancel();
						      break;
						      //Home
							  case KeyCodes.KEY_HOME: 
								  Log.info("fired event of type " + event.getAssociatedType().getClass().getName());
								  display.gotoFirstPage();
								  event.cancel();
							  break;
						      //End
							  case KeyCodes.KEY_END:
								  Log.info("fired event of type " + event.getAssociatedType().getClass().getName());
								  display.gotoLastPage();
								  event.cancel();
								  break;
						      default:
						    	  break;
						  }	  
					 }
				 }
			}
		});
		
		display.gotoFirstPage();
	}

	public Integer getRowOffset(TransUnitId transUnitId) {
		// TODO inefficient!
		for (int i=0; i<display.getRowValues().size(); i++) {
			if (transUnitId.equals(display.getTransUnitValue(i).getId())) {
				Log.info("getRowOffset returning "+i);
				return i;
			}
		}
		return null;
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
					lastRowNum = display.getTableModel().getRowCount()%display.getPageSize();
					if(lastRowNum == 0)
						lastRowNum = MAX_PAGE_ROW;
					Log.info("Last Row of Last Page " + lastRowNum);
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
		public void gotoNextRow(int row) {
			int nextRow = row+1;
			Log.info("Next Row"+nextRow);
			if(!display.isLastPage()) {
				if(nextRow <= MAX_PAGE_ROW && nextRow >= 0) {
					cancelEdit();
					selectedTransUnit = display.getTransUnitValue(nextRow);
					display.gotoRow(nextRow);
				} else if(nextRow > MAX_PAGE_ROW) {
					cancelEdit();
					display.gotoNextPage();
					selectedTransUnit = display.getTransUnitValue(nextRow);
					display.gotoRow(0);
				} 
			} else {
				if (nextRow <= lastRowNum) {
					cancelEdit();
					selectedTransUnit = display.getTransUnitValue(nextRow);
					display.gotoRow(nextRow);
				} 
			}
		}

		@Override
		public void gotoPrevRow(int row) {
			int prevRow = row-1;
			Log.info("Prev Row"+prevRow);
			if(prevRow < MAX_PAGE_ROW && prevRow >= 0) {
				cancelEdit();
				selectedTransUnit = display.getTransUnitValue(prevRow);
				display.gotoRow(prevRow);
			} else if(prevRow < 0) {
				if(!display.isFirstPage()) {
					cancelEdit();
					display.gotoPreviousPage();
					selectedTransUnit = display.getTransUnitValue(MAX_PAGE_ROW);
					display.gotoRow(MAX_PAGE_ROW);
				}
			}
		}
		
		@Override
		public void gotoNextFuzzy(int row, ContentState state) {
			nextFuzzy(row, state);
		}

		@Override
		public void gotoPrevFuzzy(int row, ContentState state) {
			prevFuzzy(row, state);
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
	
	
	private void prevFuzzy(int row, ContentState desiredState) { 
		row=row-1;
		while(row >= 0) {
			if(display.getTransUnitValue(row).getStatus() == desiredState) {
				cancelEdit();
				selectedTransUnit = display.getTransUnitValue(row);
				display.gotoRow(row);
				break;
			}
			else {
				row = row - 1;
			}
		}

		if(row < 0) {
			if(!display.isFirstPage()) {
				cancelEdit();
				display.gotoPreviousPage();
				prevFuzzy(MAX_PAGE_ROW+1, desiredState);
			}
		}
		
	}
	
	private void nextFuzzy(int row, ContentState desiredState) { 
		row=row+1;
		if(!display.isLastPage()) {
			while(row <= MAX_PAGE_ROW) {
				if(display.getTransUnitValue(row).getStatus()==desiredState) {
					cancelEdit();
					selectedTransUnit = display.getTransUnitValue(row);
					display.gotoRow(row);
					break;
				}	
				else {
					row = row + 1;
				}
			} 
			if(row > MAX_PAGE_ROW) {
				cancelEdit();
				display.gotoNextPage();
				nextFuzzy(-1, desiredState);

			}
		} else {
			while(row <= lastRowNum) {
				if(display.getTransUnitValue(row).getStatus()==desiredState) {
					cancelEdit();
					selectedTransUnit = display.getTransUnitValue(row);
					display.gotoRow(row);
					break;
				}	
				else {
					row = row + 1;
				}
			} 
		}
	}
	
	
	public TransUnit getSelectedTransUnit() {
		return selectedTransUnit;
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

	public DocumentId getDocumentId() {
		return documentId;
	}

	public void cancelEdit() {
		display.getTargetCellEditor().cancelEdit();
	}
}
