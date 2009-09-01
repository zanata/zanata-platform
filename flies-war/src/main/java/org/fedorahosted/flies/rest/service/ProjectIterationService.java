package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.dao.ProjectIterationDAO;
import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectSeries;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentRef;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("projectIterationService")
@Path("/projects/p/{projectSlug}/iterations")
public class ProjectIterationService {

	@PathParam("projectSlug")
	private String projectSlug;

	@In
	ProjectDAO projectDAO;
	
	@In
	ProjectIterationDAO projectIterationDAO;
	
	@In
	Session session;
	
	@GET
	@Path("/i/{iterationSlug}")
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public ProjectIteration getIteration (
			@PathParam("iterationSlug") String iterationSlug){

		org.fedorahosted.flies.core.model.ProjectIteration hProjectIteration = 
			projectIterationDAO.getBySlug(projectSlug, iterationSlug);
		
		if(hProjectIteration == null)
			throw new NotFoundException("No such Project Iteration");
		
		return toMini(hProjectIteration);
	}
	
	private static ProjectIteration toMini(org.fedorahosted.flies.core.model.ProjectIteration hibIt){
		ProjectIteration it = new ProjectIteration();
		it.setId(hibIt.getSlug());
		it.setName(hibIt.getName());
		it.setSummary(hibIt.getDescription());
		
		for(HDocument doc : hibIt.getContainer().getDocuments() ){
			it.getDocuments().add( 
					new DocumentRef(
							new Document(
									doc.getDocId(),
									doc.getName(),
									doc.getPath(),
									doc.getContentType(),
									doc.getRevision(),
									doc.getLocale()
									)
							)
					);
		}
		
		return it;
		
	}
	

	@POST
	@Path("/i/{iterationSlug}")
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public Response updateIteration(
			@PathParam("iterationSlug") String iterationSlug,
			ProjectIteration projectIteraton){
		org.fedorahosted.flies.core.model.ProjectIteration hProjectIteration = 
			projectIterationDAO.getBySlug(projectSlug, iterationSlug);
		
		if(hProjectIteration == null)
			throw new NotFoundException("No such Project Iteration");
		
		hProjectIteration.setName(projectIteraton.getName());
		hProjectIteration.setDescription(projectIteraton.getSummary());
		
		try{
			session.flush();
			return Response.ok().build();
		}
		catch(HibernateException e){
			return Response.notAcceptable(null).build();
		}		
	}

	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public Response addIteration(
			ProjectIteration projectIteration) throws URISyntaxException{

		org.fedorahosted.flies.core.model.ProjectIteration hProjectIteration = 
			projectIterationDAO.getBySlug(projectSlug, projectIteration.getId());
	
		Project hProject = projectDAO.getBySlug(projectSlug);
		
		if(hProjectIteration != null || hProject == null){
			return Response.status(409).build();
		}

		hProjectIteration = new org.fedorahosted.flies.core.model.ProjectIteration();
		hProjectIteration.setProject((IterationProject) hProject);
		hProjectIteration.setName(projectIteration.getName());
		hProjectIteration.setSlug(projectIteration.getId());
		hProjectIteration.setDescription(projectIteration.getSummary());
		
		try{
			HProjectContainer container = new HProjectContainer();
			session.save(container);
			hProjectIteration.setContainer(container);
			hProjectIteration.setProjectSeries( (ProjectSeries) session.load(ProjectSeries.class, 1l));
			session.save(hProjectIteration);
			return Response.created( new URI("/i/"+hProjectIteration.getSlug()) ).build();
		}
		catch(HibernateException e){
			return Response.notAcceptable(null).build();
		}
		
		
		
	}
}
