package org.fedorahosted.flies.webtrans.editor;


import org.fedorahosted.flies.gwt.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.gen2.table.client.FixedWidthGridBulkRenderer;
import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.client.PagingScrollTable;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.gen2.table.client.SelectionGrid.SelectionPolicy;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
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

public class WebTransTableView extends PagingScrollTable<TransUnit> implements
		WebTransTablePresenter.Display, HasSelectionHandlers<TransUnit>, HasPageNavigation{

	public WebTransTableView(MutableTableModel<TransUnit> tableModel, WebTransTableDefinition tableDefinition) {
		super(tableModel,tableDefinition);
		tableDefinition.setRowRenderer( new WebTransFilterRowRenderer());
		setupScrollTable();
	}

	@Inject
	public WebTransTableView(WebTransTableModel tableModel) {
		this(new CachedWebTransTableModel(tableModel), new WebTransTableDefinition());
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}

	private SyntaxToggleWidget syntaxWidget;

	protected void setupScrollTable() {
		setSize("100%", "100%");

		syntaxWidget = new SyntaxToggleWidget(ParserSyntax.MIXED, true);
		
		// Create the scroll table
		setPageSize(50);
		
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
					SelectionEvent.fire(WebTransTableView.this, tu);
				}
			}
		});
		
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
	public HasPageNavigation getPageNavigation() {
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
	

}
