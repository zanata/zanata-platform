package org.fedorahosted.flies;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.entity.Person;
import org.fedorahosted.flies.entity.Project;
import org.fedorahosted.flies.entity.ProjectSeries;
import org.fedorahosted.flies.entity.ProjectTarget;
import org.fedorahosted.flies.entity.resources.DocumentTemplate;
import org.fedorahosted.flies.projects.publican.PublicanProjectAdapter;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

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
	   
	   Project project = (Project) entityManager.createQuery("Select p from Project p where p.uname = :uname")
	   				.setParameter("uname", "deploymentguide").getSingleResult();
	   if(project == null){
		   
		   project = new Project();
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
		   
		   File basePath = new File("/home/asgeirf/projects/Deployment_Guide");
		   PublicanProjectAdapter adapter = new PublicanProjectAdapter(basePath);
		   log.info(adapter.getBrandName());
		   log.info("*************************** end observing!");
	      // Do your initialization here
	
		   for(String resource : adapter.getResources()){
			   DocumentTemplate template = new DocumentTemplate();
			   template.setRevision(1);
			   template.setName(resource);
			   template.setProject(project);
			   template.setProjectTarget(target);
			   entityManager.persist(template);
		   }
		   
		   entityManager.flush();
	   }
   }
   
   
   
}