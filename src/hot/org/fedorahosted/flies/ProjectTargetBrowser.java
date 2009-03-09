package org.fedorahosted.flies;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.core.model.ResourceCategory;
import org.fedorahosted.flies.resources.model.Document;
import org.fedorahosted.flies.resources.model.AbstractTextUnitTarget.Status;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;

@Name("projectTargetBrowser")
@Scope(ScopeType.CONVERSATION)
public class ProjectTargetBrowser {
	
    @RequestParameter
    private Long targetId;

    @Out(required=false,scope=ScopeType.CONVERSATION)
    private ProjectTarget selectedTarget;

    @Out(required=false)
    private List<ResourceCategory> selectedTargetCategories;
    
    @Logger
    private Log log;
    
    @In
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
	@Factory("selectedTargetCategories")
    public void getCategories(){
    	selectedTargetCategories =  entityManager.createQuery(
    			"select distinct d.resourceCategory from Document d where d.projectTarget = :pt")
    			.setParameter("pt", selectedTarget).getResultList();
    }
    
    @Factory("selectedTarget")
    public void getSelectedTarget(){
    	selectedTarget = entityManager.find(ProjectTarget.class, targetId);
    }
    
    public TranslationStatistics getStatisticsForCategory(ResourceCategory category){
    	
    	Session session = (Session)entityManager.getDelegate();
    	//ResourceCategory category = (ResourceCategory) session.load(ResourceCategory.class, 1l);
    	
    	Long approved = (Long) entityManager.createQuery("select count(*) from TextUnitTarget tut where " +
    			"tut.document.resourceCategory = :category and tut.document.projectTarget = :target" +
    			" and tut.status = :status")
    			.setParameter("category", category)
    			.setParameter("status", Status.Approved)
    			.setParameter("target", selectedTarget).getSingleResult();
    	Long total = (Long) entityManager.createQuery("select count(*) from TextUnitTarget tut where " +
    			"tut.document.resourceCategory = :category and tut.document.projectTarget = :target")
    			.setParameter("category", category)
    			.setParameter("target", selectedTarget).getSingleResult();
    	int notApproved = (int) (total-approved);
    	int app = (int) (approved -0);
    	TranslationStatistics stats = new TranslationStatistics(app, notApproved, 0, 0);
    	log.info("Statistics for category: {0}, {1}", category.getName(), stats);
    	return stats;
    }

    public TranslationStatistics getStatisticsForDocument(Document document){
    	Session session = (Session)entityManager.getDelegate();
    	//ResourceCategory category = (ResourceCategory) session.load(ResourceCategory.class, 1l);
    	
    	Long approved = (Long) entityManager.createQuery("select count(*) from TextUnitTarget tut where " +
    			"tut.document = :document " +
    			" and tut.status = :status")
    			.setParameter("document", document)
    			.setParameter("status", Status.Approved)
    			.getSingleResult();
    	Long total = (Long) entityManager.createQuery("select count(*) from TextUnitTarget tut where " +
    			"tut.document = :document")
    			.setParameter("document", document)
    			.getSingleResult();
    	int notApproved = (int) (total-approved);
    	int app = (int) (approved -0);
    	TranslationStatistics stats = new TranslationStatistics(app, notApproved, 0, 0);
    	log.info("Statistics for category: {0}, {1}", document.getName(), stats);
    	return stats;
    }    
    
    @SuppressWarnings("unchecked")
	public List<Document> getDocumentsForCategory(ResourceCategory category){
    	return entityManager.createQuery("select d from Document d " +
    			"where " +
    			"d.resourceCategory = :category and d.projectTarget = :target ")
    			.setParameter("category", category)
    			.setParameter("target", selectedTarget).getResultList();
    }
	
}
