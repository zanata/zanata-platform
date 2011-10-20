package org.zanata.webtrans.client.editor.table;

import com.google.gwt.gen2.table.client.TableModel.Callback;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;

public abstract class TableModelHandler<RowType>
{
   abstract void requestRows(final Request request, final Callback<RowType> callback);

   abstract boolean onSetRowValue(int row, RowType rowValue);

   abstract void onCancel(RowType cellValue);

   protected boolean onRowInserted(int beforeRow)
   {
      return false;
   }

   protected boolean onRowRemoved(int row)
   {
      return true;
   }

   abstract void gotoNextRow(boolean andEdit);

   abstract void gotoPrevRow(boolean andEdit);

   abstract void gotoFirstRow();

   abstract void gotoLastRow();

   abstract void nextFuzzyNewIndex();

   abstract void prevFuzzyNewIndex();

   abstract void nextFuzzyIndex();

   abstract void prevFuzzyIndex();

   abstract void nextNewIndex();

   abstract void prevNewIndex();

   abstract void gotoRow(int row);

   abstract void gotoRow(int rowIndex, boolean andEdit);

   abstract void updatePageAndRowIndex();

   abstract void updateRowIndex(int curPage);
}
