package org.fedorahosted.flies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.entity.Project;
import org.fedorahosted.flies.entity.ProjectSeries;
import org.fedorahosted.flies.entity.ProjectTarget;
import org.fedorahosted.flies.entity.locale.Locale;
import org.fedorahosted.flies.entity.resources.Document;
import org.fedorahosted.flies.entity.resources.DocumentTarget;
import org.fedorahosted.flies.entity.resources.TextUnit;
import org.fedorahosted.flies.entity.resources.TextUnitTarget;
import org.fedorahosted.flies.entity.resources.TextUnitTarget.Status;
import org.fedorahosted.flies.projects.publican.PublicanProjectAdapter;
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

import antlr.RecognitionException;
import antlr.TokenStreamException;

@Name("debugDataInitialization")
public class DebugDataInitialization {


   @In
   EntityManager entityManager;

   @Logger
   Log log;
   
   @Observer("org.jboss.seam.postInitialization")
   @Transactional
   public void initializeDebugData() {
	   log.info("*************************** start observing!");
	   try{
		   Project project = (Project) entityManager.createQuery("Select p from Project p where p.uname = :uname")
		   				.setParameter("uname", "deploymentguide").getSingleResult();
		   log.info("Data already exists");
		   return;
	   }
	   catch(NoResultException e){
		   // continue
	   }
	   log.info("Delegate is of type {0}", entityManager.getDelegate().getClass());

	   Locale loc;
	   try{
		   loc = (Locale) entityManager.createQuery("Select l from Locale l where l.localeId = :id")
		   				.setParameter("id", "gu-IN").getSingleResult();
		   log.info("Found locale");
	   }
	   catch(NoResultException e){
		   loc = new Locale();
		   loc.setLocaleId("gu-IN");
		   entityManager.persist(loc);
	   }
	   final Locale locale = loc;
	   
	   Project project = new Project();
	   project.setName("RHEL Deployment Guide");
	   project.setUname("deploymentguide");
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
	   
	   File basePath = new File("/home/asgeirf/projects/Deployment_Guide");
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
							tu.setDocument(template);
							tu.setContent(message.getMsgid());
							tu.setDocumentRevision(template.getRevision());
							tu.setObsolete(message.isObsolete());
							entityManager.persist(tu);
							
							if(foundTargetLangResource){
								TextUnitTarget target = new TextUnitTarget();
								target.setLocale(locale);
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
		   catch(FileNotFoundException e){
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