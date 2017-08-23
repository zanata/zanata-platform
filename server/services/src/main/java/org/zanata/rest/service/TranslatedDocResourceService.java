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

import java.util.List;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.inject.Inject;
import javax.inject.Named;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.RestUtil;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;
import com.google.common.base.Optional;

/**
 * This service allows clients to push and pull both source documents and
 * translations.
 */
@RequestScoped
@Named("translatedDocResourceService")
@Path(TranslatedDocResource.SERVICE_PATH)
@Transactional
public class TranslatedDocResourceService implements TranslatedDocResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TranslatedDocResourceService.class);

    // security actions
    // private static final String ACTION_IMPORT_TEMPLATE = "import-template";
    // private static final String ACTION_IMPORT_TRANSLATION =
    // "import-translation";
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

    /**
     * (This parameter is optional and is currently not used)
     */
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @HeaderParam("Content-Type")
    @Context
    private MediaType requestContentType;
    @Context
    @SuppressFBWarnings("SE_BAD_FIELD")
    private HttpHeaders headers;
    @Context
    @SuppressFBWarnings("SE_BAD_FIELD")
    private Request request;
    @Context
    @SuppressFBWarnings("SE_BAD_FIELD")
    private UriInfo uri;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private ETagUtils eTagUtils;
    @Inject
    private RestSlugValidator restSlugValidator;
    @Inject
    private TranslationService translationServiceImpl;
    @Inject
    private LocaleService localeServiceImpl;

    @Override
    public Response getTranslations(String idNoSlash, LocaleId locale,
            Set<String> extensions, boolean skeletons, String eTag) {
        String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return getTranslationsWithDocId(locale, id, extensions, skeletons, eTag);
    }

    @Override
    public Response getTranslationsWithDocId(LocaleId locale, String docId,
            Set<String> extensions, boolean createSkeletons, String eTag) {
        log.debug("start to get translation");
        if (StringUtils.isBlank(docId)) {
            // TODO: return Problem DTO, https://tools.ietf.org/html/rfc7807
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("missing id").build();
        }
        HProjectIteration hProjectIteration = restSlugValidator
                .retrieveAndCheckIteration(projectSlug, iterationSlug, false);
        HLocale hLocale = restSlugValidator.validateTargetLocale(locale,
                projectSlug, iterationSlug);
        ResourceUtils.validateExtensions(extensions);
        // Check Etag header
        EntityTag generatedEtag = eTagUtils.generateETagForTranslatedDocument(
                hProjectIteration, docId, hLocale);
        List<String> requestedEtagHeaders =
                headers.getRequestHeader(HttpHeaders.IF_NONE_MATCH);
        if (requestedEtagHeaders != null && !requestedEtagHeaders.isEmpty()) {
            if (requestedEtagHeaders.get(0).equals(generatedEtag.getValue())) {
                return Response.notModified(generatedEtag).build();
            }
        }
        ResponseBuilder response = request.evaluatePreconditions(generatedEtag);
        if (response != null) {
            return response.build();
        }
        HDocument document =
                documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
        if (document == null || document.isObsolete()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        TranslationsResource translationResource = new TranslationsResource();
        // TODO avoid queries for better cacheability
        List<HTextFlowTarget> hTargets =
                textFlowTargetDAO.findTranslations(document, hLocale);
        boolean foundData = resourceUtils.transferToTranslationsResource(
                translationResource, document, hLocale, extensions, hTargets,
                Optional.<String> absent());
        if (!foundData && !createSkeletons) {
            return Response.status(Status.NOT_FOUND).build();
        }
        // TODO lastChanged
        return Response.ok().entity(translationResource).tag(generatedEtag)
                .build();
    }

    @Override
    public Response deleteTranslations(String idNoSlash, LocaleId locale) {
        String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return deleteTranslationsWithDocId(locale, id);
    }

    @Override
    public Response deleteTranslationsWithDocId(LocaleId locale, String docId) {
        identity.checkPermission(getSecuredIteration().getProject(),
                "modify-translation");
        if (StringUtils.isBlank(docId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("missing id").build();
        }
        HProjectIteration hProjectIteration = restSlugValidator
                .retrieveAndCheckIteration(projectSlug, iterationSlug, true);
        HLocale hLocale = restSlugValidator.validateTargetLocale(locale,
                projectSlug, iterationSlug);
        EntityTag etag = eTagUtils.generateETagForTranslatedDocument(
                hProjectIteration, docId, hLocale);
        ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        HDocument document =
                documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
        if (document == null || document.isObsolete()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        List<HTextFlowTarget> targets =
                textFlowTargetDAO.findAllTranslations(document, locale);
        for (HTextFlowTarget target : targets) {
            target.clear();
        }
        // we also need to delete the extensions here
        document.getPoTargetHeaders().remove(hLocale);
        textFlowTargetDAO.flush();
        return Response.ok().build();
    }

    @Override
    public Response putTranslations(String idNoSlash, LocaleId locale,
            TranslationsResource messageBody, Set<String> extensions,
            String merge) {
        String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return putTranslationsWithDocId(locale, messageBody, id, extensions, merge);
    }

    @Override
    public Response putTranslationsWithDocId(LocaleId locale,
            TranslationsResource messageBody, String docId, Set<String> extensions,
            String merge) {
        // check security (cannot be on @Restrict as it refers to method
        // parameters)
        identity.checkPermission("modify-translation",
                this.localeServiceImpl.getByLocaleId(locale),
                this.getSecuredIteration().getProject());
        log.debug("start put translations");
        if (StringUtils.isBlank(docId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("missing id").build();
        }
        MergeType mergeType;
        try {
            mergeType = MergeType.valueOf(merge.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("bad merge type " + merge).build();
        }
        HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        HLocale hLocale = restSlugValidator.validateTargetLocale(locale,
                projectSlug, iterationSlug);
        EntityTag etag = eTagUtils.generateETagForTranslatedDocument(
                hProjectIteration, docId, hLocale);
        ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        // assignCreditToUploader is not supported from here
        boolean assignCreditToUploader = false;
        // Translate
        List<String> warnings = this.translationServiceImpl.translateAllInDoc(
                projectSlug, iterationSlug, docId, locale, messageBody, extensions,
                mergeType, assignCreditToUploader,
                TranslationSourceType.API_UPLOAD);
        // Regenerate etag in case it has changed
        etag = eTagUtils.generateETagForTranslatedDocument(hProjectIteration,
                docId, hLocale);
        log.debug("successful put translation");
        // TODO lastChanged
        StringBuilder sb = new StringBuilder();
        for (String warning : warnings) {
            sb.append("warning: ").append(warning).append('\n');
        }
        return Response.ok(sb.toString()).tag(etag).build();
    }
    // public for Seam's benefit (and the @Restrict in deleteTranslations)

    public HProjectIteration getSecuredIteration() {
        return restSlugValidator.retrieveAndCheckIteration(projectSlug,
                iterationSlug, false);
    }
}
