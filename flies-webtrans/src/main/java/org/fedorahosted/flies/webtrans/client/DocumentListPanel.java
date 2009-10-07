package org.fedorahosted.flies.webtrans.client;

import java.util.Arrays;

import org.fedorahosted.flies.webtrans.client.TestData;
import org.fedorahosted.flies.webtrans.client.Folder;
/*import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;*/

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.CheckBox;

public class DocumentListPanel extends Composite {

	public DocumentListPanel() {		

/*	    setBodyBorder(false);
	    setHeading("Documents");
	    setButtonAlign(HorizontalAlignment.CENTER);
	    setLayout(new RowLayout(Orientation.VERTICAL));
	    setFrame(true);
	    setSize(600, 300);*/
/*
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
	    final TreeGrid<ModelData> tree = new TreeGrid<ModelData>(store, cm);
	    tree.setBorders(true);
	    tree.setTrackMouseOver(false);
	    treeTab.add(tree);
		innerTab.add(treeTab);

		TabItem listTab = new TabItem("List");
		listTab.addText("Just a plain old List");
		innerTab.add(listTab);

//		add(innerTab);
		*/
	    // Create a tree with a few items in it.
	    Tree root = new Tree();
	    
	    TreeItem item0 = new TreeItem("item0");
	    TreeItem item1 = new TreeItem("item1");
	    TreeItem item2 = new TreeItem("item2");
	    
	    root.addItem(item0);
	    root.addItem(item1);
	    root.addItem(item2);
	    
	    item0.addItem(new TreeItem("item0.1"));
	    item0.addItem(new TreeItem("item0.2"));
	    item0.addItem(new TreeItem("item0.3"));
	    
	    item1.addItem(new TreeItem("item1.1"));
	    item1.addItem(new TreeItem("item1.2"));
	    
	    item2.addItem(new TreeItem("item2.1"));
	    item2.addItem(new TreeItem("item2.2"));
	    item2.addItem(new TreeItem("item2.3"));
	    item2.addItem(new TreeItem("item2.4"));
	    
	    root.addItem(new TreeItem("item3"));

//	    Tree t = new Tree();
//	    t.addItem(root);
	    
	    // Add it to the root panel.
//	    RootPanel.get().add(t);
	    this.initWidget(root);

	}

}
