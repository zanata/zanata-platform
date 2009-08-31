package org.fedorahosted.flies.core.action;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.repository.model.document.HDocument;
import org.fedorahosted.flies.repository.model.project.HProject;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;

@Name("hProjectHome")
public class HProjectHome extends EntityHome<HProject> {

	@Logger
	private Log log;
	
	@In
	private EntityManager entityManager;
	
	private FolderNode rootNode;

	public FolderNode getRootNode() {
		if (rootNode == null)
			loadTree();
		return rootNode;
	}

	private void loadTree() {
		rootNode = new FolderNode("/");
		
		List<HDocument> documents = getInstance().getDocuments();
		for(HDocument doc : documents){
			addNode(doc);
		}
	}

	private void addNode(HDocument doc) {
		String path = doc.getPath();
		FolderNode pathNode = findOrCreateNodesForPath(path, rootNode);
		DocNode docNode = new DocNode(doc);
		pathNode.addChild("doc_"+doc.getId(), docNode);
	}
	
	private FolderNode findOrCreateNodesForPath(String fullPath, FolderNode parent){
		int sepIndex = fullPath.indexOf('/');
		switch(sepIndex){
		case -1:
			if(fullPath.length() == 0)
				return parent;
			return findOrCreateNodeForPath(fullPath, parent);
		case 0:
			return findOrCreateNodesForPath(fullPath.substring(1), parent);
		default:
			String pathName = fullPath.substring(0, sepIndex);
			FolderNode node = findOrCreateNodeForPath(pathName, parent);
			String remainder = fullPath.substring(sepIndex+1);
			return findOrCreateNodesForPath(remainder, node);
		}
	}
	
	private FolderNode findOrCreateNodeForPath(String pathName, FolderNode parent){
		Iterator it = parent.getChildren();
		while(it.hasNext()) {
			TreeNode child =   (TreeNode) ((Entry)it.next()).getValue();
			if(child instanceof FolderNode){
				String childPath = (String) child.getData();
				if(childPath.equals(pathName)) {
					return (FolderNode) child;
				}
			}
		}
		FolderNode child = new FolderNode(pathName);
		parent.addChild("path_"+pathName, child);
		return child;
	}

	private TreeNode selectedNode;
	
    public boolean isDocSelection(){
     	return selectedNode != null && selectedNode instanceof DocNode; 
    }

    public boolean hasSelection(){
    	return selectedNode != null;
    }
    

    public DocNode getDocNode(){
    	return isDocSelection() ? (DocNode) selectedNode : null;
    }
    
    public void setDocNode(DocNode node){
    	this.selectedNode = node;
    }
    
    public FolderNode getFolderNode(){
    	return isDocSelection() ? null : (FolderNode) selectedNode;
    }

    public void setFolderNode(FolderNode node){
    	this.selectedNode = node;
    }
    
    public String getNodeFace(Object obj){
    	if(obj instanceof String)
    		return "folder";
    	return "doc";
    }
    
    public void processSelection(NodeSelectedEvent event) {
        HtmlTree tree = (HtmlTree) event.getComponent();
        selectedNode = tree.getModelTreeNode(tree.getRowKey());
    }	
    
}
