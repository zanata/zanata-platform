package org.fedorahosted.flies.core.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.FliesInit;
import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectSeries;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.core.model.Repository;
import org.fedorahosted.flies.core.model.ResourceCategory;
import org.fedorahosted.flies.projects.publican.PublicanProjectAdapter;
import org.fedorahosted.flies.repository.model.Document;
import org.fedorahosted.flies.repository.model.DocumentTarget;
import org.fedorahosted.flies.repository.model.TextUnit;
import org.fedorahosted.flies.repository.model.TextUnitTarget;
import org.fedorahosted.flies.repository.model.AbstractTextUnitTarget.Status;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.jboss.serial.util.HashStringUtil;

import com.ibm.icu.util.ULocale;

import antlr.RecognitionException;
import antlr.TokenStreamException;

@Name("debugDataInitialization")
public class DebugDataInitialization {

	@In
	EntityManager entityManager;

	@Logger
	Log log;

	@Observer(FliesInit.EVENT_Flies_Startup)
	@Transactional
	public void initializeDebugData() {
		log.info("*************************** start observing!");
		try {
			Project project = (Project) entityManager.createQuery(
					"Select p from Project p where p.slug = :slug")
					.setParameter("slug", "deploymentguide").getSingleResult();
			log.info("Data already exists");
			return;
		} catch (NoResultException e) {
			// continue
		}
		log.info("Delegate is of type {0}", entityManager.getDelegate()
				.getClass());

		FliesLocale loc;
		try {
			loc = (FliesLocale) entityManager.createQuery(
					"Select l from FliesLocale l where l.id = :id")
					.setParameter("id", "gu-IN").getSingleResult();
			log.info("Found locale");
		} catch (NoResultException e) {
			loc = new FliesLocale(new ULocale("gu", "IN"));
			entityManager.persist(loc);
		}

		ResourceCategory cat;
		cat = entityManager.find(ResourceCategory.class, 1L);
		if (cat == null) {
			cat = new ResourceCategory();
			cat.setName("Documentation");
			entityManager.persist(cat);
		}

		final ResourceCategory category = cat;
		final FliesLocale locale = loc;

		Repository repo1 = new Repository();
		repo1.setName("Flies");
		//repo1.setSlug("flies");
		repo1.setUrl("ssh://hg.fedorahosted.org//hg/flies");
		entityManager.persist(repo1);

		Repository repo2 = new Repository();
		repo2.setName("Transifex");
		//repo2.setSlug("transifex");
		repo2.setUrl("http://code.transifexhg.org/mainline");
		entityManager.persist(repo2);

		Repository repo3 = new Repository();
		repo3.setName("Fedora git");
		//repo3.setSlug("fedora");
		repo3.setUrl("ssh://git.fedorahosted.org/git");
		entityManager.persist(repo3);

		Project project = new Project();
		project.setName("RHEL Deployment Guide");
		project.setSlug("deploymentguide");
		project
				.setDescription("A comprehensive manual for Red Hat Enterprise Linux");
		entityManager.persist(project);

		ProjectSeries series = new ProjectSeries();
		series.setName("5.x");
		series.setProject(project);
		entityManager.persist(series);

		ProjectTarget projectTarget = new ProjectTarget();
		projectTarget.setName("5.3");
		projectTarget.setSlug("5dot3");
		projectTarget.setProject(project);
		projectTarget.setProjectSeries(series);
		entityManager.persist(projectTarget);

		File basePath = new File(
				"/home/asgeirf/jdev/jboss-4.2.3.GA/server/default/data/Deployment_Guide");
		if(!basePath.exists()){
			log.error("Couldnt find test dir '{0}', skipping...", basePath.getAbsolutePath());
			return;
		}
		PublicanProjectAdapter adapter = new PublicanProjectAdapter(basePath);
		log.info(adapter.getBrandName());

		List<String> guResources = adapter.getResources("gu-IN");
		log.info("gu-IN resources \n{0}", StringUtils.join(guResources, "\n"));
		log.info("template resources \n{0}", StringUtils.join(adapter
				.getResources(), "\n"));
		
		for (String resource : adapter.getResources()) {
			final Document template = new Document();
			template.setRevision(1);
			template.setName(resource);
			template.setProject(project);
			template.setProjectTarget(projectTarget);
			template.setResourceCategory(category);
			template.setContentType("pot");
			entityManager.persist(template);

			File poFile;
			final boolean foundTargetLangResource;
			String poResourceName = resource
					.substring(0, resource.length() - 1);
			if (guResources.contains(poResourceName)) {
				poFile = new File(new File(basePath, adapter
						.getResourceBasePath("gu-IN")), poResourceName);
				foundTargetLangResource = true;
				DocumentTarget dTarget = new DocumentTarget();
				dTarget.setTemplate(template);
				dTarget.setLocale(locale);
				entityManager.persist(dTarget);
			} else {
				poFile = new File(new File(basePath, adapter
						.getResourceBasePath()), resource);
				foundTargetLangResource = false;
			}

			log.info("Importing {0} from {1}", resource, poFile);
			
			try {
				
				MessageStreamParser parser = new MessageStreamParser(poFile);
				while(parser.hasNext()){
					Message message = parser.next();
					if (!message.isHeader()) {
						// create Template...
						TextUnit tu = new TextUnit();
						if(message.getMsgid() == null){
							log.debug("skipping entry with empty msgid \n{0}", message);
							return;
						}
						String rId = String.valueOf(HashStringUtil.hashName(message.getMsgid()));
						tu.setResourceId(rId);
						tu.setDocument(template);
						tu.setContent(message.getMsgid());
						tu.setDocumentRevision(template.getRevision());
						tu.setObsolete(message.isObsolete());
						entityManager.persist(tu);

						if (foundTargetLangResource) {
							TextUnitTarget target = new TextUnitTarget();
							target.setLocale(locale);
							target.setDocument(template);
							target.setDocumentRevision(template
									.getRevision());
							Status status = message.isFuzzy() ? Status.ForReview
									: Status.Approved;
							if (message.getMsgstr().isEmpty()) {
								status = Status.New;
							}
							target.setStatus(status);
							target.setTemplate(tu);
							target.setContent(message.getMsgstr());
								
							try{
								validate(target);
								entityManager.persist(target);
							}
							catch(MyValidationException e)
							{
								log.info("got exception. skipping....");
							}
						}
					}
				}

			} catch (IOException e) {
				log.error(e);
			} catch (ParseException e) {
				log.error("ParseException in file {1}: {0}", e.getMessage(),
						poFile.getName());
			}

		}

		entityManager.flush();
		log.info("*************************** end observing!");
	}

	private void validate(TextUnitTarget instance){
		ClassValidator<TextUnitTarget> v = new ClassValidator(TextUnitTarget.class);
		InvalidValue[] iv = v.getInvalidValues(instance);
		for (InvalidValue value : iv) {
			log.info("Invalid value {0}: {1}", value.getPropertyName(), value.getMessage());
		}
		if(iv.length >0){
			throw new MyValidationException();
		}
	}
	
	private static class MyValidationException extends RuntimeException{
	}

}
