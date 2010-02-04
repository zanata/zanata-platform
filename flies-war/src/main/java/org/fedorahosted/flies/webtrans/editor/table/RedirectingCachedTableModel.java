package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.gen2.table.client.CachedTableModel;

public class RedirectingCachedTableModel<RowType> extends CachedTableModel<RowType>{
	
	private final RedirectingTableModel<RowType> tableModel;
	
	public RedirectingCachedTableModel(RedirectingTableModel<RowType> tableModel) {
		super(tableModel);
		this.tableModel = tableModel;
	}
	
	public RedirectingTableModel<RowType> getTableModel() {
		return tableModel;
	}

	public void onCancel(RowType cellValue) {
		if(tableModel != null)
			tableModel.onCancel(cellValue);
	}

	public void gotoRow(int row) {
		if(tableModel != null)
			tableModel.gotoRow(row);
	}
	
	public void gotoNextFuzzy(int row, ContentState state) {
		if(tableModel != null)
			tableModel.gotoNextFuzzy(row, state);
	}
	
	public void gotoPrevFuzzy(int row, ContentState state) {
		if(tableModel != null)
			tableModel.gotoPrevFuzzy(row, state);
	}

}
