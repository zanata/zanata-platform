package org.fedorahosted.flies.core.action;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.commons.lang.StringUtils;
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
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
import org.fedorahosted.tennera.jgettext.catalog.write.MessageProcessor;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

import com.ibm.icu.util.ULocale;

import antlr.RecognitionException;
import antlr.TokenStreamException;

@Name("debugDataInitialization")
public class DebugDataInitialization {


   @In
   EntityManager entityManager;

   @Logger
   Log log;
   
   //@Observer("org.jboss.seam.postInitialization")
   @Transactional
   public void initializeDebugData() {
	   log.info("*************************** start observing!");
	   try{
		   Project project = (Project) entityManager.createQuery("Select p from Project p where p.slug = :slug")
		   				.setParameter("slug", "deploymentguide").getSingleResult();
		   log.info("Data already exists");
		   return;
	   }
	   catch(NoResultException e){
		   // continue
	   }
	   log.info("Delegate is of type {0}", entityManager.getDelegate().getClass());

	   FliesLocale loc;
	   try{
		   loc = (FliesLocale) entityManager.createQuery("Select l from FliesLocale l where l.id = :id")
		   				.setParameter("id", "gu-IN").getSingleResult();
		   log.info("Found locale");
	   }
	   catch(NoResultException e){
		   loc = new FliesLocale(new ULocale("gu", "IN"));
		   entityManager.persist(loc);
	   }
	   
	   ResourceCategory cat;
	   cat = entityManager.find(ResourceCategory.class, 1L);
	   if(cat == null){
		   cat = new ResourceCategory();
		   cat.setName("Documentation");
		   entityManager.persist(cat);
	   }
	   
	   final ResourceCategory category = cat;
	   final FliesLocale locale = loc;
	   
           Repository repo1 = new Repository();
	   repo1.setName("Flies");
	   repo1.setSlug("flies");
	   repo1.setUrl("ssh://hg.fedorahosted.org//hg/flies");
	   entityManager.persist(repo1);

           Repository repo2 = new Repository();
	   repo2.setName("Transifex");
	   repo2.setSlug("transifex");
	   repo2.setUrl("http://code.transifexhg.org/mainline");
	   entityManager.persist(repo2);

	   
           Repository repo3 = new Repository();
	   repo3.setName("Fedora git");
	   repo3.setSlug("fedora");
	   repo3.setUrl("ssh://git.fedorahosted.org/git");
	   entityManager.persist(repo3);
           
           Project project = new Project();
	   project.setName("RHEL Deployment Guide");
	   project.setSlug("deploymentguide");
	   project.setShortDescription("A comprehensive manual for Red Hat Enterprise Linux");
	   entityManager.persist(project);

	   ProjectSeries series = new ProjectSeries();
	   series.setName("5.x");
	   series.setProject(project);
	   entityManager.persist(series);
	   
	   ProjectTarget target = new ProjectTarget();
	   target.setName("5.3");
	   target.setProject(project);
	   target.setProjectSeries(series);
	   entityManager.persist(target);
	   
	   File basePath = new File("/home/asgeirf/projects/gitsvn/Deployment_Guide");
	   PublicanProjectAdapter adapter = new PublicanProjectAdapter(basePath);
	   log.info(adapter.getBrandName());

	   List<String> guResources = adapter.getResources("gu-IN");
	   log.info("gu-IN resources \n{0}", StringUtils.join(guResources, "\n"));
	   log.info("template resources \n{0}", StringUtils.join(adapter.getResources(), "\n"));
	   for(String resource : adapter.getResources()){
		   final Document template = new Document();
		   template.setRevision(1);
		   template.setName(resource);
		   template.setProject(project);
		   template.setProjectTarget(target);
		   template.setResourceCategory(category);
		   template.setContentType("pot");
		   entityManager.persist(template);
		   
		   File poFile;
		   final boolean foundTargetLangResource;
		   String poResourceName = resource.substring(0, resource.length()-1);
		   if(guResources.contains(poResourceName)){
			   poFile= new File(new File(basePath, adapter.getResourceBasePath("gu-IN")), poResourceName);
			   foundTargetLangResource = true;
			   DocumentTarget dTarget = new DocumentTarget();
			   dTarget.setTemplate(template);
			   dTarget.setLocale(locale);
			   entityManager.persist(dTarget);
		   }
		   else{
			   poFile= new File(new File(basePath, adapter.getResourceBasePath()), resource);
			   foundTargetLangResource = false;
		   }
		   
		   
		   log.info("Importing {0} from {1}", resource, poFile);
		   try{
			   ExtendedCatalogParser parser = new ExtendedCatalogParser(poFile);
			   parser.catalog();
			   Catalog catalog = parser.getCatalog();
			   catalog.processMessages(new Catalog.MessageProcessor(){
					public void processMessage(Message message) {
						if(!message.isHeader()){
							// create Template...
							TextUnit tu = new TextUnit();
							tu.setResourceId(message.getMsgid());
							tu.setDocument(template);
							tu.setContent(message.getMsgid());
							tu.setDocumentRevision(template.getRevision());
							tu.setObsolete(message.isObsolete());
							entityManager.persist(tu);
							
							if(foundTargetLangResource){
								TextUnitTarget target = new TextUnitTarget();
								target.setLocale(locale);
								target.setDocument(template);
								target.setDocumentRevision(template.getRevision());
								Status status = message.isFuzzy() ? Status.ForReview : Status.Approved;
								if(message.getMsgstr().isEmpty()){
									status = Status.New;
								}
								target.setStatus(status);
								target.setTemplate(tu);
								target.setContent(message.getMsgstr());
								entityManager.persist(target);
							}
						}
					}
			   });
		   }
		   catch(IOException e){
			   log.error(e);
		   } catch (RecognitionException e) {
			   log.error(e);
		} catch (TokenStreamException e) {
			   log.error(e);
		} catch (ParseException e){
			   log.error("ParseException in file {1}: {0}", e.getMessage(), poFile.getName());
		}
		   
	   }
	   
	   entityManager.flush();
	   log.info("*************************** end observing!");
   }
   
}
