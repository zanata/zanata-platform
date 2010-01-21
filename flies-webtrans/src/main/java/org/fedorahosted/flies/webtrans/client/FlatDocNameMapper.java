package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.TreeNode;
import org.fedorahosted.flies.webtrans.client.ui.TreeNodeMapper;

public class FlatDocNameMapper implements TreeNodeMapper<DocumentId, DocName> {
	public void addToTree(HasTreeNodes<DocumentId, DocName> tree, ArrayList<DocName> docNames, boolean openFolderNodes) {
		for (DocName docName : docNames) {
			String nodePath;
			String path = docName.getPath();
			if (path != null && path.length() != 0)
				nodePath = path+"/"+docName.getName(); 
			else
				nodePath = docName.getName();
			TreeNode<DocName> item = tree.addItem(nodePath);
			tree.nodeAdded(docName.getId(), item);
			item.setObject(docName);
		}
	}

	@Override
	public boolean passFilter(DocName docName, String filter) {
		return docName.getName().toLowerCase().contains(filter.toLowerCase());
	}

}
