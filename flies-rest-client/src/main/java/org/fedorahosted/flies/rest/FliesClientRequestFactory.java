package org.fedorahosted.flies.rest;

import java.net.URI;

import org.fedorahosted.flies.rest.client.DocumentResource;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class FliesClientRequestFactory extends ClientRequestFactory {
    static {
	ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
	RegisterBuiltin.register(instance);
    }
    
    public FliesClientRequestFactory(String apiKey) {
	getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(apiKey));
    }

    public DocumentResource getDocumentResource(URI uri) {
	return createProxy(DocumentResource.class, uri);
    }
}
