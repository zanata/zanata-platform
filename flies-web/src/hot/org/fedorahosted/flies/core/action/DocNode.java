package org.fedorahosted.flies.core.action;

import net.openl10n.packaging.jpa.document.HDocument;

import org.richfaces.model.TreeNodeImpl;

public class DocNode extends TreeNodeImpl<HDocument>{
	
	public DocNode(HDocument doc) {
		setData(doc);
	}
	
	public String getType(){
		return "doc";
	}

}
