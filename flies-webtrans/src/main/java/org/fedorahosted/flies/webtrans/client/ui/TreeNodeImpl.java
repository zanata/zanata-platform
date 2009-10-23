package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;

public class TreeNodeImpl<T> extends TreeItem implements TreeNode<T> {

	private HandlerManager handlerManager;
	private HandlerRegistration labelMouseOverReg;
	private HandlerRegistration labelMouseOutReg;

	public TreeNodeImpl(String name) {
		super(new Label(name));
	}
	
	private HandlerManager getHandlerManager() {
		return handlerManager == null ? 
				handlerManager = new HandlerManager(this) : handlerManager;
	}

	public Label getLabel() {
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
	public TreeNode<T> getNode(int index) {
		return (TreeNode<T>) getChild(index);
	}

	@Override
	public int getNodeCount() {
		return getChildCount();
	}
	
	@Override
	public HandlerRegistration addMouseOverHandler(final MouseOverHandler handler) {
		if (labelMouseOverReg == null) {
			labelMouseOverReg = getLabel().addMouseOverHandler(new MouseOverHandler() {				
				@Override
				public void onMouseOver(MouseOverEvent event) {
					// refire the event *using this TreeNode as source*
					fireEvent(event);
				}
			});
		}
		return getHandlerManager().addHandler(MouseOverEvent.getType(), handler);
	}
	
	@Override
	public HandlerRegistration addMouseOutHandler(final MouseOutHandler handler) {
		if (labelMouseOutReg == null) {
			labelMouseOutReg = getLabel().addMouseOutHandler(new MouseOutHandler() {				
				@Override
				public void onMouseOut(MouseOutEvent event) {
					// refire the event *using this TreeNode as source*
					fireEvent(event);
				}
			});
		}
		return getHandlerManager().addHandler(MouseOutEvent.getType(), handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		getHandlerManager().fireEvent(event);
	}
}
