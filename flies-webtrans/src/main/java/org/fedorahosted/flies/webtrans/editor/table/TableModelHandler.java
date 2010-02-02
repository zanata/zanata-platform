package org.fedorahosted.flies.webtrans.editor.table;

import com.google.gwt.gen2.table.client.TableModel.Callback;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;

public abstract class TableModelHandler<RowType> {
	abstract void requestRows(final Request request,	final Callback<RowType> callback);
	abstract boolean onSetRowValue(int row, RowType rowValue);
	abstract void onCancel(RowType cellValue);
	protected boolean onRowInserted(int beforeRow) {
		return false;
	}

	protected boolean onRowRemoved(int row) {
		return true;
	}
	
	abstract void gotoRow(int row);
	abstract void gotoNextFuzzy(int row);
	abstract void gotoPrevFuzzy(int row);

}
