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
public class ApiKeyHeaderDecorator implements ClientExecutionInterceptor {

	private String apiKey;
	private String username;

	public ApiKeyHeaderDecorator(){}
	public ApiKeyHeaderDecorator(String username, String apiKey) {
		this.username = username;
		this.apiKey = apiKey;
	}

	@Override
	public ClientResponse execute(ClientExecutionContext ctx) throws Exception {
		ctx.getRequest().getHeaders().add("X-Auth-User", username);
		ctx.getRequest().getHeaders().add("X-Auth-Token", apiKey);
		return ctx.proceed();
	}
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
}