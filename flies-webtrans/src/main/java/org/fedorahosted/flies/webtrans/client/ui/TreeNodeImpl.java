package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;

public class TreeNodeImpl<T> extends TreeItem implements TreeNode<T> {

	public TreeNodeImpl(String name) {
		super(new Label(name));
	}
	
	private Label getLabel() {
		return (Label) super.getWidget();
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
		// we don't expose setUserObject directly, so this cast should be safe
		return (T) super.getUserObject();
	}

	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return getLabel().addMouseOverHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		getLabel().fireEvent(event);
	}
}
