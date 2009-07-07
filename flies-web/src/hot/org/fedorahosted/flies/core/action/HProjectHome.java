package org.fedorahosted.flies.core.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.mail.Folder;

import net.openl10n.packaging.jpa.document.HDocument;
import net.openl10n.packaging.jpa.document.HResource;
import net.openl10n.packaging.jpa.project.HProject;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

@Name("hProjectHome")
public class HProjectHome extends EntityHome<HProject> {

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
		findOrCreateNodesForPath("/this/is/a/long/path", rootNode);
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
			TreeNode child = (TreeNode) it.next();
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

	private List<String> selectedNodeChildren = new ArrayList<String>();    
    private String nodeTitle;
    
    public String getNodeTitle() {
        return nodeTitle;
    }

    public void setNodeTitle(String nodeTitle) {
        this.nodeTitle = nodeTitle;
    }
    
    public String getNodeFace(Object obj){
    	if(obj instanceof String)
    		return "folder";
    	return "doc";
    }
    
    public void processSelection(NodeSelectedEvent event) {
        HtmlTree tree = (HtmlTree) event.getComponent();
        selectedNodeChildren.clear();
        TreeNode currentNode = tree.getModelTreeNode(tree.getRowKey());
        String data = (String) currentNode.getData();
        nodeTitle = (String) tree.getRowData();
        nodeTitle += " (" + data + ")";
        
        if (currentNode.isLeaf()){
            selectedNodeChildren.add((String)currentNode.getData());
        }else
        {
            Iterator<Map.Entry<Object, TreeNode>> it = currentNode.getChildren();
            while (it!=null &&it.hasNext()) {
                Map.Entry<Object, TreeNode> entry = it.next();
                selectedNodeChildren.add(entry.getValue().getData().toString()); 
            }
        }
    }	
}
