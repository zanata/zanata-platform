package org.fedorahosted.flies.core.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javassist.expr.Instanceof;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.mail.Folder;
import javax.persistence.EntityManager;

import net.openl10n.opc4j.PackageAccess;
import net.openl10n.packaging.document.Content;
import net.openl10n.packaging.document.ContentPart;
import net.openl10n.packaging.document.ContentTarget;
import net.openl10n.packaging.document.ContentTargetPart;
import net.openl10n.packaging.document.DocumentPart;
import net.openl10n.packaging.document.DocumentRef;
import net.openl10n.packaging.document.TextFlowTarget;
import net.openl10n.packaging.jpa.document.HDocument;
import net.openl10n.packaging.jpa.document.HDocumentTarget;
import net.openl10n.packaging.jpa.document.HResource;
import net.openl10n.packaging.jpa.document.HTextFlow;
import net.openl10n.packaging.jpa.document.HTextFlowTarget;
import net.openl10n.packaging.jpa.project.HProject;
import net.openl10n.packaging.project.Project;
import net.openl10n.packaging.project.ProjectPackage;
import net.openl10n.packaging.project.ProjectPart;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;
import org.richfaces.model.UploadItem;

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
    
    
    public void listener(UploadEvent event) throws Exception{
        UploadItem item = event.getUploadItem();
        log.info("Uploading {0}. temp-file: {1}", item.getFileName(), item.isTempFile());
        
		ProjectPackage projectPack = ProjectPackage.open( item.getFile(),PackageAccess.READ );
		ProjectPart projectPart = projectPack.getProjectPart();
		Project project = projectPart.getProject();
		
		getInstance().copy(project);
		
		getEntityManager().merge(getInstance());
		
		int docNum = 0;
		for(DocumentRef docInfo : project.getDocuments()){
			HDocument hDoc = getInstance().getDocuments().get(docNum++); 
			String rId = docInfo.getRelationshipId();
			DocumentPart docPart = (DocumentPart) projectPart.getPartById(rId);
			ContentPart contentPart = docPart.getContentPart();
			Content docContent = contentPart.getContent();
			
			hDoc.copy( docContent );
			getEntityManager().flush();

			List<ContentTargetPart> contentTargetParts = contentPart.getContentTargetParts();
			for(ContentTargetPart docTargetPart : contentTargetParts) {
				ContentTarget contentTarget = docTargetPart.getContentTarget();
				
				HDocumentTarget hDocTarget = new HDocumentTarget(hDoc, contentTarget);
				Iterator<TextFlowTarget> tftIt = contentTarget.getTextFlowTargets().iterator();
				Iterator<HResource> tfIt = hDoc.getResourceTree().iterator();
				while(tftIt.hasNext()) {
					TextFlowTarget tft = tftIt.next();
					HTextFlow hTf = (HTextFlow) tfIt.next();
					HTextFlowTarget hTft = new HTextFlowTarget(hDocTarget, hTf);
					hTft.copy(tft);
				}
			}
		}
		update();
    }    
    
    
}
