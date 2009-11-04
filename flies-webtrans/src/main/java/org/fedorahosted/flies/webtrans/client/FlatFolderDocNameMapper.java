package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;
import java.util.HashMap;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.TreeNode;
import org.fedorahosted.flies.webtrans.client.ui.TreeNodeMapper;

public class FlatFolderDocNameMapper implements TreeNodeMapper<DocumentId, DocName> {
	public void addToTree(HasTreeNodes<DocumentId, DocName> tree, ArrayList<DocName> docNames, boolean openFolderNodes) {
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
				folder.setState(openFolderNodes); // TreeItem can't open a node until it has children
			}
			tree.nodeAdded(docName.getId(), item);
			item.setObject(docName);
		}
	}

	@Override
	public boolean passFilter(DocName docName, String filter) {
		return docName.getName().contains(filter);
	}

}
