package org.fedorahosted.flies.webtrans.client.editor;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.client.events.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.events.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.events.FilterDisabledEvent;
import org.fedorahosted.flies.webtrans.client.events.FilterDisabledEventHandler;
import org.fedorahosted.flies.webtrans.client.events.FilterEnabledEvent;
import org.fedorahosted.flies.webtrans.client.events.FilterEnabledEventHandler;
import org.fedorahosted.flies.webtrans.client.events.NotificationEvent;
import org.fedorahosted.flies.webtrans.client.events.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.client.events.TransMemoryCopyEvent;
import org.fedorahosted.flies.webtrans.client.events.TransMemoryCopyHandler;
import org.fedorahosted.flies.webtrans.client.events.TransUnitEditEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitEditEventHandler;
import org.fedorahosted.flies.webtrans.client.events.TransUnitSelectionEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.fedorahosted.flies.webtrans.shared.auth.Identity;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnit;
import org.gwt.mosaic.ui.client.event.HasPageChangeHandlers;
import org.gwt.mosaic.ui.client.event.HasPageCountChangeHandlers;
import org.gwt.mosaic.ui.client.event.HasPageLoadHandlers;
import org.gwt.mosaic.ui.client.event.HasPagingFailureHandlers;
import org.gwt.mosaic.ui.client.event.PageChangeHandler;
import org.gwt.mosaic.ui.client.event.PageCountChangeEvent;
import org.gwt.mosaic.ui.client.event.PageCountChangeHandler;
import org.gwt.mosaic.ui.client.layout.HasLayoutManager;
import org.gwt.mosaic.ui.client.table.HasTableDefinition;
import org.gwt.mosaic.ui.client.table.TableModel;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.HasScrollHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
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

   public interface Display extends WidgetDisplay, HasLayoutManager, HasScrollHandlers, HasTableDefinition<TransUnit>, HasPageCountChangeHandlers, HasPageLoadHandlers, HasPageChangeHandlers, HasPagingFailureHandlers, HasSelectionHandlers<TransUnit>, HasPageNavigation
   {

      HasSelectionHandlers<TransUnit> getSelectionHandlers();

      boolean isFirstPage();

      boolean isLastPage();

      int getCurrentPage();

      void setPageSize(int size);

   }

   private DocumentId selectedDocumentId;
   private TransUnit selectedTransUnit;

   private final Identity identity;
   private final ListEditorMessages messages;
   private final CachedListEditorTableModel tableModel;
   private final ListEditorTableDefinition tableDefinition;

   @Inject
   public ListEditorPresenter(final Display display, final EventBus eventBus, final Identity identity, final ListEditorMessages messages, final CachedListEditorTableModel tableModel, final ListEditorTableDefinition tableDefinition)
   {
      super(display, eventBus);

      this.identity = identity;
      this.messages = messages;
      this.tableModel = tableModel;
      this.tableDefinition = tableDefinition;
   }

   @Override
   public Place getPlace()
   {
      return PLACE;
   }

   @Override
   protected void onBind()
   {
      display.setPageSize(PAGE_SIZE);
      registerHandler(display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<TransUnit>()
      {
         @Override
         public void onSelection(SelectionEvent<TransUnit> event)
         {
            if (selectedTransUnit == null || !event.getSelectedItem().getId().equals(selectedTransUnit.getId()))
            {
               selectedTransUnit = event.getSelectedItem();
               Log.info("SelectedTransUnit " + selectedTransUnit.getId());
               eventBus.fireEvent(new TransUnitSelectionEvent(selectedTransUnit));
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
            if (display.asWidget().isVisible())
            {
               NativeEvent nativeEvent = event.getNativeEvent();
               String nativeEventType = nativeEvent.getType();
               int keyCode = nativeEvent.getKeyCode();
               boolean shiftKey = nativeEvent.getShiftKey();
               boolean altKey = nativeEvent.getAltKey();
               boolean ctrlKey = nativeEvent.getCtrlKey();
               if (nativeEventType.equals("keypress") && !shiftKey && !altKey && !ctrlKey)
               {
                  Log.info("fired event of type " + event.getAssociatedType().getClass().getName());
                  // PageDown key
                  switch (keyCode)
                  {
                  case KeyCodes.KEY_PAGEDOWN:
                  case KeyCodes.KEY_PAGEUP:
                  case KeyCodes.KEY_HOME:
                  case KeyCodes.KEY_END:
                  default:
                     break;
                  }
               }
            }*/
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
      display.gotoFirstPage();
   }

   @Override
   public void gotoLastPage()
   {
      display.gotoLastPage();
   }

   @Override
   public void gotoNextPage()
   {
      display.gotoNextPage();
   }

   @Override
   public void gotoPage(int page, boolean forced)
   {
      display.gotoPage(page, forced);
   }

   @Override
   public void gotoPreviousPage()
   {
      display.gotoPreviousPage();
   }

   @Override
   public HandlerRegistration addPageChangeHandler(PageChangeHandler handler)
   {
      return display.addPageChangeHandler(handler);
   }

   public DocumentId getDocumentId()
   {
      return selectedDocumentId;
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
