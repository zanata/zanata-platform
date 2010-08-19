package org.fedorahosted.flies.webtrans.client.editor;

import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.webtrans.shared.model.TransUnit;
import org.gwt.mosaic.ui.client.event.HasRowSelectionHandlers;
import org.gwt.mosaic.ui.client.event.RowSelectionHandler;
import org.gwt.mosaic.ui.client.table.FixedWidthGridBulkRenderer;
import org.gwt.mosaic.ui.client.table.PagingScrollTable;
import org.gwt.mosaic.ui.client.table.ScrollTable;
import org.gwt.mosaic.ui.client.table.SelectionGrid.SelectionPolicy;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ListEditorTable extends PagingScrollTable<TransUnit> implements ListEditorPresenter.Display, HasRowSelectionHandlers, HasPageNavigation
{

   @Inject
   public ListEditorTable(final CachedListEditorTableModel tableModel, final EventBus eventBus, final ListEditorTableDefinition tableDefinition)
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
   public boolean isFirstPage()
   {
      return getCurrentPage() == 0;
   }

   @Override
   public boolean isLastPage()
   {
      return getCurrentPage() == getPageCount() - 1;
   }

   @Override
   public HandlerRegistration addRowSelectionHandler(RowSelectionHandler handler)
   {
      return getDataTable().addRowSelectionHandler(handler);
   }

   @Override
   public void selectRow(int row)
   {
      getDataTable().selectRow(row, true);
   }

   @Override
   public int getSelectedRow()
   {
      Set<Integer> selectedRows = getDataTable().getSelectedRows();
      if(selectedRows.isEmpty())
         return -1;
      return selectedRows.iterator().next();
   }

   @Override
   public int getRowCount()
   {
      return getDataTable().getRowCount();
   }

}
