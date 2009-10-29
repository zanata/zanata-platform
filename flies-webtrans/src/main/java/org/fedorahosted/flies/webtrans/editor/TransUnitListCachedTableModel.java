package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.gen2.table.client.CachedTableModel;
import com.google.gwt.gen2.table.event.client.RowCountChangeEvent;
import com.google.gwt.gen2.table.event.client.RowCountChangeHandler;
import com.google.inject.Inject;

/**
 * A TableModel that caches 200 rows pre and post the current page.
 * @author asgeirf
 *
 */
public class TransUnitListCachedTableModel extends CachedTableModel<TransUnit>{

	private final TransUnitListEditorTableModel tableModel;

	public TransUnitListCachedTableModel(TransUnitListEditorTableModel tableModel) {
		super(tableModel);
		this.tableModel = tableModel;
		setPreCachedRowCount(200);
		setPostCachedRowCount(200);
		tableModel.addRowCountChangeHandler(new RowCountChangeHandler() {
			
			@Override
			public void onRowCountChange(RowCountChangeEvent event) {
				setRowCount(event.getNewRowCount());
			}
		});
	}
	
	public TransUnitListEditorTableModel getTableModel() {
		return tableModel;
	}

}
