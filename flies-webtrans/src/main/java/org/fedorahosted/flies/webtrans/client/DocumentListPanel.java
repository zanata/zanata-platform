package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class DocumentListPanel extends Composite {

	public DocumentListPanel() {		

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

	    this.initWidget(root);

	}

}
