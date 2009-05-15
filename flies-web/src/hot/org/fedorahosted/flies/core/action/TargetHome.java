package org.fedorahosted.flies.core.action;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.core.model.ResourceCategory;
import org.fedorahosted.flies.repository.model.Document;
import org.fedorahosted.flies.repository.model.AbstractTextUnitTarget.Status;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.hibernate.Session;
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
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;

@Name("targetHome")
@Scope(ScopeType.CONVERSATION)
public class TargetHome extends EntityHome<ProjectTarget> {
	
	@In ProjectHome projectHome;
	
	@Logger
	Log log;
	
	@Out(required = false)
	private List<ResourceCategory> targetCategories;


	@Begin(join = true)
	public void validateSuppliedId(){
		projectHome.validateSuppliedId();
		getInstance(); // this will raise an EntityNotFound exception
					   // when id is invalid and conversation will not
		               // start
		Conversation c = Conversation.instance();
		c.setDescription(getInstance().getName());
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
		int notApproved = (int) (total - approved);
		int app = (int) (approved - 0);
		TranslationStatistics stats = new TranslationStatistics(app,
				notApproved, 0, 0);
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
		int notApproved = (int) (total - approved);
		int app = (int) (approved - 0);
		TranslationStatistics stats = new TranslationStatistics(app,
				notApproved, 0, 0);
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
	
	
	
	
}
