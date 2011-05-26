package org.zanata.webtrans.client.editor.table;


import com.google.gwt.gen2.table.client.CachedTableModel;

public class RedirectingCachedTableModel<RowType> extends CachedTableModel<RowType>
{

   private final RedirectingTableModel<RowType> tableModel;
   private boolean quiet = false;

   public RedirectingCachedTableModel(RedirectingTableModel<RowType> tableModel)
   {
      super(tableModel);
      this.tableModel = tableModel;
   }

   public RedirectingTableModel<RowType> getTableModel()
   {
      return tableModel;
   }

   public void onCancel(RowType cellValue)
   {
      if (tableModel != null)
         tableModel.onCancel(cellValue);
   }

   public void gotoNextRow(int row)
   {
      if (tableModel != null)
         tableModel.gotoNextRow(row);
   }

   public void gotoPrevRow(int row)
   {
      if (tableModel != null)
         tableModel.gotoPrevRow(row);
   }

   public void gotoNextFuzzy(int row)
   {
      if (tableModel != null)
         tableModel.gotoNextFuzzy(row);
   }

   public void gotoPrevFuzzy(int row)
   {
      if (tableModel != null)
         tableModel.gotoPrevFuzzy(row);
   }

   public void setRowValueOverride(int row, RowType rowValue)
   {
      // TODO ideally, we would just replace the affected row in the cache
      clearCache();
      quiet = true;
      try
      {
         setRowValue(row, rowValue);
      }
      finally
      {
         quiet = false;
      }

   }

   @Override
   protected final boolean onSetRowValue(int row, RowType rowValue)
   {
      if (quiet)
         return true;
      return super.onSetRowValue(row, rowValue);
   }

   public void setRowCount(int rowCount)
   {
      super.setRowCount(rowCount);
   }

}
