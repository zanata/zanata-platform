package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.gen2.table.client.CachedTableModel;
import com.google.gwt.gen2.table.event.client.RowCountChangeEvent;
import com.google.gwt.gen2.table.event.client.RowCountChangeHandler;
import com.google.inject.Inject;

public class CachedWebTransTableModel extends CachedTableModel<TransUnit>{
	
	@Inject
	public CachedWebTransTableModel(WebTransTableModel tableModel) {
		super(tableModel);
		setPreCachedRowCount(200);
		setPostCachedRowCount(200);
		tableModel.addRowCountChangeHandler(new RowCountChangeHandler() {
			
			@Override
			public void onRowCountChange(RowCountChangeEvent event) {
				setRowCount(event.getNewRowCount());
			}
		});
	}
	

}
