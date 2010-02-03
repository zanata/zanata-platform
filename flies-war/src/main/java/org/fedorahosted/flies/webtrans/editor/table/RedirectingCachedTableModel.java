package org.fedorahosted.flies.webtrans.editor.table;

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

}
