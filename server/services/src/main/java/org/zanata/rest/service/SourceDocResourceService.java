/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.inject.Inject;
import javax.inject.Named;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.rest.RestUtil;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.LocaleService;
import org.zanata.util.UrlUtil;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RequestScoped
@Named("sourceDocResourceService")
@Path(SourceDocResource.SERVICE_PATH)
@Transactional
public class SourceDocResourceService implements SourceDocResource {
    private static final Logger log =
            LoggerFactory.getLogger(SourceDocResourceService.class);

    @Context
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private Request request;
    @Context
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private UriInfo uri;

    /**
     * Project Identifier.
     */
    @PathParam("projectSlug")
    private String projectSlug;

    /**
     * Project Iteration identifier.
     */
    @PathParam("iterationSlug")
    private String iterationSlug;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private DocumentService documentServiceImpl;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private ETagUtils eTagUtils;
    @Inject
    private ZanataIdentity identity;

    @Inject
    private UrlUtil urlUtil;

    @Override
    public Response head() {
        HProjectIteration hProjectIteration = retrieveAndCheckIteration(false);
        EntityTag etag =
                projectIterationDAO.getResourcesETag(hProjectIteration);
        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        return Response.ok().tag(etag).build();
    }

    @Override
    public Response get(Set<String> extensions) {
        HProjectIteration hProjectIteration = retrieveAndCheckIteration(false);
        EntityTag etag =
                projectIterationDAO.getResourcesETag(hProjectIteration);
        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        List<ResourceMeta> resources = new ArrayList<ResourceMeta>();
        for (HDocument doc : hProjectIteration.getDocuments().values()) {
            // TODO we shouldn't need this check
            if (!doc.isObsolete()) {
                ResourceMeta resource = new ResourceMeta();
                resourceUtils.transferToAbstractResourceMeta(doc, resource);
                resources.add(resource);
            }
        }
        Object entity =
                new GenericEntity<List<ResourceMeta>>(resources){};
        return Response.ok(entity).tag(etag).build();
    }

