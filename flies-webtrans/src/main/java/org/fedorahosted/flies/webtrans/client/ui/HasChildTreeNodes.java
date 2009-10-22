package org.fedorahosted.flies.webtrans.client.ui;

public interface HasChildTreeNodes<T> {

	public TreeNode<T> getNode(int index);

	public int getNodeCount();

}