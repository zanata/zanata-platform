package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.model.TransUnit;

import com.google.gwt.gen2.table.client.CachedTableModel;
import com.google.inject.Inject;

public class CachedTransUnitTableModel extends CachedTableModel<TransUnit>{
	
	@Inject
	public CachedTransUnitTableModel(TransUnitTableModel tableModel) {
		super(tableModel);
		setPreCachedRowCount(50);
		setPostCachedRowCount(50);
		setRowCount(1000);
	}

}
