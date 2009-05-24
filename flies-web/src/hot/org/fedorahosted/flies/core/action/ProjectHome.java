package org.fedorahosted.flies.core.action;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;

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
import org.jboss.seam.faces.FacesMessages;
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

	public void verifySlugAvailable(ValueChangeEvent e) {
	    String slug = (String) e.getNewValue();
	    validateSlug(slug, e.getComponent().getId());
	}
	
	public boolean validateSlug(String slug, String componentId){
	    if (!isSlugAvailable(slug)) {
	    	FacesMessages.instance().addToControl(
	    			componentId, "This slug is not available");
	    	return false;
	    }
	    return true;
	}
	
	public boolean isSlugAvailable(String slug) {
    	try{
    		getEntityManager().createQuery("from Project p where p.slug = :slug")
    		.setParameter("slug", slug).getSingleResult();
    		return false;
    	}
    	catch(NoResultException e){
    		// pass
    	}
    	return true;
	}
	
	@Override
	@Restrict("#{identity.loggedIn}")
	public String persist() {
		
		if(!validateSlug(getInstance().getSlug(), "slug"))
			return null;
		
		if(authenticatedAccount != null){
			Person currentPerson = getEntityManager().find(Person.class, authenticatedAccount.getPerson().getId());
			if(currentPerson != null)
				getInstance().getMaintainers().add(currentPerson);
		}
		return super.persist();
	}

	public void cancel(){}
}
