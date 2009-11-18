package org.fedorahosted.flies.core.action;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.core.model.HCommunity;
import org.fedorahosted.flies.core.model.HPerson;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;

@Name("communityHome")
@Scope(ScopeType.CONVERSATION)
public class CommunityHome extends SlugHome<HCommunity>{

	private String slug;
	
	@Override
	@Restrict("#{identity.loggedIn}")
	protected HCommunity createInstance() {
		HCommunity instance = super.createInstance();
		instance.setOwner(getEntityManager().find(HPerson.class, 1l));
		return instance;
	}

	public void validateSuppliedId(){
		getInstance(); // this will raise an EntityNotFound exception
					   // when id is invalid and conversation will not
		               // start
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
    		getEntityManager().createQuery("from HCommunity c where c.slug = :slug")
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
	
	
	@Override
	public NaturalIdentifier getNaturalId() {
		return Restrictions.naturalId().set("slug", slug);
	}
	
	@Override
	public boolean isIdDefined() {
		return slug != null;
	}
	
	public String getSlug() {
		return slug;
	}
	
	public void setSlug(String slug) {
		this.slug = slug;
	}
	
	@Override
	public Object getId() {
		return slug;
	}
}
