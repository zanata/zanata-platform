package org.fedorahosted.flies.webtrans.client.ui;

import java.util.ArrayList;


public interface TreeNodeMapper<K, T> {
	public void addToTree(HasTreeNodes<K, T> tree, ArrayList<T> elements, boolean openFolderNodes);
	public boolean passFilter(T element, String filter);
}