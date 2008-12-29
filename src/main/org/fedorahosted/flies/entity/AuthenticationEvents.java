package org.fedorahosted.flies.entity;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("authenticationEvents")
public class AuthenticationEvents {

	@Logger
	Log log;

	@Observer(JpaIdentityStore.EVENT_USER_AUTHENTICATED)
	public void loginSuccessful(Account account) {
		log.info("Member {0} authenticated with person {1}", account.getUsername(), account.getPerson());

		Contexts.getSessionContext().set("authenticatedPerson",
				account.getPerson());
	}
	
}
