package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.ContentQualifier;
import org.fedorahosted.flies.rest.dto.Document;
import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

@Name("documentService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/documents/d/{documentId}")
public class DocumentService {

	@PathParam("projectSlug")
	private String projectSlug;

	@PathParam("iterationSlug")
	private String iterationSlug;

	@PathParam("documentId")
	private String documentId;

	@Context
	private UriInfo uri;

	@Logger
	private Log log;

	@In
	private DocumentConverter documentConverter;

	@In
	private DocumentDAO documentDAO;

	@In
	private ProjectIterationDAO projectIterationDAO;

	@In
	private Session session;

	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_DOCUMENT_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON,
			MediaType.APPLICATION_JSON })
	public Response get(
			@QueryParam("resources") @DefaultValue("") ContentQualifier resources) {
		HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(
				projectSlug, iterationSlug);

		if (hProjectIteration == null)
			return containerNotFound();

		String docId = URIHelper.convertFromDocumentURIId(documentId);

		HDocument hDoc = documentDAO.getByDocId(hProjectIteration, docId);

		if (hDoc == null) {
			return Response.status(Status.NOT_FOUND).entity(
					"Document not found").build();
		}

		Set<LocaleId> requestedLanguages = resources.getLanguages();
		if (resources.isAll()) {
			requestedLanguages = documentDAO.getTargetLocales(hDoc);
		}

		int requestedLevels = resources.isNone() ? 0 : Integer.MAX_VALUE;
		Document doc = hDoc.toDocument(requestedLevels);

		URI iterationUri = uri.getBaseUri().resolve(
				URIHelper.getIteration(projectSlug, iterationSlug));
		URI docUri = uri.getBaseUri().resolve(
				URIHelper.getDocument(projectSlug, iterationSlug, documentId));
		documentConverter.addLinks(doc, docUri, iterationUri);

		return Response.ok().entity(doc).tag("v-" + doc.getRevision()).build();
	}

	@PUT
	@Consumes( { MediaTypes.APPLICATION_FLIES_DOCUMENT_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON,
			MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(Document document) throws URISyntaxException {

		String hDocId = URIHelper.convertFromDocumentURIId(documentId);

		if (!document.getId().equals(hDocId)) {
			return Response.status(Status.BAD_REQUEST).entity(
					"Invalid document Id").build();
		}

		HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(
				projectSlug, iterationSlug);

		if (hProjectIteration == null)
			return containerNotFound();

		HDocument hDoc = documentDAO.getByDocId(hProjectIteration, hDocId);

		if (hDoc == null) { // it's a create operation
			log.debug("PUT creating new HDocument with id {0}", document
					.getId());
			hDoc = new HDocument(document);
			hDoc.setRevision(0);
			hDoc.setProjectIteration(hProjectIteration);

			documentConverter.copy(document, hDoc);
			hProjectIteration.getDocuments().put(hDoc.getDocId(), hDoc);
			session.save(hDoc);
			try {
				session.flush();
				return Response.created(
						uri.getBaseUri().resolve(
								URIHelper.getDocument(projectSlug,
										iterationSlug, documentId))).build();
			} catch (Exception e) {
				log.error("Invalid document content", e);
				// TODO validation on the input data
				// this could also be a server error
				return Response.status(Status.BAD_REQUEST).entity(
						"Invalid document content").build();
			}
		} else { // it's an update operation
		// documentConverter.merge(document, hDoc);
			documentConverter.copy(document, hDoc);
			hDoc.setObsolete(false);
			session.save(hDoc);
			session.flush();
			return Response.status(205).build();
		}

	}

	private Response containerNotFound() {
		return Response.status(Status.NOT_FOUND).entity(
				"Project Container not found").build();
	}

}
