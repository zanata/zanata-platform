package org.zanata.webtrans.client.editor.table;


import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;

public class RedirectingTableModel<RowType> extends MutableTableModel<RowType>
{

   private TableModelHandler<RowType> tableModelHandler;

   public RedirectingTableModel()
   {
   }

   public RedirectingTableModel(TableModelHandler<RowType> dataSource)
   {
      this.tableModelHandler = dataSource;
   }

   public void onCancel(RowType cellValue)
   {
      if (tableModelHandler != null)
         tableModelHandler.onCancel(cellValue);
   }

   @Override
   protected boolean onRowInserted(int beforeRow)
   {
      if (tableModelHandler != null)
         return tableModelHandler.onRowInserted(beforeRow);
      return true;
   }

   @Override
   protected boolean onRowRemoved(int row)
   {
      if (tableModelHandler != null)
         return tableModelHandler.onRowRemoved(row);
      return true;
   }

   @Override
   protected boolean onSetRowValue(int row, RowType rowValue)
   {
      if (tableModelHandler != null)
         return tableModelHandler.onSetRowValue(row, rowValue);
      return false;
   }

   @Override
   public void requestRows(final Request request, final Callback<RowType> callback)
   {
      if (tableModelHandler != null)
         tableModelHandler.requestRows(request, callback);
      else
         callback.onFailure(new RuntimeException("No datasource"));
   }

   public void setTableModelHandler(TableModelHandler<RowType> tableModelHandler)
   {
      this.tableModelHandler = tableModelHandler;
   }

   public TableModelHandler<RowType> getTableModelHandler()
   {
      return tableModelHandler;
   }

   public void gotoNextRow(int row)
   {
      if (tableModelHandler != null)
         tableModelHandler.gotoNextRow(row);
   }

   public void gotoPrevRow(int row)
   {
      if (tableModelHandler != null)
         tableModelHandler.gotoPrevRow(row);
   }

   public void gotoNextFuzzyNew(int row)
   {
      if (tableModelHandler != null)
         tableModelHandler.nextFuzzyNewIndex(row);
   }

   public void gotoPrevFuzzyNew(int row)
   {
      if (tableModelHandler != null)
         tableModelHandler.prevFuzzyNewIndex(row);
   }

   public void gotoNextFuzzy(int row)
   {
      if (tableModelHandler != null)
         tableModelHandler.nextFuzzyIndex(row);
   }

   public void gotoPrevFuzzy(int row)
   {
      if (tableModelHandler != null)
         tableModelHandler.prevFuzzyIndex(row);
   }

   public void gotoNextNew(int row)
   {
      if (tableModelHandler != null)
         tableModelHandler.nextNewIndex(row);
   }

   public void gotoPrevNew(int row)
   {
      if (tableModelHandler != null)
         tableModelHandler.prevNewIndex(row);
   }
}
