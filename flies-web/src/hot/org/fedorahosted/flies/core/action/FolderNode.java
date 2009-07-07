package org.fedorahosted.flies.core.action;

import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

public class FolderNode extends TreeNodeImpl<String>{
	
	public FolderNode(String folderName) {
		setData(folderName);
	}

	public String getType(){
		return "folder";
	}
	
	public void addChild(Object identifier, DocNode child) {
		super.addChild(identifier, (TreeNode) child);
	}
	
}
