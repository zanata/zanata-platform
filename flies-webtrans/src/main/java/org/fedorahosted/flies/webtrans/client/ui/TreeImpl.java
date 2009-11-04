package org.fedorahosted.flies.webtrans.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;

public class TreeImpl<K, T> extends Tree implements HasTreeNodes<K, T> {
	
	private Map<K, TreeNode<T>> nodeMap = new HashMap<K, TreeNode<T>>();

	public TreeImpl() {
		super();
	}

	public TreeImpl(TreeImages images) {
		super(images);
	}

	@Override
	public TreeNodeImpl<K, T> addItem(String itemText) {
		TreeNodeImpl<K, T> treeNodeImpl = new TreeNodeImpl<K, T>(itemText);
		addItem(treeNodeImpl);
		// TODO add to the hashmap
		
		return treeNodeImpl;
	}
	
	@Override
	public void clear() {
		super.clear();
		nodeMap.clear();
	}
	
	@Override
	public TreeNodeImpl<K, T> getSelectedNode() {
		TreeItem item = super.getSelectedItem();
		if (item instanceof TreeNodeImpl<?, ?>)
			return (TreeNodeImpl<K, T>) item;
		else
			return null;
	}
	
	@Override
	public void setSelectedNode(TreeNode<T> node) {
		setSelectedItem((TreeItem) node, false);
	}

	@Override
	public TreeNode<T> getNode(int index) {
		return (TreeNode<T>) getItem(index);
	}

	@Override
	public int getNodeCount() {
		return getItemCount();
	}

	@Override
	public TreeNode<T> getNodeByKey(K key) {
		return nodeMap.get(key);
	}

	@Override
	public void nodeAdded(K key, TreeNode<T> node) {
		nodeMap.put(key, node);
	}
	
}
