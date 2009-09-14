package org.fedorahosted.flies.rest;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.client.DocumentResource;
import org.fedorahosted.flies.rest.client.DocumentsResource;
import org.fedorahosted.flies.rest.client.ProjectIterationResource;
import org.fedorahosted.flies.rest.client.ProjectResource;
import org.fedorahosted.flies.rest.client.ProjectsResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectRefs;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class FliesClientRequestFactory extends ClientRequestFactory {
	static {
		ResteasyProviderFactory instance = ResteasyProviderFactory
				.getInstance();
		RegisterBuiltin.register(instance);
	}

	public FliesClientRequestFactory(String username, String apiKey) {
		getPrefixInterceptors().registerInterceptor(
				new ApiKeyHeaderDecorator(username, apiKey));
	}
	
	public DocumentResource getDocumentResource(URI uri) {
		final DocumentResource documentResource = createProxy(DocumentResource.class);
		return new DocumentResource() {
			
			@Override
			public Response put(Document document) {
				return documentResource.put(document);
			}
			
			@Override
			public ClientResponse<Document> get(String includeTargets) {
				return documentResource.get(includeTargets);
			}
		};
	}

	public DocumentsResource getDocumentsResource(final URI uri) {
		final DocumentsResource documentsResource = createProxy(DocumentsResource.class, uri);
		return new DocumentsResource() {
			
			@Override
			public Response put(Documents documents) {
				return documentsResource.put(documents);
			}
			
			@Override
			public Response post(Documents documents) {
				return documentsResource.post(documents);
			}
			
			@Override
			public ClientResponse<Documents> getDocuments() {
				return documentsResource.getDocuments();
			}
			
			@Override
			public DocumentResource getDocument(String documentId) {
				return getDocumentResource(uri.resolve("d/"+documentId));
			}
		};
	}

	public ProjectIterationResource getProjectIterationResource(final URI uri) {
		final ProjectIterationResource projectIterationResource =  createProxy(ProjectIterationResource.class, uri);
		return new ProjectIterationResource() {
			
			@Override
			public Response put(ProjectIteration project) {
				return projectIterationResource.put(project);
			}
			
			@Override
			public DocumentsResource getDocuments() {
				return getDocumentsResource(uri.resolve("documents"));
			}
			
			@Override
			public ClientResponse<ProjectIteration> get() {
				return projectIterationResource.get();
			}
		};
	}

	public ProjectResource getProjectResource(final URI uri) {
		final ProjectResource projectResource =  createProxy(ProjectResource.class, uri);
		return new ProjectResource() {
			
			@Override
			public Response put(Project project) {
				return projectResource.put(project);
			}
			
			@Override
			public ProjectIterationResource getIteration(String iterationSlug) {
				return getProjectIterationResource(uri.resolve("iterations/i/"+iterationSlug));
			}
			
			@Override
			public ClientResponse<Project> get() {
				return projectResource.get();
			}
		};
	}

	public ProjectsResource getProjectsResource(final URI uri) {
		final ProjectsResource projectsResource = createProxy(ProjectsResource.class, uri);
		
		return new ProjectsResource() {
			
			@Override
			public ClientResponse<ProjectRefs> getProjects() {
				return projectsResource.getProjects();
			}
			
			@Override
			public ProjectResource getProject(String projectSlug) {
				return getProjectResource(uri.resolve("p/"+projectSlug));
			}
		};
		
	}
}
