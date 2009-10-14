package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;

public interface DocNameMapper {
	public void addToTree(HasTreeNodes tree, ArrayList<DocName> docNames);
}