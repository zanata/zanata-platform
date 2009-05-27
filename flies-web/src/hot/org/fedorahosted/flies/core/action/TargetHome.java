package org.fedorahosted.flies.core.action;

import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectSeries;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.core.model.ResourceCategory;
import org.fedorahosted.flies.repository.model.Document;
import org.fedorahosted.flies.repository.model.AbstractTextUnitTarget.Status;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;

@Name("targetHome")
@Scope(ScopeType.CONVERSATION)
public class TargetHome extends MultiSlugHome<ProjectTarget>{
	
	@Logger
	Log log;
	
	@In(value="#{projectHome.instance}", scope=ScopeType.CONVERSATION, required=false)
	Project project;
	
	@In(create=true)
	ProjectDAO projectDAO;
	
	@Out(required = false)
	private List<ResourceCategory> targetCategories;

	@Override
	protected ProjectTarget createInstance() {
		ProjectTarget target = new ProjectTarget();
		target.setProject(project);
		return target;
	}

	private String managementType;
	
	public String getManagementType() {
		return managementType;
	}
	
	public void setManagementType(String managementType) {
		this.managementType = managementType;
	}
	
	@Begin(join=true)
	public void validateSuppliedId(){
		getInstance(); // this will raise an EntityNotFound exception
					   // when id is invalid and conversation will not
		               // start
		Conversation c = Conversation.instance();
		c.setDescription(getMultiSlug());
	}

	@Override
	protected ProjectTarget loadInstance() {
		Session session = (Session) getEntityManager().getDelegate();
		return (ProjectTarget) session.createCriteria(ProjectTarget.class)
			.add( Restrictions.naturalId()
		        .set("project", projectDAO.getBySlug( getSlug(0) ) )
		        .set("slug", getId() )
		    )
			.setCacheable(true).uniqueResult();
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
    		getEntityManager().createQuery("from ProjectTarget t where t.slug = :slug and t.project = :project")
    		.setParameter("slug", slug)
    		.setParameter("project", getInstance().getProject()).getSingleResult();
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
	
	public List<ProjectSeries> getAvailableProjectSeries(){
		return getEntityManager().createQuery("from ProjectSeries where project = :project")
			.setParameter("project", getInstance().getProject()).getResultList();
		
	}

	@SuppressWarnings("unchecked")
	@Factory("targetCategories")
	public void getCategories() {
		log.debug("calling getCategories");
		targetCategories = getEntityManager()
				.createQuery(
						"select distinct d.resourceCategory from Document d where d.projectTarget = :pt")
				.setParameter("pt", getInstance()).getResultList();
	}

	public TranslationStatistics getStatisticsForCategory(
			ResourceCategory category) {

		Session session = (Session) getEntityManager().getDelegate();
		// ResourceCategory category = (ResourceCategory)
		// session.load(ResourceCategory.class, 1l);

		Long approved = (Long) getEntityManager()
				.createQuery(
						"select count(*) from TextUnitTarget tut where "
								+ "tut.document.resourceCategory = :category and tut.document.projectTarget = :target"
								+ " and tut.status = :status").setParameter(
						"category", category).setParameter("status",
						Status.Approved).setParameter("target", getInstance())
				.getSingleResult();
		Long total = (Long) getEntityManager()
				.createQuery(
						"select count(*) from TextUnitTarget tut where "
								+ "tut.document.resourceCategory = :category and tut.document.projectTarget = :target")
				.setParameter("category", category).setParameter("target",
						getInstance()).getSingleResult();
		long notApproved = (total - approved);
		long app = (approved - 0);
		TranslationStatistics stats = new TranslationStatistics(app,
				notApproved, 0l, 0l);
		log
				.info("Statistics for category: {0}, {1}", category.getName(),
						stats);
		return stats;
	}

	public TranslationStatistics getStatisticsForDocument(Document document) {
		Session session = (Session) getEntityManager().getDelegate();
		// ResourceCategory category = (ResourceCategory)
		// session.load(ResourceCategory.class, 1l);

		Long approved = (Long) getEntityManager().createQuery(
				"select count(*) from TextUnitTarget tut where "
						+ "tut.document = :document "
						+ " and tut.status = :status").setParameter("document",
				document).setParameter("status", Status.Approved)
				.getSingleResult();
		Long total = (Long) getEntityManager().createQuery(
				"select count(*) from TextUnitTarget tut where "
						+ "tut.document = :document").setParameter("document",
				document).getSingleResult();
		long notApproved = (total - approved);
		long app = (approved - 0);
		TranslationStatistics stats = new TranslationStatistics(app,
				notApproved, 0l, 0l);
		log
				.info("Statistics for category: {0}, {1}", document.getName(),
						stats);
		return stats;
	}

	@SuppressWarnings("unchecked")
	public List<Document> getDocumentsForCategory(ResourceCategory category) {
		return getEntityManager()
				.createQuery(
						"select d from Document d "
								+ "where "
								+ "d.resourceCategory = :category and d.projectTarget = :target ")
				.setParameter("category", category).setParameter("target",
						getInstance()).getResultList();
	}
	
	public void cancel(){}
	
	@In PublicanImporter publicanImporter;
	
	@Override
	public String update() {
		String retValue = super.update();
		if(ManagementTypes.TYPE_LOCAL.equals(getManagementType()) && !getInstance().getLocalDirectory().isEmpty()){
			publicanImporter.process(getInstance().getLocalDirectory(), getInstance().getId());
		}
		return retValue;
	}
	
	
}
