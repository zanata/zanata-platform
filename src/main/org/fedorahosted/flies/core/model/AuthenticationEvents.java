package org.fedorahosted.flies.core.model;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
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

	@In
	EntityManager entityManager;
	
	@Observer(JpaIdentityStore.EVENT_USER_AUTHENTICATED)
	public void loginSuccessful(Account account) {
		log.info("Member {0} authenticated with person {1}", account.getUsername(), account.getPerson());

		Contexts.getSessionContext().set("authenticatedPerson",
				account.getPerson());
	}

	@Observer(JpaIdentityStore.EVENT_USER_CREATED)
	public void createSuccessful(Account account) {
		Person p = new Person();
		p.setName(account.getName());
		p.setPersonId(account.getUsername());
		p.setAccount(account);
		entityManager.persist(p);
		account.setPerson(p);
		entityManager.persist(account);
		log.info("Created person {1}  with for account {0}", account.getUsername(), p.getName());
	}
	
}
