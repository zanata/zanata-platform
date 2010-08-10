package org.fedorahosted.flies.webtrans.client.editor;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.webtrans.client.events.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.events.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.events.NotificationEvent;
import org.fedorahosted.flies.webtrans.client.events.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnit;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTransUnits;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTransUnitsResult;
import org.gwt.mosaic.ui.client.table.AbstractMutableTableModel;
import org.gwt.mosaic.ui.client.table.TableModelHelper.Request;
import org.gwt.mosaic.ui.client.table.TableModelHelper.SerializableResponse;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ListEditorTableModel extends AbstractMutableTableModel<TransUnit>
{

   private DocumentId selectedDocumentId;
   private final DispatchAsync dispatcher;
   private final EventBus eventBus;
   private final ListEditorMessages messages;

   @Inject
   public ListEditorTableModel(final EventBus eventBus, final CachingDispatchAsync dispatcher, final ListEditorMessages messages)
   {
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.messages = messages;

      eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler()
      {
         @Override
         public void onDocumentSelected(DocumentSelectionEvent event)
         {
            if (!event.getDocument().getId().equals(selectedDocumentId))
            {
               selectedDocumentId = event.getDocument().getId();
               
            }
         }
      });

   }

   @Override
   public void requestRows(final Request request, final org.gwt.mosaic.ui.client.table.TableModel.Callback<TransUnit> callback)
   {

      int numRows = request.getNumRows();
      int startRow = request.getStartRow();

      if (selectedDocumentId == null)
      {
         callback.onFailure(new RuntimeException("No Document Selected"));
         return;
      }

      Log.info("Table requesting " + numRows + " starting from " + startRow);

      dispatcher.execute(new GetTransUnits(selectedDocumentId, startRow, numRows), new AsyncCallback<GetTransUnitsResult>()
      {
         @Override
         public void onSuccess(GetTransUnitsResult result)
         {
            SerializableResponse<TransUnit> response = new SerializableResponse<TransUnit>(result.getUnits());
            Log.debug("Got " + result.getUnits().size() + " rows back");
            callback.onRowsReady(request, response);
            Log.info("Total of " + result.getTotalCount() + " rows available");
            setRowCount(result.getTotalCount());
         }

         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("GetTransUnits failure " + caught, caught);
            eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUnknownError()));
         }
      });

   }

   @Override
   public boolean onRowInserted(int beforeRow)
   {
      return false;
   }

   @Override
   public boolean onRowRemoved(int row)
   {
      return false;
   }

   @Override
   public boolean onSetRowValue(int row, TransUnit rowValue)
   {
      eventBus.fireEvent(new NotificationEvent(Severity.Info, "Saved entry.."));
      rowValue.setTarget( "(SAVED) " + rowValue.getTarget());
      return true;
   }

}
