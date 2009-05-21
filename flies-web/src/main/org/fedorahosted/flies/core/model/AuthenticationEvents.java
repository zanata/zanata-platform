package org.fedorahosted.flies.core.model;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("authenticationEvents")
@Scope(ScopeType.STATELESS)
public class AuthenticationEvents {

	@Logger
	Log log;

	@Out(required=false, scope=ScopeType.SESSION)
	Person authenticatedPerson;
	
	@Observer(JpaIdentityStore.EVENT_USER_AUTHENTICATED)
	public void loginSuccessful(Account account) {
		log.info("Account {0} authenticated", account.getUsername());

		authenticatedPerson = account.getPerson();
	}

	@Observer(JpaIdentityStore.EVENT_USER_CREATED)
	public void createSuccessful(Account account) {
		log.info("Account {0} created", account.getUsername());
	}

}
