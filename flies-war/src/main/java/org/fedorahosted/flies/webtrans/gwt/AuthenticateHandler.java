package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.gwt.rpc.AuthenticateAction;
import org.fedorahosted.flies.gwt.rpc.AuthenticateResult;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.web.ServletContexts;

@Name("webtrans.gwt.AuthenticateHandler")
@Scope(ScopeType.STATELESS)
public class AuthenticateHandler implements ActionHandler<AuthenticateAction, AuthenticateResult> {

	@Logger Log log;
	
	@In Session session;
	
	@Override
	public AuthenticateResult execute(AuthenticateAction action, ExecutionContext context)
			throws ActionException {
		log.info("Authenticating {0}", action.getUsername());
		
		Identity.instance().getCredentials().setUsername(action.getUsername());
		Identity.instance().getCredentials().setPassword(action.getPassword());
		Identity.instance().tryLogin();
		
		if(Identity.instance().isLoggedIn()) {
			return new AuthenticateResult(ServletContexts.instance().getRequest().getSession().getId());
		}
		else {
			return AuthenticateResult.FAILED;
		}
	}

	@Override
	public Class<AuthenticateAction> getActionType() {
		return AuthenticateAction.class;
	}

	@Override
	public void rollback(AuthenticateAction action, AuthenticateResult result,
			ExecutionContext context) throws ActionException {
	}
	
}