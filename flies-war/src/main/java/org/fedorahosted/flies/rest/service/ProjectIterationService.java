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
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.core.model.HProjectSeries;
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
import org.jboss.seam.annotations.security.Restrict;

@Name("projectIterationService")
@Path("/projects/p/{projectSlug}/iterations/{iterationSlug}")
public class ProjectIterationService {

	@PathParam("projectSlug")
	private String projectSlug;

	@PathParam("iterationSlug")
	private String iterationSlug;

	@In
	ProjectDAO projectDAO;
	
	@In
	ProjectIterationDAO projectIterationDAO;
	
	@In
	Session session;
	
	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public ProjectIteration get(){

		org.fedorahosted.flies.core.model.HProjectIteration hProjectIteration = 
			projectIterationDAO.getBySlug(projectSlug, iterationSlug);
		
		if(hProjectIteration == null)
			throw new NotFoundException("No such Project Iteration");
		
		return toMini(hProjectIteration);
	}
	
	private static ProjectIteration toMini(org.fedorahosted.flies.core.model.HProjectIteration hibIt){
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
	

	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(ProjectIteration projectIteration){

		org.fedorahosted.flies.core.model.HProjectIteration hProjectIteration = 
			projectIterationDAO.getBySlug(projectSlug, projectIteration.getId());
	
		HProject hProject = projectDAO.getBySlug(projectSlug);
		
		if(hProjectIteration != null || hProject == null){
			return Response.status(409).build();
		}

		hProjectIteration = new org.fedorahosted.flies.core.model.HProjectIteration();
		hProjectIteration.setProject((HIterationProject) hProject);
		hProjectIteration.setName(projectIteration.getName());
		hProjectIteration.setSlug(projectIteration.getId());
		hProjectIteration.setDescription(projectIteration.getSummary());
		
		try{
			HProjectContainer container = new HProjectContainer();
			session.save(container);
			hProjectIteration.setContainer(container);
			hProjectIteration.setProjectSeries( (HProjectSeries) session.load(HProjectSeries.class, 1l));
			session.save(hProjectIteration);
			return Response.created( URI.create("/i/"+hProjectIteration.getSlug()) ).build();
		}
		catch(HibernateException e){
			return Response.notAcceptable(null).build();
		}
		
		
		
	}
}
