package org.fedorahosted.flies.core.action;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Name("projectIterationHome")
@Scope(ScopeType.EVENT)
public class ProjectIterationHome extends SlugHome<HProjectIteration>{
	
	private String slug;
	private String projectSlug;
	
	@Logger
	Log log;
	
	@In(create=true)
	ProjectDAO projectDAO;
	
	@Override
	protected HProjectIteration createInstance() {
		HProjectIteration iteration = new HProjectIteration();
		iteration.setProject((HIterationProject) projectDAO.getBySlug(projectSlug));
		return iteration;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getSlug() {
		return slug;
	}
	
	public String getProjectSlug() {
		return projectSlug;
	}
	
	public void setProjectSlug(String projectSlug) {
		this.projectSlug = projectSlug;
	}
	
	public void validateSuppliedId(){
		getInstance(); // this will raise an EntityNotFound exception
					   // when id is invalid and conversation will not
		               // start
	}

	public void validateProjectSlug() {
		if(projectDAO.getBySlug(projectSlug) == null) {
			throw new EntityNotFoundException("no entity with slug " + projectSlug);
		}
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
    		getEntityManager().createQuery("from HProjectIteration t where t.slug = :slug and t.project.slug = :projectSlug")
    		.setParameter("slug", slug)
    		.setParameter("projectSlug", projectSlug).getSingleResult();
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
		return super.persist();
	}
	
	public void cancel(){}

	@Override
	public Object getId() {
		return projectSlug+"/"+slug;
	}

	@Override
	public NaturalIdentifier getNaturalId() {
		return Restrictions.naturalId()
			.set("slug", slug)
			.set("project", projectDAO.getBySlug(projectSlug));
	}
	
	@Override
	public boolean isIdDefined() {
		return slug != null && projectSlug != null;
	}
	
}
