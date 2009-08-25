package org.fedorahosted.flies.core.rest;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.openl10n.api.ContentType;
import net.openl10n.api.rest.document.Document;
import net.openl10n.api.rest.project.Project;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import com.google.common.collect.ImmutableSet;

@Name("projectIterationResource")
public class ProjectIterationResourceImpl implements ProjectIterationResource{

	private ProjectIteration projectIteration;
	
	public void setProjectIteration(ProjectIteration projectIteration) {
		this.projectIteration = projectIteration;
	}

	@Override
	public Project get(String ext) {
		Project p = new Project("id", "name", "summary");
		Set<String> extensions = ImmutableSet.of( StringUtils.split(ext, ',') );
		if(extensions != null && extensions.contains("docs")){
			p.getDocuments().add( new Document("/path/to/doc.txt", ContentType.TextPlain ) );
		}
		return p;
	}
	
	@Override
	public DocumentResource getDocument(String documentId) {
		DocumentResourceImpl docRes = (DocumentResourceImpl) Component.getInstance(DocumentResourceImpl.class, true);
		//docRes.setProject();
		return DocumentResourceImpl.getProxyWrapper(docRes);
	}
	
	
	// hack to allow sub-resource in resteasy
	public static ProjectIterationResource getProxyWrapper(final ProjectIterationResource instance){
		return new ProjectIterationResource(){

			@Override
			public Project get(String extensions) {
				return instance.get(extensions);
			}
			
			@Override
			public DocumentResource getDocument(String documentId) {
				return instance.getDocument(documentId);
			}
			
		};
	}	
	
}
