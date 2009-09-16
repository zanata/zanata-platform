package org.fedorahosted.flies.rest;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.core.dao.AccountDAO;
import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.security.FliesIdentity;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimplePrincipal;

@SecurityPrecedence
@ServerInterceptor
public class FliesRestSecurityInterceptor implements PreProcessInterceptor{

	public static final String X_AUTH_TOKEN_HEADER = "X-Auth-Token";
	public static final String X_AUTH_USER_HEADER = "X-Auth-User";
	
	@In
	AccountDAO accountDAO;
	
	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
			throws Failure, WebApplicationException {
		
		Log log =  Logging.getLog(FliesRestSecurityInterceptor.class);
		log.info("Authenticating a REST request...");
		String username = request.getHttpHeaders().getRequestHeaders().getFirst(X_AUTH_USER_HEADER);
		String apiKey = request.getHttpHeaders().getRequestHeaders().getFirst(X_AUTH_TOKEN_HEADER);
		
		if(username != null && apiKey != null){
			FliesIdentity.instance().getCredentials().setUsername(username);
			FliesIdentity.instance().setApiKey(apiKey);
			FliesIdentity.instance().tryLogin();
		}
		return null;
		//throw new UnauthorizedException();
	}	
}
