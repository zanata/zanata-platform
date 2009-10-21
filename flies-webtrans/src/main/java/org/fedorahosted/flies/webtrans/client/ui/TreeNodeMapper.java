package org.fedorahosted.flies.webtrans.client.ui;

import java.util.ArrayList;


public interface TreeNodeMapper<T> {
	public void addToTree(HasTreeNodes<T> tree, ArrayList<T> elements, boolean openFolderNodes);
	public boolean passFilter(T element, String filter);
}