package org.fedorahosted.flies.webtrans.client.ui;

import org.fedorahosted.flies.gwt.model.DocName;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.user.client.ui.TreeItem;

public interface HasTreeNodes<T> extends HasSelectionHandlers<TreeItem> {
	public TreeNode<T> addItem(String name);
	public void clear();
	public void removeItems();
	public TreeNode<T> getSelectedNode();
	public void setSelectedNode(TreeNode<DocName> node);
}
