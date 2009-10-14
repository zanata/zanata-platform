package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;

public class TreeImpl extends Tree implements HasTreeNodes {
	
	public TreeImpl() {
	}

	public TreeImpl(TreeImages images) {
		super(images);
	}

	@Override
	public TreeNodeImpl addItem(String itemText) {
		TreeNodeImpl treeNodeImpl = new TreeNodeImpl(itemText);
		addItem(treeNodeImpl);
		return treeNodeImpl;
	}

}
