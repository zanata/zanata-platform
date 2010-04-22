package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowHistory;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.hibernate.Session;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

@Name("documentsService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/documents")
public class DocumentsService {

	@PathParam("projectSlug")
	private String projectSlug;

	@PathParam("iterationSlug")
	private String iterationSlug;

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uri;

	@Logger
	private Log log;

	@In
	private DocumentDAO documentDAO;

	@In
	private ProjectContainerDAO projectContainerDAO;

	@In
	private DocumentConverter documentConverter;

	@In
	private Session session;

	@POST
	@Consumes( { MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENTS_JSON,
			MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response post(Documents documents) {
		log.debug("HTTP POST {0} : \n{1}", request.getRequestURL(), documents);
		HProjectContainer hContainer = projectContainerDAO.getBySlug(
				projectSlug, iterationSlug);
		if (hContainer == null)
			return containerNotFound();
		Map<String, HDocument> docMap = hContainer.getDocuments();
		for (Document doc : documents.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
			HDocument hDoc = docMap.get(doc.getId());
			if (hDoc == null) {
				log
						.info("POST creating new HDocument with id {0}", doc
								.getId());
				hDoc = new HDocument(doc);
				hDoc.setRevision(0);
				hDoc.setProject(hContainer);
			} else {
				log.info("POST updating HDocument with id {0}", doc.getId());
			}

			docMap.put(hDoc.getDocId(), hDoc);
			// TODO handle invalid data. See put()
			session.save(hDoc);
			documentConverter.copy(doc, hDoc);
		}
		session.flush();
		return Response.ok().build();
	}

	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENTS_JSON })
	public Response get() {
		log.debug("HTTP GET {0}", request.getRequestURL());
		URI baseUri = uri.getBaseUri();
		URI iterationUri = baseUri.resolve(URIHelper.getIteration(projectSlug,
				iterationSlug));

		HProjectContainer hContainer = projectContainerDAO.getBySlug(
				projectSlug, iterationSlug);
		if (hContainer == null)
			return containerNotFound();

		Collection<HDocument> hdocs = hContainer.getDocuments().values();
		Documents result = new Documents();

		for (HDocument hDocument : hdocs) {
			Document doc = hDocument.toDocument(true);

			URI docUri = baseUri.resolve(URIHelper.getDocument(projectSlug,
					iterationSlug, doc.getId()));
			documentConverter.addLinks(doc, docUri, iterationUri);

			result.getDocuments().add(doc);
		}
		log.debug("HTTP GET result :\n" + result);
		return Response.ok(result).build();
	}

	@PUT
	@Consumes( { MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENTS_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(Documents documents) {
		log.debug("HTTP PUT {0} : \n{1}", request.getRequestURL(), documents);
		HProjectContainer hContainer = projectContainerDAO.getBySlug(
				projectSlug, iterationSlug);
		if (hContainer == null)
			return containerNotFound();
		Map<String, HDocument> docMap = hContainer.getDocuments();
		// any Docs still in this set at the end will be marked obsolete
		Set<HDocument> obsoleteDocs = new HashSet<HDocument>(docMap.values());
		ClassValidator<HDocument> docValidator = new ClassValidator<HDocument>(
				HDocument.class);
		StringBuilder sb = new StringBuilder();

		for (Document doc : documents.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
			HDocument hDoc = documentDAO.getByDocId(hContainer, doc.getId());
			if (hDoc == null) {
				log
						.debug("PUT creating new HDocument with id {0}", doc
								.getId());
				hDoc = new HDocument(doc);
				hDoc.setRevision(0);
				hDoc.setProject(hContainer);
			} else {
				log.debug("PUT updating HDocument with id {0}", doc.getId());
				obsoleteDocs.remove(hDoc);
				hDoc.setObsolete(false);
			}
			docMap.put(hDoc.getDocId(), hDoc);
			try {
				documentConverter.copy(doc, hDoc);
				InvalidValue[] invalidValues = docValidator
						.getInvalidValues(hDoc);
				if (invalidValues.length != 0) {
					String message = "Document with id '" + doc.getId()
							+ "' is invalid: " + Arrays.asList(invalidValues);
					obsoleteDocs.add(hDoc);
					log.error(message);
					sb.append(message);
					sb.append('\n');
				} else {
					session.save(hDoc);
				}
			} catch (InvalidStateException e) {
				String message = "Document with id '" + doc.getId()
						+ "' is invalid: "
						+ Arrays.asList(e.getInvalidValues());
				log.warn(message + '\n' + doc, e);
				return Response.status(Status.BAD_REQUEST).entity(message)
						.build();
			}
			// session.save(hDoc);
		}
		for (HDocument hDoc : obsoleteDocs) {
			// mark document resources as obsolete
			for (HTextFlow htf : hDoc.getResources()) {
				HTextFlowHistory history = new HTextFlowHistory(htf);
				htf.getHistory().put(htf.getRevision(), history);
				htf.setObsolete(true);
			}
			hDoc.setObsolete(true);
			hDoc.setRevision(hDoc.getRevision() + 1);
			docMap.remove(hDoc.getId());
		}
		session.flush();
		return Response.ok(sb.toString()).build();
	}

	private Response containerNotFound() {
		return Response.status(Status.NOT_FOUND).entity(
				"Project Container not found").build();
	}

}
