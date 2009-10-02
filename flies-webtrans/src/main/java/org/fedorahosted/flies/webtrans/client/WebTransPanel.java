package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

public class WebTransPanel extends ContentPanel {

	public WebTransPanel() {

	    // add paging support for a local collection of models
	    PagingModelMemoryProxy proxy = new PagingModelMemoryProxy(getSampleData());
		
	    // loader
	    PagingLoader<PagingLoadResult<ModelData>> loader = new BasePagingLoader<PagingLoadResult<ModelData>>(
	        proxy);
	    loader.setRemoteSort(true);

	    ListStore<TransUnit> store = new ListStore<TransUnit>(loader);
	    
	    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

	    final PagingToolBar toolBar = new PagingToolBar(100);
	    toolBar.bind(loader);
	    setBottomComponent(toolBar);
	    
	    loader.load(0, 100);
	    
		GridCellRenderer<TransUnit> sourceRenderer = new GridCellRenderer<TransUnit>() {
			public Object render(TransUnit model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<TransUnit> store, Grid<TransUnit> grid) {
				Label lbl = new Label();
				lbl.setText((String) model.get(property));
				lbl.setWidth("100%");
				lbl.setWordWrap(true);
				return lbl;
			}
		};

		GridCellRenderer<TransUnit> statusRenderer = new GridCellRenderer<TransUnit>() {
			public Object render(TransUnit model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<TransUnit> store, Grid<TransUnit> grid) {
				ContentPanel statusPanel = new ContentPanel(new RowLayout(Orientation.VERTICAL));
				statusPanel.add( new Label("X"));
				statusPanel.add( new Label("Y"));
				statusPanel.add( new Label("Z"));
				return statusPanel;
			}
		};
		ColumnConfig column = new ColumnConfig();
		column.setId("stat");
		// column.setHeader(header)
		column.setWidth(20);
		column.setRenderer(statusRenderer);
		configs.add(column);
		
		column = new ColumnConfig();
		column.setId("source");
		column.setHeader("Source");
		column.setWidth(250);
		column.setRenderer(sourceRenderer);
		configs.add(column);

		column = new ColumnConfig();
		column.setId("target");
		column.setHeader("Target");
		column.setWidth(250);
		configs.add(column);

		ColumnModel cm = new ColumnModel(configs);

		grid = new Grid<TransUnit>(store, cm);
		//grid.setStyleAttribute("borderTop", "none");
		// grid.setAutoExpandColumn("name");
		grid.setBorders(false);
		grid.setMinColumnWidth(20);
		grid.setHideHeaders(true);
		
		
		add(grid);
	}

	private static Grid<TransUnit> grid;

	@Override
	protected void onResize(int width, int height) {
		int combinedWidth = width - 20 - 20 ;
		int lWidth = combinedWidth / 2;
		grid.getColumnModel().setColumnWidth(1, lWidth);
		grid.getColumnModel().setColumnWidth(2, combinedWidth - lWidth);
		super.onResize(width, height);
	}
	
	private static ArrayList<TransUnit> getSampleData() {
		ArrayList<TransUnit> units = new ArrayList<TransUnit>();
		for (int i = 0; i < 1000; i++) {
			String source = "source ";
			for (int j = 0; j < i; j++) {
				source += "source " + j;
			}
			units.add(new TransUnit(source, "Target target target " + i));
		}
		return units;

	}

}
