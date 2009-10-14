package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;
import org.fedorahosted.flies.webtrans.model.DocName;

public interface DocNameMapper {
	public void addToTree(HasTreeNodes tree, ArrayList<DocName> docNames);
}