package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.user.client.ui.TreeItem;

public interface HasTreeNodes<K, T> extends HasSelectionHandlers<TreeItem>, HasChildTreeNodes<T> {
	public TreeNode<T> addItem(String name);
	public void clear();
	public void removeItems();
	public TreeNode<T> getSelectedNode();
	public void setSelectedNode(TreeNode<T> node);
	public void nodeAdded(K key, TreeNode<T> node);
	public TreeNode<T> getNodeByKey(K key);
}
