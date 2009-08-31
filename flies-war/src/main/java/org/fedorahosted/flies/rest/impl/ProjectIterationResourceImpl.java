package org.fedorahosted.flies.rest.impl;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.rest.DocumentResource;
import org.fedorahosted.flies.rest.ProjectIterationResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentRef;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

@Name("projectIterationResource")
public class ProjectIterationResourceImpl implements ProjectIterationResource{

	private org.fedorahosted.flies.core.model.ProjectIteration projectIteration;
	
	public void setProjectIteration(org.fedorahosted.flies.core.model.ProjectIteration projectIteration) {
		this.projectIteration = projectIteration;
	}

	@Override
	public ProjectIteration get(String ext) {
		ProjectIteration p = load();
		//Set<String> extensions = ImmutableSet.of( StringUtils.split(ext, ',') );
		//if(extensions != null && extensions.contains("docs")){
			Document d = new Document("123", "name", "/full/path", ContentType.TextPlain, 1);
			p.getDocuments().add( new DocumentRef(d) );
		//}
		return p;
	}
	
	private ProjectIteration load(){
		ProjectIteration p = new ProjectIteration();
		p.setId( projectIteration.getProject().getSlug() + '/' + projectIteration.getSlug());
		p.setName(projectIteration.getProject().getName() + " - " + projectIteration.getName());
		p.setSummary( projectIteration.getDescription() );
		return p;
	}
	
	@Override
	public Response post(ProjectIteration project) {
		return Response.created( URI.create("http://example.com/project") ).build();
	}
	
	@Override
	public Response put(ProjectIteration project) {
		return Response.created( URI.create("http://example.com/project") ).build();
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
			public ProjectIteration get(String extensions) {
				return instance.get(extensions);
			}
			
			@Override
			public Response post(ProjectIteration project) {
				return instance.post(project);
			}
			
			@Override
			public Response put(ProjectIteration project) {
				return instance.put(project);
			}
			
			@Override
			public DocumentResource getDocument(String documentId) {
				return instance.getDocument(documentId);
			}
			
		};
	}	
	
}
