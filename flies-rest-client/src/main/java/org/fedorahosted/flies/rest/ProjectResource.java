package org.fedorahosted.flies.rest;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.jboss.resteasy.client.ClientResponse;

public class ProjectResource implements IProjectResource{
	
	private final IProjectResource projectResource;
	private final FliesClientRequestFactory requestFactory;
	private final URI uri;

	public ProjectResource(FliesClientRequestFactory requestFactory, IProjectResource projectResource, URI uri) {
		this.projectResource = projectResource;
		this.requestFactory = requestFactory;
		this.uri = uri;
	}
	
	@Override
	public ClientResponse<Project> get() {
		return projectResource.get();
	}

	public ProjectIterationResource getIterationResource(String iterationSlug) {
		URI uri = this.uri.resolve("iterations/i/"+iterationSlug);
		IProjectIterationResource projectIterationResource = requestFactory.getProjectIterationResource(uri);
		return new ProjectIterationResource(requestFactory, projectIterationResource, uri);
	}

	@Override
	public Response put(Project project) {
		return projectResource.put(project);
	}
	


}
