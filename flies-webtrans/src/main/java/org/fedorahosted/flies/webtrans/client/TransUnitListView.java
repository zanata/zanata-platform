package org.fedorahosted.flies.webtrans.client;


import org.fedorahosted.flies.webtrans.client.mvp.TextAreaCellEditor;
import org.fedorahosted.flies.webtrans.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.gen2.table.client.CachedTableModel;
import com.google.gwt.gen2.table.client.CellRenderer;
import com.google.gwt.gen2.table.client.ColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultRowRenderer;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.FixedWidthFlexTable;
import com.google.gwt.gen2.table.client.FixedWidthGridBulkRenderer;
import com.google.gwt.gen2.table.client.PagingScrollTable;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.gen2.table.client.TableDefinition;
import com.google.gwt.gen2.table.client.TableDefinition.AbstractCellView;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.gen2.table.event.client.TableEvent.Row;
import com.google.gwt.gen2.table.override.client.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.SyntaxLanguage;

public class TransUnitListView extends Composite implements
		TransUnitListPresenter.Display, HasSelectionHandlers<TransUnit>, HasPageNavigation{

	final FlowPanel panel = new FlowPanel();
	
	public TransUnitListView() {
		Log.info("setting up TransUnitListView");
		setupScrollTable();
		initWidget(panel);
		setSize("100%", "100%");
		panel.setSize("100%", "100%");
		panel.add( pagingScrollTable );
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

	/**
	 * The {@link CachedTableModel} around the main table model.
	 */
	private CachedTableModel<TransUnit> cachedTableModel = null;

	/**
	 * The {@link PagingScrollTable}.
	 */
	private PagingScrollTable<TransUnit> pagingScrollTable = null;

	private SyntaxLanguageWidget syntaxSelectionWidget;


	protected void setupScrollTable() {
		// Setup the controller
		DataSourceTableModel tableModel = new DataSourceTableModel();
		cachedTableModel = new CachedTableModel<TransUnit>(tableModel);
		cachedTableModel.setPreCachedRowCount(50);
		cachedTableModel.setPostCachedRowCount(50);
		cachedTableModel.setRowCount(1000);
		Log.info("Row count: "+ tableModel.getRowCount() );
		Log.info("Row count: "+ cachedTableModel.getRowCount() );

		syntaxSelectionWidget = new SyntaxLanguageWidget(SyntaxLanguage.MIXED);
		
		// Create a TableCellRenderer
		TableDefinition<TransUnit> tableDef = createTableDefinition();

		// Create the scroll table
		pagingScrollTable = new PagingScrollTable<TransUnit>(cachedTableModel,
				tableDef);
		pagingScrollTable.setPageSize(50);
		pagingScrollTable.setEmptyTableWidget(new HTML(
				"There is no data to display"));

		// Setup the bulk renderer
		FixedWidthGridBulkRenderer<TransUnit> bulkRenderer = new FixedWidthGridBulkRenderer<TransUnit>(
				pagingScrollTable.getDataTable(), pagingScrollTable);
		pagingScrollTable.setBulkRenderer(bulkRenderer);

		// Setup the formatting
		pagingScrollTable.setCellPadding(3);
		pagingScrollTable.setCellSpacing(0);
		pagingScrollTable.setResizePolicy(ScrollTable.ResizePolicy.FILL_WIDTH);
		pagingScrollTable.setFooterTable( createFooterTable() );
		pagingScrollTable.setSize("100%", "100%");
		
		pagingScrollTable.getDataTable().addRowSelectionHandler(new RowSelectionHandler() {
			@Override
			public void onRowSelection(RowSelectionEvent event) {
				if(!event.getSelectedRows().isEmpty()){
					Row row = event.getSelectedRows().iterator().next();
					TransUnit tu = pagingScrollTable.getRowValue(row.getRowIndex());
					SelectionEvent.fire(TransUnitListView.this, tu);
				}
			}
		});
		
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		pagingScrollTable.gotoFirstPage();
	}

	private FlowPanel toolbar;
	
	private FixedWidthFlexTable createFooterTable() {
		FixedWidthFlexTable footerTable = new FixedWidthFlexTable();

		FlexCellFormatter headerFormatter = footerTable.getFlexCellFormatter();
		toolbar = new FlowPanel();
		toolbar.add(new Label("Navigation toolbar goes here"));
		toolbar.add(syntaxSelectionWidget);
		footerTable.setWidget(0, 0, toolbar);
		headerFormatter.setColSpan(0, 0, 2);
		
		return footerTable;
	}
	
	private TableDefinition<TransUnit> createTableDefinition() {

		// Create the table definition
		DefaultTableDefinition<TransUnit> tableDefinition = new DefaultTableDefinition<TransUnit>();

		// Set the row renderer
		String[] rowColors = new String[] { "#FFFFDD", "#EEEEEE" };
		tableDefinition.setRowRenderer(new DefaultRowRenderer<TransUnit>(
				rowColors));

		{
			TransUnitColumnDefinition<String> columnDef = new TransUnitColumnDefinition<String>() {

				@Override
				public void setCellValue(TransUnit rowValue, String cellValue) {
					rowValue.setSource(cellValue);
				}

				@Override
				public String getCellValue(TransUnit rowValue) {
					return rowValue.getSource();
				}
			};
			columnDef.setCellRenderer(new CellRenderer<TransUnit, String>() {
				@Override
				public void renderRowValue(TransUnit rowValue,
						ColumnDefinition<TransUnit, String> columnDef,
						AbstractCellView<TransUnit> view) {
					HighlightingLabel widget = new HighlightingLabel(rowValue.getSource());
					widget.observe(syntaxSelectionWidget);
					view.setWidget(widget);
				}
			});
			tableDefinition.addColumnDefinition(columnDef);

		}

		{
			TransUnitColumnDefinition<String> columnDef = new TransUnitColumnDefinition<String>() {

				@Override
				public void setCellValue(TransUnit rowValue, String cellValue) {
					rowValue.setTarget(cellValue);
				}

				@Override
				public String getCellValue(TransUnit rowValue) {
					return rowValue.getTarget();
				}
			};
			columnDef.setCellRenderer(new CellRenderer<TransUnit, String>() {
				@Override
				public void renderRowValue(TransUnit rowValue,
						ColumnDefinition<TransUnit, String> columnDef,
						AbstractCellView<TransUnit> view) {
					HighlightingLabel widget = new HighlightingLabel(rowValue.getTarget());
					widget.observe(syntaxSelectionWidget);
					view.setWidget(widget);
				}
			});
			columnDef.setCellEditor(new TextAreaCellEditor());
//			CodeMirrorEditorWidget editorWidget = HighlightingCellEditor.createWidget();
//			editorWidget.observe(syntaxSelectionWidget);
//			columnDef.setCellEditor(new HighlightingCellEditor(editorWidget));
			tableDefinition.addColumnDefinition(columnDef);
		}

		return tableDefinition;
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
	public HasWidgets getToolbar() {
		return toolbar;
	}

	@Override
	public HasPageNavigation getPageNavigation() {
		return this;
	}

	@Override
	public void gotoFirstPage() {
		pagingScrollTable.gotoFirstPage();
	}

	@Override
	public void gotoLastPage() {
		pagingScrollTable.gotoLastPage();
	}

	@Override
	public void gotoNextPage() {
		pagingScrollTable.gotoNextPage();
	}

	@Override
	public void gotoPage(int page, boolean forced) {
		pagingScrollTable.gotoPage(page, forced);
	}

	@Override
	public void gotoPreviousPage() {
		pagingScrollTable.gotoPreviousPage();
	}

	@Override
	public HasPageChangeHandlers getPageChangeHandlers() {
		return pagingScrollTable;
	}

	@Override
	public HasPageCountChangeHandlers getPageCountChangeHandlers() {
		return pagingScrollTable;
	}

}
