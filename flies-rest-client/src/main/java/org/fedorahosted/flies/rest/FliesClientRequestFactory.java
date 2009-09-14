package org.fedorahosted.flies.rest;

import java.net.URI;

import org.fedorahosted.flies.rest.client.DocumentResource;
import org.fedorahosted.flies.rest.client.DocumentsResource;
import org.fedorahosted.flies.rest.client.ProjectIterationResource;
import org.fedorahosted.flies.rest.client.ProjectResource;
import org.fedorahosted.flies.rest.client.ProjectsResource;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class FliesClientRequestFactory extends ClientRequestFactory {
    static {
	ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
	RegisterBuiltin.register(instance);
    }
    
    public FliesClientRequestFactory(String username, String apiKey) {
	getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(username, apiKey));
    }

    public DocumentResource getDocumentResource(URI uri) {
	return createProxy(DocumentResource.class, uri);
    }

    public DocumentsResource getDocumentsResource(URI uri) {
	return createProxy(DocumentsResource.class, uri);
    }

    public ProjectIterationResource getProjectIterationResource(URI uri) {
	return createProxy(ProjectIterationResource.class, uri);
    }

    public ProjectResource getProjectResource(URI uri) {
	return createProxy(ProjectResource.class, uri);
    }

    public ProjectsResource getProjectsResource(URI uri) {
	return createProxy(ProjectsResource.class, uri);
    }
}
