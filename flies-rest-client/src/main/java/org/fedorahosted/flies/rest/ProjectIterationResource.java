package org.fedorahosted.flies.rest;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.jboss.resteasy.client.ClientResponse;

public class ProjectIterationResource implements IProjectIterationResource{
	
	private final IProjectIterationResource projectIterationResource;
	private final FliesClientRequestFactory requestFactory;
	private final URI uri;
	
	public ProjectIterationResource(FliesClientRequestFactory requestFactory, IProjectIterationResource projectIterationResource, URI uri) {
		this.requestFactory = requestFactory;
		this.projectIterationResource = projectIterationResource;
		this.uri = uri;
	}
	
	@Override
	public ClientResponse<ProjectIteration> get() {
		return projectIterationResource.get();
	}

	public DocumentsResource getDocumentsResource() {
		URI uri = this.uri.resolve("documents");
		IDocumentsResource documentsResource = requestFactory.getDocumentsResource(uri);
		return new DocumentsResource(requestFactory, documentsResource, uri);
	}
	
	@Override
	public Response put(ProjectIteration project) {
		return projectIterationResource.put(project);
	}

	
}
