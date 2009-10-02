package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalSplitPanel;
import com.google.gwt.user.client.ui.Widget;

public class TranslatorsPanel extends ContentPanel {

	public TranslatorsPanel() {
		setHeading("Participants");
		setFrame(false);
		setCollapsible(true);

		TabPanel innerTab = new TabPanel();
		innerTab.setPlain(false);
		innerTab.setTabPosition(TabPosition.BOTTOM);

		TabItem treeTab = new TabItem("My Tribe");

		treeTab.add(getChatThisLocalePanel());
		innerTab.add(treeTab);
		
		TabItem listTab = new TabItem("All Tribes");

		
		listTab.add(getChatAllPanel());
		innerTab.add(listTab);

		add(innerTab);

	}
	
	private static Tree createTranslatorTree() {
		Tree tree = new Tree();
		tree.setWidth("100%");
		addData(tree);
		return tree;
	}
	
	private static Tree createLocaleTranslatorsTree() {
		Tree tree = new Tree();
		tree.setWidth("100%");
		addLocaleData(tree, "German");
		return tree;
	}
	
	private static void addData(Tree tree) {
		tree.addItem( createTranslator("Bob") );
		tree.addItem( createTranslator("Jane") );
	}
	
	private static TreeItem createTranslator(String name) {
		TreeItem item = new TreeItem();
		item.setText(name);
		return item;
	}
	
	private static void addLocaleData(Tree tree, String locale) {
		TreeItem item = new TreeItem();
		item.setText(locale);
		tree.addItem(item);
		item.addItem( createTranslator("Bob") );
		item.addItem( createTranslator("Jane") );
	}

	public static Widget getChatAllPanel() {
		
		VerticalSplitPanel vSplit = new VerticalSplitPanel();
		vSplit.setBottomWidget(new Label("Chat"));
		vSplit.setWidth("100%");
		vSplit.setHeight("300px");
		vSplit.setTopWidget(createLocaleTranslatorsTree());
		return vSplit;
	}

	public static Widget getChatThisLocalePanel() {
		
		VerticalSplitPanel vSplit = new VerticalSplitPanel();
		vSplit.setBottomWidget(new Label("Chat"));
		vSplit.setWidth("100%");
		vSplit.setHeight("300px");
		vSplit.setTopWidget(createTranslatorTree());
		return vSplit;
	}
}