    @Override
    public Response post(Resource resource, Set<String> extensions,
            boolean copytrans) {
        identity.checkPermission(getSecuredIteration(), "import-template");
        HProjectIteration hProjectIteration = retrieveAndCheckIteration(true);
        ResourceUtils.validateExtensions(extensions); // gettext, comment
        String resourceName = resource.getName();
        if (!Pattern.matches(SourceDocResource.RESOURCE_NAME_REGEX,
                resourceName)) {
            log.warn("bad resource name in post(): {}", resourceName);
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("not a legal resource name: " + resourceName)
                    .build());
        }
        HDocument document = documentDAO
                .getByDocIdAndIteration(hProjectIteration, resourceName);
        // already existing non-obsolete document.
        if (document != null) {
            if (!document.isObsolete()) {
                // updates must happen through PUT on the actual resource
                return Response.status(Response.Status.CONFLICT)
                        .entity("A document with name " + resourceName
                                + " already exists.")
                        .build();
            }
        }
        // TODO No need for docId param since it's resource.getName()
        document = this.documentServiceImpl.saveDocument(this.projectSlug,
                this.iterationSlug, resource, extensions, copytrans);
        EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration,
                document.getDocId(), extensions);
        return Response
                .created(URI.create(
                        "r/" + resourceUtils.encodeDocId(document.getDocId())))
                .tag(etag).build();
    }

    @Deprecated
    @Override
    public Response getResource(String idNoSlash, Set<String> extensions) {
        String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return getResourceWithDocId(id, extensions);
    }

    @Override
    public Response getResourceWithDocId(String docId, Set<String> extensions) {
        log.debug("start get resource");
        if (StringUtils.isBlank(docId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("missing id").build();
        }
        HProjectIteration hProjectIteration = retrieveAndCheckIteration(false);
        ResourceUtils.validateExtensions(extensions);
        final Set<String> extSet = new HashSet<>(extensions);
        EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration,
                docId, extSet);
        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        HDocument doc =
                documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
        if (doc == null || doc.isObsolete()) {
            // TODO: return Problem DTO, https://tools.ietf.org/html/rfc7807
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("document not found").build();
        }
        Resource entity = new Resource(doc.getDocId());
        log.debug("get resource details {}", entity.toString());
        resourceUtils.transferToResource(doc, entity);
        for (HTextFlow htf : doc.getTextFlows()) {
            TextFlow tf =
                    new TextFlow(htf.getResId(), doc.getLocale().getLocaleId());
            resourceUtils.transferToTextFlow(htf, tf);
            resourceUtils.transferToTextFlowExtensions(htf,
                    tf.getExtensions(true), extensions);
            entity.getTextFlows().add(tf);
        }
        // handle extensions
        resourceUtils.transferToResourceExtensions(doc,
                entity.getExtensions(true), extensions);
        log.debug("Get resource :{}", entity.toString());
        return Response.ok().entity(entity).tag(etag)
                .lastModified(doc.getLastChanged()).build();
    }

    @Deprecated
    @Override
    public Response putResource(String idNoSlash, Resource resource,
            Set<String> extensions, boolean copytrans) {
        String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return putResourceWithDocId(resource, id, extensions, copytrans);
    }

    @Override
    public Response putResourceWithDocId(Resource resource, String docId,
            Set<String> extensions, boolean copytrans) {
        identity.checkPermission(getSecuredIteration(), "import-template");
        log.debug("start put resource");
        if (StringUtils.isBlank(docId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("missing docId").build();
        }
        Response.ResponseBuilder response;
        HProjectIteration hProjectIteration = retrieveAndCheckIteration(true);
        ResourceUtils.validateExtensions(extensions);
        HDocument document =
                this.documentDAO.getByDocIdAndIteration(hProjectIteration,
                        docId);
        if (document == null || document.isObsolete()) {
            response = Response.created(
                    UriBuilder.fromUri(urlUtil.restPath(uri.getPath()))
                            .queryParam("docId", docId).build());
        } else {
            response = Response.ok();
        }
        resource.setName(docId);
        document = this.documentServiceImpl.saveDocument(projectSlug,
                iterationSlug, resource, extensions, copytrans);
        EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration,
                document.getDocId(), extensions);
        log.debug("put resource successfully");
        return response.tag(etag).build();
    }

    @Deprecated
    @Override
    public Response deleteResource(String idNoSlash) {
        String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return deleteResourceWithDocId(id);
    }

    @Override
    public Response deleteResourceWithDocId(String docId) {
        identity.checkPermission(getSecuredIteration(), "import-template");
        if (StringUtils.isBlank(docId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("missing id").build();
        }
        HProjectIteration hProjectIteration = retrieveAndCheckIteration(true);
        EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration,
                docId, new HashSet<String>());
        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        HDocument document =
                documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
        documentServiceImpl.makeObsolete(document);
        return Response.ok().build();
    }

    @Deprecated
    @Override
    public Response getResourceMeta(String idNoSlash, Set<String> extensions) {
        String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return getResourceMetaWithDocId(id, extensions);
    }

    @Override
    public Response getResourceMetaWithDocId(String docId,
            Set<String> extensions) {
        log.debug("start to get resource meta");
        if (StringUtils.isBlank(docId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("missing id").build();
        }
        HProjectIteration hProjectIteration = retrieveAndCheckIteration(false);
        EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration,
                docId, extensions);
        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        HDocument doc =
                documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
        if (doc == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("document not found").build();
        }
        ResourceMeta entity = new ResourceMeta(doc.getDocId());
        resourceUtils.transferToAbstractResourceMeta(doc, entity);
        // transfer extensions
        resourceUtils.transferToResourceExtensions(doc,
                entity.getExtensions(true), extensions);
        log.debug("successfuly get resource meta: {}", entity);
        return Response.ok().entity(entity).tag(etag).build();
    }

    @Deprecated
    @Override
    public Response putResourceMeta(String idNoSlash, ResourceMeta messageBody,
            Set<String> extensions) {
        String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return putResourceMetaWithDocId(messageBody, id , extensions);
    }

    @Override
    public Response putResourceMetaWithDocId(ResourceMeta messageBody,
            String docId, Set<String> extensions) {
        identity.checkPermission(getSecuredIteration(), "import-template");
        if (StringUtils.isBlank(docId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("missing id").build();
        }
        log.debug("start to put resource meta");
        HProjectIteration hProjectIteration = retrieveAndCheckIteration(true);
        EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration,
                docId, extensions);
        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        log.debug("pass evaluation");
        log.debug("put resource meta: {}", messageBody);
        HDocument document =
                documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
        if (document == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (document.isObsolete()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        HLocale hLocale = validateTargetLocale(messageBody.getLang(),
                projectSlug, iterationSlug);
        boolean changed = resourceUtils.transferFromResourceMetadata(
                messageBody, document, extensions, hLocale,
                document.getRevision() + 1);
        if (changed) {
            documentDAO.flush();
            etag = eTagUtils.generateETagForDocument(hProjectIteration, docId,
                    extensions);
        }
        log.debug("put resource meta successfully");
        return Response.ok().tag(etag).lastModified(document.getLastChanged())
                .build();
    }

    private HProjectIteration
            retrieveAndCheckIteration(boolean writeOperation) {
        HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        HProject hProject = hProjectIteration == null ? null
                : hProjectIteration.getProject();
        if (hProjectIteration == null) {
            throw new NoSuchEntityException("Project Iteration \'" + projectSlug
                    + ":" + iterationSlug + "\' not found.");
        } else if (!haveReadAccess(hProjectIteration) ||
                hProjectIteration.getStatus().equals(EntityStatus.OBSOLETE)
                || hProject.getStatus().equals(EntityStatus.OBSOLETE)) {
            throw new NoSuchEntityException(
                    "Project Iteration \'" + projectSlug
                            + ":" + iterationSlug + "\' not found.");
        } else if (writeOperation) {
            if (hProjectIteration.getStatus().equals(EntityStatus.READONLY)
                    || hProject.getStatus().equals(EntityStatus.READONLY)) {
                throw new ReadOnlyEntityException(
                        "Project Iteration \'" + projectSlug + ":"
                                + iterationSlug + "\' is read-only.");
            } else {
                return hProjectIteration;
            }
        } else {
            return hProjectIteration;
        }
    }

    private HLocale validateTargetLocale(LocaleId locale, String projectSlug,
            String iterationSlug) {
        HLocale hLocale;
        try {
            hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale,
                    projectSlug, iterationSlug);
            return hLocale;
        } catch (ZanataServiceException e) {
            log.warn("Exception validating target locale {} in proj {} iter {}",
                    locale, projectSlug, iterationSlug, e);
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity(e.getMessage()).build());
        }
    }

    public HProjectIteration getSecuredIteration() {
        return retrieveAndCheckIteration(false);
    }

    /**
     * Check if current user have read access to the project
     * (checking for private project)
     */
    public boolean haveReadAccess(HProjectIteration version) {
        return identity.hasPermission(version, "read");
    }
}
