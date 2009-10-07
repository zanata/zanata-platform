package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalSplitPanel;
import com.google.gwt.user.client.ui.Widget;

public class WorkspaceUsersView extends Composite {

	public WorkspaceUsersView() {
		initWidget( getChatAllPanel() );
	}
	
	private static Tree createLocaleTranslatorsTree() {
		Tree tree = new Tree();
		tree.setWidth("100%");
		addLocaleData(tree, "German");
		return tree;
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
}
