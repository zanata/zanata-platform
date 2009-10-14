package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;
import java.util.HashMap;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.TreeNode;

public class FlatFolderDocNameMapper implements DocNameMapper {
	public void addToTree(HasTreeNodes tree, ArrayList<DocName> docNames) {
		HashMap<String, TreeNode> folderNodes = new HashMap<String, TreeNode>();
		for (DocName docName : docNames) {
			String path = docName.getPath();
			TreeNode item;
			if (path == null || path.length() == 0) {
				item = tree.addItem(docName.getName());
			} else {
				TreeNode folder = folderNodes.get(path);
				if (folder == null) {
					// TODO create intervening folders for a hierarchical impl
					folder = tree.addItem(path);
					folderNodes.put(path, folder);
				}
				item = folder.addItem(docName.getName());
			}
			item.setUserObject(docName.getId());
		}
	}

}
