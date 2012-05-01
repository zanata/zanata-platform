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

   public void gotoNextRow()
   {
      if (tableModel != null)
         tableModel.gotoNextRow();
   }

   public void gotoPrevRow()
   {
      if (tableModel != null)
         tableModel.gotoPrevRow();
   }

   public void gotoFirstRow()
   {
      if (tableModel != null)
         tableModel.gotoFirstRow();
   }

   public void gotoLastRow()
   {
      if (tableModel != null)
         tableModel.gotoLastRow();
   }

   public void gotoNextFuzzyNew()
   {
      if (tableModel != null)
         tableModel.gotoNextFuzzyNew();
   }

   public void gotoPrevFuzzyNew()
   {
      if (tableModel != null)
         tableModel.gotoPrevFuzzyNew();
   }

   public void gotoNextFuzzy()
   {
      if (tableModel != null)
         tableModel.gotoNextFuzzy();
   }

   public void gotoPrevFuzzy()
   {
      if (tableModel != null)
         tableModel.gotoPrevFuzzy();
   }

   public void gotoNextNew()
   {
      if (tableModel != null)
         tableModel.gotoNextNew();
   }

   public void gotoPrevNew()
   {
      if (tableModel != null)
         tableModel.gotoPrevNew();
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
