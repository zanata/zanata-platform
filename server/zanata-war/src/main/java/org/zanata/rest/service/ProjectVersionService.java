package org.zanata.rest.service;

import static org.zanata.common.EntityStatus.ACTIVE;
import static org.zanata.common.EntityStatus.OBSOLETE;
import static org.zanata.common.EntityStatus.READONLY;
import static org.zanata.webtrans.server.rpc.GetTransUnitsNavigationService.TextFlowResultTransformer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import com.google.common.base.Objects;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jboss.resteasy.util.GenericType;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.TransUnitStatus;
import org.zanata.rest.dto.User;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.editor.service.UserService;
import org.zanata.webtrans.shared.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.ConfigurationService;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.webtrans.shared.model.DocumentId;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

/**
 * Project version REST API implementation.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("projectVersionService")
@Path(ProjectVersionResource.SERVICE_PATH)
@Transactional
public class ProjectVersionService implements ProjectVersionResource {
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Context
    private Request request;
    @Inject
    private ETagUtils eTagUtils;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private ConfigurationService configurationServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private UserService userService;
    @Inject
    private ApplicationConfiguration applicationConfiguration;
    @Context
    private UriInfo uri;

    @Override
    public Response head(@PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug) {
        EntityTag etag =
                eTagUtils.generateETagForIteration(projectSlug, versionSlug);
        Response.ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        return Response.ok().tag(etag).build();
    }

    @Override
    public Response put(@PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            ProjectIteration projectVersion) {
        Response.ResponseBuilder response;
        EntityTag etag = null;
        boolean changed = false;
        Response projTypeError =
                getProjectTypeError(projectVersion.getProjectType());
        if (projTypeError != null) {
            return projTypeError;
        }
        HProject hProject = projectDAO.getBySlug(projectSlug);
        if (hProject == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Project \'" + projectSlug + "\' not found.")
                    .build();
        } else if (Objects.equal(hProject.getStatus(), OBSOLETE)) {
            // Project is Obsolete
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Project \'" + projectSlug + "\' not found.")
                    .build();
        } else if (Objects.equal(hProject.getStatus(), READONLY)) {
            // Project is ReadOnly
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Project \'" + projectSlug + "\' is read-only.")
                    .build();
        }
        HProjectIteration hProjectVersion =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (hProjectVersion == null) {
            // must be a create operation
            response = request.evaluatePreconditions();
            if (response != null) {
                return response.build();
            }
            hProjectVersion = new HProjectIteration();
            hProjectVersion.setSlug(versionSlug);
            copyProjectConfiguration(projectVersion, hProjectVersion, hProject);
            hProject.addIteration(hProjectVersion);
            // pre-emptive entity permission check
            // identity.checkWorkspaceAction(hProject, "add-iteration");
            identity.checkPermission(hProjectVersion, "insert");
            response = Response.created(uri.getAbsolutePath());
            changed = true;
        } else if (Objects.equal(hProjectVersion.getStatus(), OBSOLETE)) {
            // Iteration is Obsolete
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Project Iiteration \'" + projectSlug + ":"
                            + versionSlug + "\' is obsolete.")
                    .build();
        } else {
            // must be an update operation
            // pre-emptive entity permission check
            identity.checkPermission(hProjectVersion, "update");
            if (Objects.equal(hProjectVersion.getStatus(), READONLY)
                    && !Objects.equal(projectVersion.getStatus(), ACTIVE)) {
                // User is attempting to update a ReadOnly version
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("Project Iteration \'" + projectSlug + ":"
                                + versionSlug + "\' is read-only.")
                        .build();
            }
            copyProjectConfiguration(projectVersion, hProjectVersion, null);
            etag = eTagUtils.generateETagForIteration(projectSlug, versionSlug);
            response = request.evaluatePreconditions(etag);
            if (response != null) {
                return response.build();
            }
            response = Response.ok();
            changed = true;
        }
        if (changed) {
            projectIterationDAO.makePersistent(hProjectVersion);
            projectIterationDAO.flush();
            etag = eTagUtils.generateETagForIteration(projectSlug, versionSlug);
        }
        return response.tag(etag).build();
    }

    @Override
    public Response sampleConfiguration(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug) {
        HProjectIteration iteration =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (iteration == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String generalConfig = configurationServiceImpl
                .getGeneralConfig(projectSlug, versionSlug);
        return Response.ok().entity(generalConfig).build();
    }

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
        getProjectVersionDetails(hProjectIteration, it);
        return Response.ok(it).tag(etag).build();
    }

    @Override
    public Response getContributors(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @PathParam("dateRange") String dateRange) {
        DateRange dateRangeObject = DateRange.from(dateRange);
        List<HAccount> accountList = projectIterationDAO
                .getContributors(projectSlug, versionSlug, dateRangeObject);
        boolean displayEmail = applicationConfiguration.isDisplayUserEmail();
        List<User> userList = Lists.newArrayList();
        userList.addAll(accountList.stream()
                .map(account -> userService.getUserInfo(account, displayEmail))
                .collect(Collectors.toList()));
        Type genericType = new GenericType<List<User>>() {

        }.getGenericType();
        Object entity = new GenericEntity<>(userList, genericType);
        return Response.ok(entity).build();
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
        List<LocaleDetails> localesRefs =
                Lists.newArrayListWithExpectedSize(locales.size());
        localesRefs.addAll(locales.stream()
                .map(hLocale -> LocaleServiceImpl.convertToDTO(hLocale))
                .collect(Collectors.toList()));
        Type genericType = new GenericType<List<LocaleDetails>>() {

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
            @PathParam("docId") String noSlashDocId,
            @DefaultValue("en-US") @PathParam("localeId") String localeId) {
        if (StringUtils.isEmpty(noSlashDocId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String docId = URIHelper.convertFromDocumentURIId(noSlashDocId);
        HDocument document = documentDAO
                .getByProjectIterationAndDocId(projectSlug, versionSlug, docId);
        if (document == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
        if (hLocale == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        TextFlowResultTransformer resultTransformer =
                new TextFlowResultTransformer(hLocale);
        FilterConstraints filterConstraints =
                FilterConstraints.builder().build();
        List<HTextFlow> textFlows = textFlowDAO.getNavigationByDocumentId(
                new DocumentId(document.getId(), document.getDocId()), hLocale,
                resultTransformer, filterConstraints);
        List<TransUnitStatus> statusList =
                Lists.newArrayListWithExpectedSize(textFlows.size());
        for (HTextFlow textFlow : textFlows) {
            ContentState state =
                    textFlow.getTargets().get(hLocale.getId()).getState();
            statusList.add(new TransUnitStatus(textFlow.getId(),
                    textFlow.getResId(), state));
        }
        Type genericType = new GenericType<List<TransUnitStatus>>() {

        }.getGenericType();
        Object entity = new GenericEntity<>(statusList, genericType);
        return Response.ok(entity).build();
    }

    @VisibleForTesting
    protected HProjectIteration retrieveAndCheckIteration(String projectSlug,
            String versionSlug, boolean writeOperation) {
        HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        HProject hProject = hProjectIteration == null ? null
                : hProjectIteration.getProject();
        if (hProjectIteration == null
                || hProjectIteration.getStatus().equals(EntityStatus.OBSOLETE)
                || hProject.getStatus().equals(EntityStatus.OBSOLETE)) {
            throw new NoSuchEntityException("Project version \'" + projectSlug
                    + ":" + versionSlug + "\' not found.");
        } else if (writeOperation) {
            if (hProjectIteration.getStatus().equals(EntityStatus.READONLY)
                    || hProject.getStatus().equals(EntityStatus.READONLY)) {
                throw new ReadOnlyEntityException("Project version \'"
                        + projectSlug + ":" + versionSlug + "\' is read-only.");
            }
        }
        return hProjectIteration;
    }

    /**
     * Copy project configuration into new version(project type and validation
     * rules)
     *
     * @param from
     *            must have pre-validated project type
     * @param to
     * @param hProject
     */
    public static void copyProjectConfiguration(ProjectIteration from,
            HProjectIteration to, HProject hProject) {
        ProjectType projectType;
        try {
            projectType = ProjectType.getValueOf(from.getProjectType());
        } catch (Exception e) {
            projectType = null;
        }
        if (from.getStatus() != null) {
            to.setStatus(from.getStatus());
        }
        if (projectType == null) {
            if (hProject != null) {
                to.setProjectType(hProject.getDefaultProjectType());
            }
        } else {
            to.setProjectType(projectType);
        }
        if (hProject != null) {
            to.getCustomizedValidations()
                    .putAll(hProject.getCustomizedValidations());
        }
    }

    public static void getProjectVersionDetails(HProjectIteration from,
            ProjectIteration to) {
        to.setId(from.getSlug());
        to.setStatus(from.getStatus());
        if (from.getProjectType() != null) {
            to.setProjectType(from.getProjectType().toString());
        }
    }

    /**
     * Generate an error response object for invalid projectType. Valid types
     * are null or a type that is valid for this server.
     *
     * @param projectType
     *            to check
     * @return null if projectType is valid, otherwise a Response object with
     *         appropriate error string
     */
    private Response getProjectTypeError(String projectType) {
        if (projectType != null) {
            if (projectType.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("If a project type is specified, it must not be empty.")
                        .build();
            }
            try {
                ProjectType.getValueOf(projectType);
            } catch (Exception e) {
                String validTypes =
                        StringUtils.join(ProjectType.values(), ", ");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Project type \"" + projectType
                                + "\" not valid for this server. Valid types: ["
                                + validTypes + "]")
                        .build();
            }
        }
        return null;
    }

    public ProjectVersionService() {
    }

    @java.beans.ConstructorProperties({ "textFlowDAO", "documentDAO",
            "projectDAO", "projectIterationDAO", "localeServiceImpl", "request",
            "eTagUtils", "resourceUtils", "configurationServiceImpl",
            "identity", "userService", "applicationConfiguration", "uri" })
    protected ProjectVersionService(final TextFlowDAO textFlowDAO,
            final DocumentDAO documentDAO, final ProjectDAO projectDAO,
            final ProjectIterationDAO projectIterationDAO,
            final LocaleService localeServiceImpl, final Request request,
            final ETagUtils eTagUtils, final ResourceUtils resourceUtils,
            final ConfigurationService configurationServiceImpl,
            final ZanataIdentity identity, final UserService userService,
            final ApplicationConfiguration applicationConfiguration,
            final UriInfo uri) {
        this.textFlowDAO = textFlowDAO;
        this.documentDAO = documentDAO;
        this.projectDAO = projectDAO;
        this.projectIterationDAO = projectIterationDAO;
        this.localeServiceImpl = localeServiceImpl;
        this.request = request;
        this.eTagUtils = eTagUtils;
        this.resourceUtils = resourceUtils;
        this.configurationServiceImpl = configurationServiceImpl;
        this.identity = identity;
        this.userService = userService;
        this.applicationConfiguration = applicationConfiguration;
        this.uri = uri;
    }
}
