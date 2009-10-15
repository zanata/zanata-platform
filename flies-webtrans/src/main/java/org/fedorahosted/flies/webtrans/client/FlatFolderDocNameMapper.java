package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.TreeNode;

public class FlatFolderDocNameMapper implements DocNameMapper {
	public void addToTree(HasTreeNodes<DocName> tree, ArrayList<DocName> _docNames) {
		ArrayList<DocName> docNames = new ArrayList<DocName>(_docNames);
		Collections.sort(docNames, new Comparator<DocName>() {
			@Override
			public int compare(DocName o1, DocName o2) {
				String path1 = o1.getPath();
				if(path1 == null)
					path1 = "";
				String path2 = o2.getPath();
				if(path2 == null)
					path2 = "";
				int pathCompare = path1.compareTo(path2);
				if(pathCompare == 0)
					return o1.getName().compareTo(o2.getName());
				return pathCompare;
			}});
		HashMap<String, TreeNode<DocName>> folderNodes = new HashMap<String, TreeNode<DocName>>();
		for (DocName docName : docNames) {
			String path = docName.getPath();
			TreeNode<DocName> item;
			if (path == null || path.length() == 0) {
				item = tree.addItem(docName.getName());
			} else {
				TreeNode<DocName> folder = folderNodes.get(path);
				if (folder == null) {
					// TODO create intervening folders for a hierarchical impl
					folder = tree.addItem(path);
					folderNodes.put(path, folder);
				}
				item = folder.addItem(docName.getName());
			}
			item.setObject(docName);
		}
	}

}
