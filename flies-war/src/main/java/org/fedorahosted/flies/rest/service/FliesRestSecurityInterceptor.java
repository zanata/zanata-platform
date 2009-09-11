package org.fedorahosted.flies.rest.service;

import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.fedorahosted.flies.core.dao.AccountDAO;
import org.fedorahosted.flies.core.model.HAccount;
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

@SecurityPrecedence
@ServerInterceptor
public class FliesRestSecurityInterceptor implements PreProcessInterceptor{

	public static final String X_AUTH_TOKEN_HEADER = "X-Auth-Token";
	
	@In
	AccountDAO accountDAO;
	
	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
			throws Failure, WebApplicationException {
		
		AccountDAO accountDAO = (AccountDAO) Component.getInstance(AccountDAO.class,true);
		Log log =  Logging.getLog(FliesRestSecurityInterceptor.class);
		log.info("Authenticating a REST request...");
		List<String> tokenHeaders = request.getHttpHeaders().getRequestHeader(X_AUTH_TOKEN_HEADER);
		if(!tokenHeaders.isEmpty()){
			String apiKey = tokenHeaders.get(0);
			HAccount account = accountDAO.getByApiKey(apiKey);
			if(account != null) {
				// TODO set up identity
				return null;
			}
		}
		
		throw new UnauthorizedException();
	}	
}
