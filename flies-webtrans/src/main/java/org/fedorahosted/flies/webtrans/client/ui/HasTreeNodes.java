package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.user.client.ui.TreeItem;

public interface HasTreeNodes extends HasSelectionHandlers<TreeItem> {
	public TreeNode addItem(String name);
	public void clear();
}
