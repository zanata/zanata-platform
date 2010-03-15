package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Composite;

public abstract class Node<T> extends Composite {

	private T dataItem;
	
	abstract boolean isDocument();
	
	public T getDataItem() {
		return dataItem;
	}
	public void setDataItem(T dataItem) {
		this.dataItem = dataItem;
		refresh();
	}

	abstract void refresh();
}
