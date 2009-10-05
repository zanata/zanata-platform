package org.fedorahosted.flies.webtrans.client;

import java.util.Arrays;

import org.fedorahosted.flies.webtrans.client.TestData;
import org.fedorahosted.flies.webtrans.client.Folder;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

public class DocumentListPanel extends ContentPanel {

	public DocumentListPanel() {		

	    setBodyBorder(false);
	    setHeading("Documents");
	    setButtonAlign(HorizontalAlignment.CENTER);
	    setLayout(new RowLayout(Orientation.VERTICAL));
	    setFrame(true);
	    setSize(600, 300);

	    Folder model = TestData.getTreeModel();
	    
	    TreeStore<ModelData> store = new TreeStore<ModelData>();
	    store.add(model.getChildren(), true);
	    
	    ColumnConfig name = new ColumnConfig("name", "Name", 100);
	    name.setRenderer(new TreeGridCellRenderer<ModelData>());
	    
	    ColumnConfig date = new ColumnConfig("author", "Author", 100);
	    
	    ColumnConfig size = new ColumnConfig("genre", "Genre", 100);

	    ColumnModel cm = new ColumnModel(Arrays.asList(name, date, size));
	    
		TabPanel innerTab = new TabPanel();
		innerTab.setPlain(false);
		innerTab.setTabPosition(TabPosition.BOTTOM);

		TabItem treeTab = new TabItem("Tree");
	    TreeGrid<ModelData> tree = new TreeGrid<ModelData>(store, cm);
	    tree.setBorders(true);
	    //tree.getStyle().setLeafIcon(Examples.ICONS.music());
	    tree.setSize(400, 400);
	    tree.setAutoExpandColumn("name");
	    tree.setTrackMouseOver(false);
	    treeTab.add(tree);
		innerTab.add(treeTab);
		TabItem listTab = new TabItem("List");
		listTab.addText("Just a plain old List");
		innerTab.add(listTab);

		add(innerTab);
	}

}
