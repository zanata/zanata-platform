package org.fedorahosted.flies.rest;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.client.IDocumentResource;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.client.IProjectsResource;
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
	
	public IDocumentResource getDocumentResource(URI uri) {
		return createProxy(IDocumentResource.class, uri);
	}

	public IDocumentsResource getDocumentsResource(final URI uri) {
		return createProxy(IDocumentsResource.class, uri);
	}

	public IProjectIterationResource getProjectIterationResource(final URI uri) {
		return  createProxy(IProjectIterationResource.class, uri);
	}

	public IProjectResource getProjectResource(final URI uri) {
		return createProxy(IProjectResource.class, uri);
	}

	public IProjectsResource getProjectsResource(final URI uri) {
		return createProxy(IProjectsResource.class, uri);
	}
}
