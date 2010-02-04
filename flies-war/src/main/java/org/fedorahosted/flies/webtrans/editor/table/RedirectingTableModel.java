package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;

public class RedirectingTableModel<RowType> extends MutableTableModel<RowType> {

	private TableModelHandler<RowType> tableModelHandler;
	
	public RedirectingTableModel() {
	}
	
	public RedirectingTableModel(TableModelHandler<RowType> dataSource) {
		this.tableModelHandler = dataSource;
	}
	
	public void onCancel(RowType cellValue) {
		if(tableModelHandler != null)
			tableModelHandler.onCancel(cellValue);
	}
	
	@Override
	protected boolean onRowInserted(int beforeRow) {
		if(tableModelHandler != null)
			return tableModelHandler.onRowInserted(beforeRow);
		return true;
	}

	@Override
	protected boolean onRowRemoved(int row) {
		if(tableModelHandler != null)
			return tableModelHandler.onRowRemoved(row);
		return true;
	}

	@Override
	protected boolean onSetRowValue(int row, RowType rowValue) {
		if(tableModelHandler != null)
			return tableModelHandler.onSetRowValue(row, rowValue);
		return false;
	}

	@Override
	public void requestRows(
			final Request request,
			final Callback<RowType> callback) {
		if(tableModelHandler != null)
			tableModelHandler.requestRows(request, callback);
		else
			callback.onFailure(new RuntimeException("No datasource"));
	}

	public void setTableModelHandler(
			TableModelHandler<RowType> tableModelHandler) {
		this.tableModelHandler = tableModelHandler;
	}
	
	public TableModelHandler<RowType> getTableModelHandler() {
		return tableModelHandler;
	}

	public void gotoRow(int row) {
		if(tableModelHandler != null)
			tableModelHandler.gotoRow(row);
	}
	
	public void gotoNextFuzzy(int row, ContentState state) {
		if(tableModelHandler != null)
			tableModelHandler.gotoNextFuzzy(row, state);
	}
	
	public void gotoPrevFuzzy(int row, ContentState state) {
		if(tableModelHandler != null)
			tableModelHandler.gotoPrevFuzzy(row, state);
	}
}
