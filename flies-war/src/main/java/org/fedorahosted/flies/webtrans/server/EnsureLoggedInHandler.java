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
import org.fedorahosted.flies.gwt.rpc.EnsureLoggedInAction;
import org.fedorahosted.flies.gwt.rpc.EnsureLoggedInResult;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
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

@Name("webtrans.gwt.EnsureLoggedInHandler")
@Scope(ScopeType.STATELESS)
public class EnsureLoggedInHandler implements ActionHandler<EnsureLoggedInAction, EnsureLoggedInResult> {

	@Logger Log log;
	
	@In Session session;
	
	@Override
	public EnsureLoggedInResult execute(EnsureLoggedInAction action, ExecutionContext context)
			throws ActionException {

		if(Identity.instance().isLoggedIn()) {
			
			SessionId sessionId = AuthenticateHandler.retrieveSessionId();
			Person person = AuthenticateHandler.retrievePerson();
			
			return new EnsureLoggedInResult(sessionId, person);
		}
		else {
			return EnsureLoggedInResult.FAILED;
		}
	}

	@Override
	public Class<EnsureLoggedInAction> getActionType() {
		return EnsureLoggedInAction.class;
	}

	@Override
	public void rollback(EnsureLoggedInAction action, EnsureLoggedInResult result,
			ExecutionContext context) throws ActionException {
	}
	
}