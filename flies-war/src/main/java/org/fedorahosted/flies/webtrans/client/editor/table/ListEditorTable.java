package org.fedorahosted.flies.webtrans.client.editor.table;

import java.util.List;

import org.fedorahosted.flies.webtrans.client.editor.HasPageNavigation;
import org.fedorahosted.flies.webtrans.shared.model.TransUnit;
import org.gwt.mosaic.ui.client.event.RowSelectionEvent;
import org.gwt.mosaic.ui.client.event.RowSelectionHandler;
import org.gwt.mosaic.ui.client.event.TableEvent.Row;
import org.gwt.mosaic.ui.client.table.FixedWidthGridBulkRenderer;
import org.gwt.mosaic.ui.client.table.PagingScrollTable;
import org.gwt.mosaic.ui.client.table.ScrollTable;
import org.gwt.mosaic.ui.client.table.SelectionGrid.SelectionPolicy;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ListEditorTable extends PagingScrollTable<TransUnit> implements ListEditorPresenter.Display, HasSelectionHandlers<TransUnit>, HasPageNavigation
{

   @Inject
   public ListEditorTable(CachedListEditorTableModel tableModel, ListEditorTableDefinition tableDefinition)
   {
      super(tableModel, tableDefinition);

      setStylePrimaryName("TableEditorWrapper");
      setPageSize(10);
      setEmptyTableWidget(new HTML("There is no data to display"));

      // Setup the bulk renderer
      FixedWidthGridBulkRenderer<TransUnit> bulkRenderer = new FixedWidthGridBulkRenderer<TransUnit>(getDataTable(), this);
      setBulkRenderer(bulkRenderer);

      // Setup the formatting
      setCellPadding(3);
      setCellSpacing(0);
      setResizePolicy(ScrollTable.ResizePolicy.FILL_WIDTH);

      getDataTable().setStylePrimaryName("TableEditor");
      getDataTable().setSelectionPolicy(SelectionPolicy.ONE_ROW);
      getDataTable().setCellPadding(3);
      getDataTable().addRowSelectionHandler(new RowSelectionHandler()
      {
         @Override
         public void onRowSelection(RowSelectionEvent event)
         {
            if (!event.getSelectedRows().isEmpty())
            {
               Row row = event.getSelectedRows().iterator().next();
               TransUnit tu = getRowValue(row.getRowIndex());
               SelectionEvent.fire(ListEditorTable.this, tu);
            }
         }
      });
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void startProcessing()
   {
      Log.info("start processing...");
      setVisible(false);
   }

   @Override
   public void stopProcessing()
   {
      Log.info("end processing...");
      setVisible(true);
   }

   @Override
   public HandlerRegistration addSelectionHandler(SelectionHandler<TransUnit> handler)
   {
      return addHandler(handler, SelectionEvent.getType());
   }

   @Override
   public HasSelectionHandlers<TransUnit> getSelectionHandlers()
   {
      return this;
   }

   @Override
   public boolean isFirstPage()
   {
      return getCurrentPage() == 0;
   }

   @Override
   public boolean isLastPage()
   {
      return getCurrentPage() == getPageCount() - 1;
   }

}
