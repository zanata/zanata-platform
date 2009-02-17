package org.fedorahosted.flies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.entity.Project;
import org.fedorahosted.flies.entity.Repository;
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
	   
           Repository repo1 = new Repository();
	   repo1.setName("Flies");
	   repo1.setUname("flies");
	   repo1.setUrl("ssh://hg.fedorahosted.org//hg/flies");
	   entityManager.persist(repo1);

           Repository repo2 = new Repository();
	   repo2.setName("Transifex");
	   repo2.setUname("transifex");
	   repo2.setUrl("http://code.transifexhg.org/mainline");
	   entityManager.persist(repo2);

	   
           Repository repo3 = new Repository();
	   repo3.setName("Fedora git");
	   repo3.setUname("fedora");
	   repo3.setUrl("ssh://git.fedorahosted.org/git");
	   entityManager.persist(repo3);
           
           Project project = new Project();
	   project.setName("RHEL Deployment Guide");
	   project.setUname("deploymentguide");
	   project.setShortDescription("A comprehensive manual for Red Hat Enterprise Linux");
	   entityManager.persist(project);

	   ProjectSeries series = new ProjectSeries();
	   series.setName("default");
	   series.setProject(project);
	   entityManager.persist(series);
	   
	   ProjectTarget target = new ProjectTarget();
	   target.setName("5.3");
	   target.setProject(project);
	   target.setProjectSeries(series);
	   entityManager.persist(target);
	   
	   File basePath = new File("/home/jamesni/Deployment_Guide");
	   PublicanProjectAdapter adapter = new PublicanProjectAdapter(basePath);
	   log.info(adapter.getBrandName());
	   log.info("*************************** end observing!");
      // Do your initialization here

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
							entityManager.persist(tu);
							
							if(foundTargetLangResource){
								TextUnitTarget target = new TextUnitTarget();
								target.setDocumentRevision(template.getRevision());
								target.setStatus(Status.Approved);
								target.setTemplate(tu);
								target.setContent(message.getMsgstr());
								entityManager.persist(target);
							}
						}
			    		log.info(message.getMsgid());
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
   }
   
}
