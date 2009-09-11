package org.fedorahosted.flies.core.action;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.core.model.Community;
import org.fedorahosted.flies.core.model.HPerson;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;

@Name("communityHome")
@Scope(ScopeType.CONVERSATION)
public class CommunityHome extends SlugHome<Community>{

	@Override
	@Restrict("#{identity.loggedIn}")
	protected Community createInstance() {
		Community instance = super.createInstance();
		instance.setOwner(getEntityManager().find(HPerson.class, 1l));
		return instance;
	}

	@Begin(join = true)
	public void validateSuppliedId(){
		getInstance(); // this will raise an EntityNotFound exception
					   // when id is invalid and conversation will not
		               // start
		Conversation c = Conversation.instance();
		c.setDescription(getInstance().getName());
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
    		getEntityManager().createQuery("from Community c where c.slug = :slug")
    		.setParameter("slug", slug).getSingleResult();
    		return false;
    	}
    	catch(NoResultException e){
    		// pass
    	}
    	return true;
	}
	
	@Override
	public String persist() {
		if(!validateSlug(getInstance().getSlug(), "slug"))
			return null;
		// TODO Auto-generated method stub
		return super.persist();
	}
	
	public void cancel(){}
}
