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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.dao.ProjectIterationDAO;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HResource;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentRef;
import org.fedorahosted.flies.rest.dto.DocumentRefs;
import org.fedorahosted.flies.rest.dto.Documents;
import org.fedorahosted.flies.rest.dto.Resource;
import org.hibernate.Session;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("documentService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/documents")
public class DocumentService {
	
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
	@Path("/d/{documentId}")
	@Produces({ MediaTypes.APPLICATION_FLIES_DOCUMENT_XML, MediaType.APPLICATION_JSON })
	public Document getDocument(
			@PathParam("documentId") String documentId,
			@QueryParam("includeTargets") String includeTargets) {
		
		return null;
	}

	@POST
	@Path("/d/{documentId}")
	@Consumes({ MediaTypes.APPLICATION_FLIES_DOCUMENT_XML, MediaType.APPLICATION_JSON })
	public Response updateDocument(
			@PathParam("documentId") String documentId,
			Document document) {
		return null;
	}
	
	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_DOCUMENT_XML, MediaType.APPLICATION_JSON })
	public Response addDocument(Document document) throws URISyntaxException {
		
		HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
		
		if(hProjectIteration == null)
			throw new NotFoundException("Project Iteration not found");
		
		HDocument hDoc = new HDocument(document);
		hProjectIteration.getContainer().getDocuments().add(hDoc);
		try{
			session.flush();
			for(Resource res : document.getResources()) {
				HResource hRes = HDocument.create(res);
				hRes.setDocument(hDoc);
				hDoc.getResourceTree().add(hRes);
				session.flush();
			}
			return Response.created( new URI("/d/"+hDoc.getDocId())).build();
		}
		catch(Exception e){
			return Response.notAcceptable(null).build();
		}
		
	}

	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_DOCUMENTREFS_XML, MediaType.APPLICATION_JSON })
	public DocumentRefs getDocuments() {
		HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
		
		if(hProjectIteration == null)
			throw new NotFoundException("No such Project Iteration");
		
		DocumentRefs documents = new DocumentRefs();

		for(HDocument doc : hProjectIteration.getContainer().getDocuments() ){
			documents.getDocuments().add( 
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
		
		return documents;
	}

	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML, MediaType.APPLICATION_JSON })
	public Response addDocuments(Documents documents) {
	    // TODO
	    return null;
	}

	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML, MediaType.APPLICATION_JSON })
	public Documents getAllDocuments() {
	    // TODO
	    return null;
	}

	@POST
	@Consumes({ MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML, MediaType.APPLICATION_JSON })
	public Response replace(Documents documents) {
	    // TODO
	    return null;
	}

}
