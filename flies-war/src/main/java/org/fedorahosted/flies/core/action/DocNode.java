package org.fedorahosted.flies.core.action;


import org.fedorahosted.flies.repository.model.document.HDocument;
import org.richfaces.model.TreeNodeImpl;

public class DocNode extends TreeNodeImpl<HDocument>{
	
	public DocNode(HDocument doc) {
		setData(doc);
	}
	
	public String getType(){
		return "doc";
	}

}
