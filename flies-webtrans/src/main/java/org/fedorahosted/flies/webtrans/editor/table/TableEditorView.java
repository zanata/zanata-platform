package org.fedorahosted.flies.webtrans.editor.table;


import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.editor.HasPageNavigation;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.gen2.table.client.CachedTableModel;
import com.google.gwt.gen2.table.client.FixedWidthGridBulkRenderer;
import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.client.PagingScrollTable;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.gen2.table.client.TableModel;
import com.google.gwt.gen2.table.client.SelectionGrid.SelectionPolicy;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
import com.google.gwt.gen2.table.event.client.RowCountChangeEvent;
import com.google.gwt.gen2.table.event.client.RowCountChangeHandler;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.gen2.table.event.client.TableEvent.Row;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.weborient.codemirror.client.ParserSyntax;
import com.weborient.codemirror.client.SyntaxToggleWidget;

public class TableEditorView extends PagingScrollTable<TransUnit> implements
		TableEditorPresenter.Display, HasSelectionHandlers<TransUnit>, HasPageNavigation{

	private final RedirectingCachedTableModel<TransUnit> cachedTableModel;
	private int cachedPages = 2;
	
	public TableEditorView() {
		this(new RedirectingTableModel<TransUnit>());
	}
	
	public TableEditorView(RedirectingTableModel<TransUnit> tableModel) {
		this(new RedirectingCachedTableModel<TransUnit>(tableModel), new TableEditorTableDefinition());
		
	}
	
	public TableEditorView(RedirectingCachedTableModel<TransUnit> tableModel, TableEditorTableDefinition tableDefinition) {
		super(tableModel,tableDefinition);
		this.cachedTableModel = tableModel;
		setSize("100%", "100%");
		tableDefinition.setRowRenderer( new TableEditorRowRenderer());
		setPageSize(10);
		setEmptyTableWidget(new HTML(
				"There is no data to display"));

		// Setup the bulk renderer
		FixedWidthGridBulkRenderer<TransUnit> bulkRenderer = new FixedWidthGridBulkRenderer<TransUnit>(
				getDataTable(), this);
		setBulkRenderer(bulkRenderer);

		// Setup the formatting
		setCellPadding(3);
		setCellSpacing(0);
		setResizePolicy(ScrollTable.ResizePolicy.FILL_WIDTH);
		
		getDataTable().setSelectionPolicy(SelectionPolicy.ONE_ROW);
		getDataTable().setCellPadding(3);
		getDataTable().addRowSelectionHandler(new RowSelectionHandler() {
			@Override
			public void onRowSelection(RowSelectionEvent event) {
				if(!event.getSelectedRows().isEmpty()){
					Row row = event.getSelectedRows().iterator().next();
					TransUnit tu = getRowValue(row.getRowIndex());
					SelectionEvent.fire(TableEditorView.this, tu);
				}
			}
		});
	}

	@Override
	public void setPageSize(int pageSize) {
		super.setPageSize(pageSize);
		cachedTableModel.setPostCachedRowCount(pageSize*cachedPages);
		cachedTableModel.setPreCachedRowCount(pageSize*cachedPages);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
		setVisible(false);
	}

	@Override
	public void stopProcessing() {
		setVisible(true);
	}

	@Override
	public HandlerRegistration addSelectionHandler(
			SelectionHandler<TransUnit> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}

	@Override
	public HasSelectionHandlers<TransUnit> getSelectionHandlers() {
		return this;
	}
	
	
	@Override
	public HasPageChangeHandlers getPageChangeHandlers() {
		return this;
	}

	@Override
	public HasPageCountChangeHandlers getPageCountChangeHandlers() {
		return this;
	}
	
	public void setCachedPages(int cachedPages) {
		this.cachedPages = cachedPages;
	}
	
	public int getCachedPages() {
		return cachedPages;
	}

	@Override
	public RedirectingCachedTableModel<TransUnit> getTableModel() {
		return cachedTableModel;
	}
	
	@Override
	public void setTableModelHandler(TableModelHandler<TransUnit> handler) {
		cachedTableModel.getTableModel().setTableModelHandler(handler);
	}

}
