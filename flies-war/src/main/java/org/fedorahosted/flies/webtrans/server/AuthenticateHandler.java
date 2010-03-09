package org.fedorahosted.flies.webtrans.server;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.core.model.HPerson;
import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.model.PersonId;
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
		
		if(!Identity.instance().isLoggedIn()) {
			Identity.instance().getCredentials().setUsername(action.getUsername());
			Identity.instance().getCredentials().setPassword(action.getPassword());
			
		}
		String loggedIn = Identity.instance().login();
		
		if("loggedIn".equals(loggedIn)) {
			SessionId sessionId = retrieveSessionId();
			Person person = retrievePerson();
			
			// TODO pass along permissions and roles
			
			return new AuthenticateResult(sessionId, person);
		}
		else{
			return AuthenticateResult.FAILED;
		}
		
	}
	
	public static SessionId retrieveSessionId() {
		return new SessionId(ServletContexts.instance().getRequest().getSession().getId());
	}
	

	@Override
	public Class<AuthenticateAction> getActionType() {
		return AuthenticateAction.class;
	}

	@Override
	public void rollback(AuthenticateAction action, AuthenticateResult result,
			ExecutionContext context) throws ActionException {
	}
	
	public static PersonId retrievePersonId(){
		HPerson authenticatedPerson = (HPerson) Contexts.getSessionContext().get("authenticatedPerson");
		return new PersonId(authenticatedPerson.getAccount().getUsername());
	}
	public static Person retrievePerson(){
		HPerson authenticatedPerson = (HPerson) Contexts.getSessionContext().get("authenticatedPerson");
		return new Person( new PersonId(authenticatedPerson.getAccount().getUsername()), authenticatedPerson.getName());
	}
	
}