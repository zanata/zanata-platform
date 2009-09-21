package org.fedorahosted.flies.rest;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.UnhandledException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;
import org.jboss.seam.resteasy.ResteasyBootstrap;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;

@Name("org.jboss.seam.resteasy.bootstrap")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
@Install(classDependencies = "org.jboss.resteasy.spi.ResteasyProviderFactory", precedence=Install.DEPLOYMENT)
public class FliesResteasyBootstrap extends ResteasyBootstrap {

	@Logger
	Log log;
	
	@Override
	protected void initDispatcher() {
		super.initDispatcher();
		getDispatcher().getProviderFactory().getServerPreProcessInterceptorRegistry().register(FliesRestSecurityInterceptor.class);
	}
	
	@Override
	protected Dispatcher createDispatcher(
			SeamResteasyProviderFactory providerFactory) {
		return new SynchronousDispatcher(providerFactory){
			@Override
			public void invoke(HttpRequest request, HttpResponse response) {
				try{
					super.invoke(request, response);
				}
				catch(UnhandledException e){
					log.error("Failed to process REST request", e.getCause());
					try{
						response.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Error processing Request");
					}
					catch(IOException ioe){
						log.error("Failed to send error on failed REST request", ioe);
					}
				}
			}
		};	
	}
}
