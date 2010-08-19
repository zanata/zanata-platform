package net.openl10n.flies.webtrans.client.editor;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import net.openl10n.flies.webtrans.client.editor.TransUnitRowEditor.Callback;
import net.openl10n.flies.webtrans.client.editor.TransUnitRowEditor.RowEditInfo;
import net.openl10n.flies.webtrans.client.events.DocumentSelectionEvent;
import net.openl10n.flies.webtrans.client.events.DocumentSelectionHandler;
import net.openl10n.flies.webtrans.client.events.FilterDisabledEvent;
import net.openl10n.flies.webtrans.client.events.FilterDisabledEventHandler;
import net.openl10n.flies.webtrans.client.events.FilterEnabledEvent;
import net.openl10n.flies.webtrans.client.events.FilterEnabledEventHandler;
import net.openl10n.flies.webtrans.client.events.NotificationEvent;
import net.openl10n.flies.webtrans.client.events.TransMemoryCopyEvent;
import net.openl10n.flies.webtrans.client.events.TransMemoryCopyHandler;
import net.openl10n.flies.webtrans.client.events.TransUnitEditEvent;
import net.openl10n.flies.webtrans.client.events.TransUnitEditEventHandler;
import net.openl10n.flies.webtrans.client.events.TransUnitSelectionEvent;
import net.openl10n.flies.webtrans.client.events.TransUnitUpdatedEvent;
import net.openl10n.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import net.openl10n.flies.webtrans.client.events.NotificationEvent.Severity;
import net.openl10n.flies.webtrans.shared.auth.Identity;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnit;

import org.gwt.mosaic.override.client.HTMLTable;
import org.gwt.mosaic.ui.client.event.HasPageChangeHandlers;
import org.gwt.mosaic.ui.client.event.HasPageCountChangeHandlers;
import org.gwt.mosaic.ui.client.event.HasPageLoadHandlers;
import org.gwt.mosaic.ui.client.event.HasPagingFailureHandlers;
import org.gwt.mosaic.ui.client.event.HasRowSelectionHandlers;
import org.gwt.mosaic.ui.client.event.PageChangeHandler;
import org.gwt.mosaic.ui.client.event.PageCountChangeEvent;
import org.gwt.mosaic.ui.client.event.PageCountChangeHandler;
import org.gwt.mosaic.ui.client.event.RowSelectionEvent;
import org.gwt.mosaic.ui.client.event.RowSelectionHandler;
import org.gwt.mosaic.ui.client.layout.HasLayoutManager;
import org.gwt.mosaic.ui.client.table.HasTableDefinition;
import org.gwt.mosaic.ui.client.table.TableModel;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.HasScrollHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.inject.Inject;

public class ListEditorPresenter extends WidgetPresenter<ListEditorPresenter.Display> implements HasPageNavigation, HasPageChangeHandlers, HasPageCountChangeHandlers
{

   static final int PAGE_SIZE = 50;
   public static final Place PLACE = new Place("TableEditor");

   public interface Display extends WidgetDisplay, HasLayoutManager, HasScrollHandlers, HasTableDefinition<TransUnit>, HasPageCountChangeHandlers, HasPageLoadHandlers, HasPageChangeHandlers, HasPagingFailureHandlers, HasPageNavigation, HasRowSelectionHandlers
   {

      boolean isFirstPage();

      boolean isLastPage();

      int getCurrentPage();

      void setPageSize(int size);

      public TransUnit getRowValue(int row);
      
      public void selectRow(int row);
      public int getSelectedRow();
      public int getRowCount();
      
   }

   private DocumentId selectedDocumentId;
   private TransUnit selectedTransUnit;

   private final Identity identity;
   private final ListEditorMessages messages;
   private final CachedListEditorTableModel tableModel;
   private final ListEditorTableDefinition tableDefinition;
   private final TransUnitRowEditor rowEditor;

   @Inject
   public ListEditorPresenter(final Display display, final EventBus eventBus, final Identity identity, final TransUnitRowEditor rowEditor, final ListEditorMessages messages, final CachedListEditorTableModel tableModel, final ListEditorTableDefinition tableDefinition)
   {
      super(display, eventBus);

      this.identity = identity;
      this.messages = messages;
      this.tableModel = tableModel;
      this.tableDefinition = tableDefinition;
      this.rowEditor = rowEditor;
   }

   @Override
   public Place getPlace()
   {
      return PLACE;
   }

   private final Callback editRowCallback = new Callback()
   {

      @Override
      public void onSave(RowEditInfo rowEditInfo, TransUnit rowValue)
      {
         tableModel.setRowValue(rowEditInfo.getIndex(), rowValue);
      }

      @Override
      public void onCancel(RowEditInfo rowEditInfo)
      {
      }
   };

