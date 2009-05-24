package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.model.Account;
import org.fedorahosted.flies.core.model.Person;
import org.fedorahosted.flies.core.model.Project;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("projectHome")
@Scope(ScopeType.CONVERSATION)
public class ProjectHome extends SlugHome<Project> {

	@In(required=false, value=JpaIdentityStore.AUTHENTICATED_USER) 
	Account authenticatedAccount;
	
	
	@Begin(join = true)
	public void validateSuppliedId(){
		getInstance(); // this will raise an EntityNotFound exception
					   // when id is invalid and conversation will not
		               // start
		Conversation c = Conversation.instance();
		c.setDescription(getInstance().getSlug());
	}
	
	
	@Override
	@Restrict("#{identity.loggedIn}")
	public String persist() {
		if(authenticatedAccount != null){
			Person currentPerson = getEntityManager().find(Person.class, authenticatedAccount.getPerson().getId());
			if(currentPerson != null)
				getInstance().getMaintainers().add(currentPerson);
		}
		return super.persist();
	}

	public void cancel(){}
}
