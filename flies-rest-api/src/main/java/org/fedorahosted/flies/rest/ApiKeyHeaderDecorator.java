package org.fedorahosted.flies.rest;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyWriterContext;
import org.jboss.resteasy.spi.interception.MessageBodyWriterInterceptor;

@Provider
@ClientInterceptor
class ApiKeyHeaderDecorator implements ClientExecutionInterceptor {

	private String apiKey;
	
	public ApiKeyHeaderDecorator() {
	}

	public ApiKeyHeaderDecorator(String key) {
		this.apiKey = key;
	}
	
	@Override
	public ClientResponse execute(ClientExecutionContext ctx) throws Exception {
		ctx.getRequest().getHeaders().add("X-Auth-Token", apiKey);
		return ctx.proceed();
	}
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}