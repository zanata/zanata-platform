package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.TreeItem;

public class TreeNodeImpl<T> extends TreeItem implements TreeNode<T> {

	public TreeNodeImpl(String name) {
		super(name);
	}

	@Override
	public TreeNodeImpl<T> addItem(String itemText) {
		TreeNodeImpl<T> item = new TreeNodeImpl<T>(itemText);
		addItem(item);
		return item;
	}
	
	@Override
	public void setObject(T userObj) {
		super.setUserObject(userObj);
	}
	
	@Override
	public T getObject() {
		// we don't expose setUserObject directly, so this should be safe
		return (T) super.getUserObject();
	}
}
