package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.model.TransUnitId;

import com.google.gwt.gen2.table.client.CachedTableModel;

public class RedirectingCachedTableModel<RowType> extends CachedTableModel<RowType>{
	
	private final RedirectingTableModel<RowType> tableModel;
	private boolean quiet = false;
	
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
	
	public void setRowValueOverride(int row, RowType rowValue) {
		// TODO ideally, we would just replace the affected row in the cache
		clearCache();
		quiet = true;
		try {
			setRowValue(row, rowValue);
		} finally {
			quiet = false;
		}
		
	}
	
	protected final boolean onSetRowValue(int row, RowType rowValue) {
		if (quiet)
			return true;
		return super.onSetRowValue(row, rowValue);
	}

}
