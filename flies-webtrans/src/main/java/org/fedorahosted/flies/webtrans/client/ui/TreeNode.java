package org.fedorahosted.flies.webtrans.client.ui;

public interface TreeNode<T> {

	public TreeNode<T> addItem(String name);
	public T getObject();
	public void setObject(T userObj);
	public void setState(boolean open);
}
