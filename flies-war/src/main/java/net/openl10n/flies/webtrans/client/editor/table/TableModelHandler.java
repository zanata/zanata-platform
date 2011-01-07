package net.openl10n.flies.webtrans.client.editor.table;

import net.openl10n.flies.common.NavigationType;

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

   abstract void gotoNextRow(int row);

   abstract void gotoPrevRow(int row);

   abstract void nextFuzzyIndex(int row, NavigationType state);

   abstract void prevFuzzyIndex(int row, NavigationType state);

}
