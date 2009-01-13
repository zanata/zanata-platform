package org.fedorahosted.flies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.entity.Project;
import org.fedorahosted.flies.entity.ProjectSeries;
import org.fedorahosted.flies.entity.ProjectTarget;
import org.fedorahosted.flies.entity.resources.Document;
import org.fedorahosted.flies.entity.resources.TextUnit;
import org.fedorahosted.flies.entity.resources.TextUnitTarget;
import org.fedorahosted.flies.entity.resources.TextUnitTarget.Status;
import org.fedorahosted.flies.projects.publican.PublicanProjectAdapter;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
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
		   if(guResources.contains(resource)){
			   poFile= new File(new File(basePath, adapter.getResourceBasePath("gu-IN")), resource);
			   foundTargetLangResource = true;
		   }
		   else{
			   poFile= new File(new File(basePath, adapter.getResourceBasePath()), resource);
			   foundTargetLangResource = false;
		   }
		   
		   
		   log.info("Importing resource {0}", resource);
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
								target.setDocumentRevision(template.getRevision());
								Status status = message.isFuzzy() ? Status.ForReview : Status.Approved;
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
		}
		   
	   }
	   
	   entityManager.flush();
	   log.info("*************************** end observing!");
   }
   
}