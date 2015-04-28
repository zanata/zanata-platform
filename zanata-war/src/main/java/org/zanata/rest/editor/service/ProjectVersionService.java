package org.zanata.rest.editor.service;

import static org.zanata.webtrans.server.rpc.GetTransUnitsNavigationService.TextFlowResultTransformer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.util.GenericType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.editor.dto.Locale;
import org.zanata.rest.editor.dto.TransUnitStatus;
import org.zanata.rest.editor.service.resource.ProjectVersionResource;
import org.zanata.rest.service.ETagUtils;
import org.zanata.rest.service.ProjectIterationService;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.rest.service.URIHelper;
import org.zanata.search.FilterConstraints;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.model.DocumentId;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("editor.projectVersionService")
@Path(ProjectVersionResource.SERVICE_PATH)
@Transactional
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectVersionService implements ProjectVersionResource {
    @In
    private TextFlowDAO textFlowDAO;

    @In
    private DocumentDAO documentDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private LocaleService localeServiceImpl;

    @Context
    private Request request;

    @In
    private ETagUtils eTagUtils;

    @In
    private ResourceUtils resourceUtils;

    @Override
    public Response getVersion(@PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug) {
        EntityTag etag =
                eTagUtils.generateETagForIteration(projectSlug, versionSlug);

        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }

        HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (hProjectIteration == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ProjectIteration it = new ProjectIteration();
        ProjectIterationService.transfer(hProjectIteration, it);

        return Response.ok(it).tag(etag).build();
    }

    @Override
    public Response getLocales(@PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug) {
        HProjectIteration version =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (version == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<HLocale> locales =
                localeServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, versionSlug);

        List<Locale> localesRefs =
                Lists.newArrayListWithExpectedSize(locales.size());

        for (HLocale hLocale : locales) {
            localesRefs.add(new Locale(hLocale.getLocaleId(),
                    hLocale.retrieveDisplayName()));
        }

        Type genericType = new GenericType<List<Locale>>() {
        }.getGenericType();
        Object entity = new GenericEntity<>(localesRefs, genericType);
        return Response.ok(entity).build();
    }

    @Override
    public Response getDocuments(@PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug) {
        HProjectIteration hProjectIteration =
                retrieveAndCheckIteration(projectSlug, versionSlug, false);

        EntityTag etag =
                projectIterationDAO.getResourcesETag(hProjectIteration);

        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }

        List<ResourceMeta> resources = new ArrayList<>();

        for (HDocument doc : hProjectIteration.getDocuments().values()) {

            ResourceMeta resource = new ResourceMeta();
            resourceUtils.transferToAbstractResourceMeta(doc, resource);
            resources.add(resource);
        }

        Type genericType = new GenericType<List<ResourceMeta>>() {
        }.getGenericType();
        Object entity = new GenericEntity<>(resources, genericType);
        return Response.ok(entity).tag(etag).build();
    }

    @Override
    public Response getTransUnitStatus(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @PathParam("docId") String docId,
            @PathParam("localeId") String localeId) {
        docId = URIHelper.convertFromDocumentURIId(docId);

        HDocument document =
                documentDAO.getByProjectIterationAndDocId(projectSlug,
                        versionSlug, docId);

        if (document == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
        if (hLocale == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        TextFlowResultTransformer resultTransformer =
                new TextFlowResultTransformer(hLocale);

        FilterConstraints filterConstraints = FilterConstraints.builder().build();

        List<HTextFlow> textFlows =
                textFlowDAO.getNavigationByDocumentId(
                        new DocumentId(document.getId(), document.getDocId()),
                        hLocale, resultTransformer, filterConstraints);

        List<TransUnitStatus> statusList =
                Lists.newArrayListWithExpectedSize(textFlows.size());

        for (HTextFlow textFlow : textFlows) {
            ContentState state = textFlow.getTargets().get(hLocale.getId()).getState();
            statusList.add(new TransUnitStatus(textFlow.getId(), textFlow
                    .getResId(), state));
        }

        Type genericType = new GenericType<List<Locale>>() {
        }.getGenericType();
        Object entity = new GenericEntity<>(statusList, genericType);
        return Response.ok(entity).build();
    }

    @VisibleForTesting
    protected HProjectIteration retrieveAndCheckIteration(String projectSlug,
            String versionSlug, boolean writeOperation) {
        HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        HProject hProject =
                hProjectIteration == null ? null : hProjectIteration
                        .getProject();

        if (hProjectIteration == null) {
            throw new NoSuchEntityException("Project version '" + projectSlug
                    + ":" + versionSlug + "' not found.");
        } else if (hProjectIteration.getStatus().equals(EntityStatus.OBSOLETE)
                || hProject.getStatus().equals(EntityStatus.OBSOLETE)) {
            throw new NoSuchEntityException("Project version '" + projectSlug
                    + ":" + versionSlug + "' not found.");
        } else if (writeOperation) {
            if (hProjectIteration.getStatus().equals(EntityStatus.READONLY)
                    || hProject.getStatus().equals(EntityStatus.READONLY)) {
                throw new ReadOnlyEntityException("Project version '"
                        + projectSlug + ":" + versionSlug + "' is read-only.");
            } else {
                return hProjectIteration;
            }
        } else {
            return hProjectIteration;
        }
    }

}