   @Override
   protected void onBind()
   {
      display.setPageSize(PAGE_SIZE);
      registerHandler(display.addRowSelectionHandler(new RowSelectionHandler()
      {

         @Override
         public void onRowSelection(RowSelectionEvent event)
         {
            if (!event.getSelectedRows().isEmpty())
            {
               int index = event.getSelectedRows().iterator().next().getRowIndex();
               selectedTransUnit = display.getRowValue(index);
               Log.info("SelectedTransUnit " + selectedTransUnit.getId());
               eventBus.fireEvent(new TransUnitSelectionEvent(selectedTransUnit));

               RowEditInfo editInfo = new RowEditInfo((HTMLTable) event.getSource(), index);
               rowEditor.editCell(editInfo, selectedTransUnit, editRowCallback);
            }
         }
      }));

      registerHandler(rowEditor.addKeyUpHandler(new KeyUpHandler()
      {
         
         @Override
         public void onKeyUp(KeyUpEvent event)
         {
            // NB: if you change these, please change NavigationConsts too!
            if (event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
            {
               rowEditor.release();
            }
            else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE)
            {
               rowEditor.cancelEdit();
            }
            else if (event.isControlKeyDown() && event.isShiftKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN)
            { // was alt-e
              // handleNextState(ContentState.NeedReview);
            }
            else if (event.isControlKeyDown() && event.isShiftKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
            { // was alt-m
              // handlePrevState(ContentState.NeedReview);
              // } else if(event.isControlKeyDown() && event.getNativeKeyCode()
              // == KeyCodes.KEY_PAGEDOWN) { // bad in Firefox
            }
            else if (event.isAltKeyDown() && event.isDownArrow())
            {
               int row = display.getSelectedRow();
               int rowCount = display.getRowCount();
               if(row < rowCount-1)
               {
                  rowEditor.release();
                  display.selectRow(row+1);
               }
            }
            else if (event.isAltKeyDown() && event.isUpArrow())
            {
               // handlePrev();
            }
            else if (event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN)
            { // alt-down
              // handleNextState(ContentState.New);
            }
            else if (event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
            { // alt-up
              // handlePrevState(ContentState.New);
            }
            
         }
      }));
      
      registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler()
      {
         @Override
         public void onDocumentSelected(DocumentSelectionEvent event)
         {
            if (!event.getDocument().getId().equals(selectedDocumentId))
            {
               rowEditor.release();
               display.startProcessing();
               selectedDocumentId = event.getDocument().getId();
               tableModel.clearCache();
               tableModel.setRowCount(TableModel.UNKNOWN_ROW_COUNT);
               display.gotoPage(0, true);
               display.stopProcessing();
            }
         }
      }));

      registerHandler(eventBus.addHandler(FilterEnabledEvent.getType(), new FilterEnabledEventHandler()
      {
         @Override
         public void onFilterEnabled(FilterEnabledEvent event)
         {
            tableDefinition.setContentFilter(event.getContentFilter());
         }
      }));

      registerHandler(eventBus.addHandler(FilterDisabledEvent.getType(), new FilterDisabledEventHandler()
      {

         @Override
         public void onFilterDisabled(FilterDisabledEvent event)
         {
            tableDefinition.clearContentFilter();
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitEditEvent.getType(), new TransUnitEditEventHandler()
      {
         @Override
         public void onTransUnitEdit(TransUnitEditEvent event)
         {
            if (selectedDocumentId != null && selectedDocumentId.equals(event.getDocumentId()))
            {
               if (selectedTransUnit != null && selectedTransUnit.getId().equals(event.getTransUnitId()))
               {
                  // handle change in current selection
                  if (!event.getSessionId().equals(identity.getSessionId()))
                     eventBus.fireEvent(new NotificationEvent(Severity.Warning, messages.notifyInEdit()));
               }
            }
         }
      }));

      registerHandler(eventBus.addHandler(TransMemoryCopyEvent.getType(), new TransMemoryCopyHandler()
      {
         @Override
         public void onTransMemoryCopy(TransMemoryCopyEvent event)
         {
            
         }
      }));

      Event.addNativePreviewHandler(new NativePreviewHandler()
      {
         @Override
         public void onPreviewNativeEvent(NativePreviewEvent event)
         {
            return;
            /*
             * if (display.asWidget().isVisible()) { NativeEvent nativeEvent =
             * event.getNativeEvent(); String nativeEventType =
             * nativeEvent.getType(); int keyCode = nativeEvent.getKeyCode();
             * boolean shiftKey = nativeEvent.getShiftKey(); boolean altKey =
             * nativeEvent.getAltKey(); boolean ctrlKey =
             * nativeEvent.getCtrlKey(); if (nativeEventType.equals("keypress")
             * && !shiftKey && !altKey && !ctrlKey) {
             * Log.info("fired event of type " +
             * event.getAssociatedType().getClass().getName()); // PageDown key
             * switch (keyCode) { case KeyCodes.KEY_PAGEDOWN: case
             * KeyCodes.KEY_PAGEUP: case KeyCodes.KEY_HOME: case
             * KeyCodes.KEY_END: default: break; } } }
             */
         }
      });

      display.gotoFirstPage();
   }

   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }

   @Override
   protected void onPlaceRequest(PlaceRequest request)
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void refreshDisplay()
   {
   }

   @Override
   public void revealDisplay()
   {
   }

   @Override
   public void gotoFirstPage()
   {
      if (display.getCurrentPage() != 0)
      {
         rowEditor.release();
         display.gotoFirstPage();
      }

   }

   @Override
   public void gotoLastPage()
   {
      if (!display.isLastPage())
      {
         rowEditor.release();
         display.gotoLastPage();
      }
   }

   @Override
   public void gotoNextPage()
   {
      if(!display.isLastPage())
      {
         rowEditor.release();
         display.gotoNextPage();
      }
   }

   @Override
   public void gotoPage(int page, boolean forced)
   {
      if(display.getCurrentPage() != page)
      {
         rowEditor.release();
         display.gotoPage(page, forced);
      }
   }

   @Override
   public void gotoPreviousPage()
   {
      if(!display.isFirstPage())
      {
         rowEditor.release();
         display.gotoPreviousPage();
      }
   }

   @Override
   public HandlerRegistration addPageChangeHandler(PageChangeHandler handler)
   {
      return display.addPageChangeHandler(handler);
   }

   @Override
   public void fireEvent(GwtEvent<?> event)
   {
      eventBus.fireEvent(event);
   }

   @Override
   public HandlerRegistration addPageCountChangeHandler(PageCountChangeHandler handler)
   {
      return eventBus.addHandler(PageCountChangeEvent.getType(), handler);
   }
}
