package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;

public interface TreeNode<T> extends HasMouseOverHandlers, HasMouseOutHandlers, HasChildTreeNodes<T> {
	public TreeNode<T> addItem(String name);
	public T getObject();
	public void setObject(T userObj);
	public void setState(boolean open);
}
