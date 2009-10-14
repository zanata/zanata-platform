package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.TreeItem;

public class TreeNodeImpl extends TreeItem implements TreeNode {

	public TreeNodeImpl(String name) {
		super(name);
	}

	@Override
	public TreeNodeImpl addItem(String itemText) {
		TreeNodeImpl item = new TreeNodeImpl(itemText);
		addItem(item);
		return item;
	}
}
