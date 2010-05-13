package org.fedorahosted.flies.action;

import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.model.HAccount;
import org.fedorahosted.flies.model.HIterationProject;
import org.fedorahosted.flies.model.HPerson;
import org.fedorahosted.flies.model.HProjectIteration;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
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
@Scope(ScopeType.EVENT)
public class ProjectHome extends SlugHome<HIterationProject> {

	private String slug;
	
	@In(required=false, value=JpaIdentityStore.AUTHENTICATED_USER) 
	HAccount authenticatedAccount;
	
	@Override
	protected HIterationProject loadInstance() {
		Session session = (Session) getEntityManager().getDelegate();
		return (HIterationProject) session.createCriteria(getEntityClass())
		.add( Restrictions.naturalId()
		        .set("slug", getSlug())
		    ).setCacheable(true)
		    .uniqueResult();
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
    		getEntityManager().createQuery("from HProject p where p.slug = :slug")
    		.setParameter("slug", slug).getSingleResult();
    		return false;
    	}
    	catch(NoResultException e){
    		// pass
    	}
    	return true;
	}
	
	@Override
	@Restrict("#{s:hasRole('admin')}")
	public String persist() {
		
		if(!validateSlug(getInstance().getSlug(), "slug"))
			return null;
		
		if(authenticatedAccount != null){
			HPerson currentPerson = getEntityManager().find(HPerson.class, authenticatedAccount.getPerson().getId());
			if(currentPerson != null)
				getInstance().getMaintainers().add(currentPerson);
		}
		
		String retValue = super.persist();

		return retValue;
	}

	public List<HProjectIteration> getActiveIterations(){
		return getEntityManager().createQuery(
				"from HProjectIteration t where t.project.slug = :projectSlug and t.active = true")
				.setParameter("projectSlug", slug)
				.getResultList();
	}

	public List<HProjectIteration> getRetiredIterations(){
		return getEntityManager().createQuery(
				"from HProjectIteration t where t.project.slug = :projectSlug and t.active = false")
				.setParameter("projectSlug", slug)
				.getResultList();
	}
	
	public void cancel(){}
	
	public String getSlug() {
		return slug;
	}
	
	public void setSlug(String slug) {
		this.slug = slug;
	}
	
	@Override
	public boolean isIdDefined() {
		return slug != null;
	}

	@Override
	public NaturalIdentifier getNaturalId() {
		return Restrictions.naturalId().set("slug", slug);
	}
	
	@Override
	public Object getId() {
		return slug;
	}
	
}
