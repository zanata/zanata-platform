package org.zanata.webtrans.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import de.novanic.eventservice.service.registry.EventRegistry;
import de.novanic.eventservice.service.registry.EventRegistryFactory;
import de.novanic.eventservice.service.registry.user.UserManager;
import de.novanic.eventservice.service.registry.user.UserManagerFactory;
import org.zanata.async.Async;
import org.zanata.common.EntityStatus;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.LogoutEvent;
import org.zanata.events.ProjectIterationUpdate;
import org.zanata.events.ProjectUpdate;
import org.zanata.events.ServerStarted;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.WorkspaceContextUpdate;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static com.google.common.base.MoreObjects.firstNonNull;
import static org.zanata.transaction.TransactionUtilImpl.runInTransaction;

@javax.enterprise.context.ApplicationScoped
@Named("translationWorkspaceManager")
public class TranslationWorkspaceManagerImpl
        implements TranslationWorkspaceManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TranslationWorkspaceManagerImpl.class);

    @Inject
    private GravatarService gravatarServiceImpl;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private EntityManager entityManager;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private ValidationService validationServiceImpl;
    @Inject
    private TranslationWorkspaceFactory translationWorkspaceFactory;
    private ConcurrentHashMap<WorkspaceId, TranslationWorkspace> workspaceMap;
    private Multimap<ProjectIterationId, TranslationWorkspace> projIterWorkspaceMap;
    private EventRegistry eventRegistry;

    @PostConstruct
    public void postConstruct() {
        this.workspaceMap = new ConcurrentHashMap<>();
        Multimap<ProjectIterationId, TranslationWorkspace> piwm =
                HashMultimap.create();
        this.projIterWorkspaceMap = Multimaps.synchronizedMultimap(piwm);
        this.eventRegistry =
                EventRegistryFactory.getInstance().getEventRegistry();
    }

    public void start(@Observes ServerStarted payload) {
        log.info("starting...");
    }

    public void exitWorkspace(@Observes LogoutEvent payload) {
        // NB: avoid using session-scoped beans, because this event is
        // fired during session expiry.
        exitWorkspace(payload.getUsername(), payload.getSessionId(),
                firstNonNull(payload.getPersonName(), "<unknown>"),
                firstNonNull(payload.getPersonEmail(), "<unknown>"));
    }

    @VisibleForTesting
    void exitWorkspace(String username, String httpSessionId, String personName,
            String personEmail) {
        if (httpSessionId == null) {
            log.debug("Logout: null session");
            return;
        }
        ImmutableSet<TranslationWorkspace> workspaceSet =
                ImmutableSet.copyOf(workspaceMap.values());
        for (TranslationWorkspace workspace : workspaceSet) {
            Collection<EditorClientId> editorClients =
                    workspace.removeEditorClients(httpSessionId);
            for (EditorClientId editorClientId : editorClients) {
                log.info(
                        "Publishing ExitWorkspace event for user {} with editorClientId {} from workspace {}",
                        username, editorClientId,
                        workspace.getWorkspaceContext());
                // Send GWT Event to client to update the userlist
                ExitWorkspace event = new ExitWorkspace(editorClientId,
                        new Person(new PersonId(username), personName,
                                gravatarServiceImpl.getUserImageUrl(16,
                                        personEmail)));
                workspace.publish(event);
            }
        }
    }
    // transaction has already been committed and marked as rolled back only for
    // current thread. We have to open a new transaction to load any lazy
    // properties (otherwise exception like javax.resource.ResourceException:
    // IJ000460: Error checking for a transaction: Transactions are not active)

    @Async
    public void projectUpdate(@Observes final ProjectUpdate payload) {
        // avoid WELD-2019
        final ProjectUpdate event = payload;
        try {
            runInTransaction(() -> projectUpdate(event.getProject(),
                    event.getOldSlug()));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    void projectUpdate(HProject project, String oldProjectSlug) {
        // need to reload the entity since it's in separate thread/transaction
        project = entityManager.find(HProject.class, project.getId());
        String projectSlug = project.getSlug();
        log.info("Project newSlug={}, oldSlug={} updated, status={}",
                projectSlug, oldProjectSlug, project.getStatus());
        for (HProjectIteration iter : project.getProjectIterations()) {
            projectIterationUpdate(iter, Optional.of(oldProjectSlug),
                    Optional.<String> absent());
        }
    }

    @Async
    public void
            projectIterationUpdate(@Observes ProjectIterationUpdate payload) {
        projectIterationUpdate(payload.getIteration(),
                Optional.<String> absent(), Optional.of(payload.getOldSlug()));
    }

    void projectIterationUpdate(HProjectIteration projectIteration,
            Optional<String> oldProjectSlug,
            Optional<String> oldIterationSlug) {
        HashMap<ValidationId, State> validationStates = Maps.newHashMap();
        for (ValidationAction validationAction : validationServiceImpl
                .getValidationActions(projectIteration.getProject().getSlug(),
                        projectIteration.getSlug())) {
            validationStates.put(validationAction.getId(),
                    validationAction.getState());
        }
        String projectSlug = projectIteration.getProject().getSlug();
        String iterSlug = projectIteration.getSlug();
        HProject project = projectIteration.getProject();
        Boolean isProjectActive = projectIterationIsActive(project.getStatus(),
                projectIteration.getStatus());
        ProjectType projectType = projectIteration.getProjectType();
        log.info(
                "Project {} iteration {} updated, status={}, isProjectActive={}, projectType={}, oldProjectSlug={}, oldIterationSlug={}",
                projectSlug, iterSlug, projectIteration.getStatus(),
                isProjectActive, projectType, oldProjectSlug, oldIterationSlug);
        ProjectIterationId iterId = createProjectIterationId(projectIteration,
                oldProjectSlug, oldIterationSlug, projectSlug, iterSlug);
        for (TranslationWorkspace workspace : projIterWorkspaceMap
                .get(iterId)) {
            WorkspaceContextUpdate event = new WorkspaceContextUpdate(
                    isProjectActive, projectType, validationStates);
            if (oldProjectSlug.isPresent()) {
                event = event.projectSlugChanged(oldProjectSlug.get(),
                        projectSlug);
            }
            if (oldIterationSlug.isPresent()) {
                event = event.iterationSlugChanged(oldIterationSlug.get(),
                        iterSlug);
            }
            workspace.publish(event);
        }
    }

    /**
     * We need to use old slug to retrieve existing workspace if slug has
     * changed.
     *
     * @param projectIteration
     *            project iteration
     * @param oldProjectSlug
     *            optional old project slug
     * @param oldIterationSlug
     *            optional old iteration slug
     * @param projectSlug
     *            current project slug
     * @param iterSlug
     *            current iteration slug
     * @return a ProjectIterationId object that will be the part of exising
     *         workspace id.
     */
    private ProjectIterationId createProjectIterationId(
            HProjectIteration projectIteration, Optional<String> oldProjectSlug,
            Optional<String> oldIterationSlug, String projectSlug,
            String iterSlug) {
        ProjectIterationId iterId;
        if (oldProjectSlug.isPresent() && oldIterationSlug.isPresent()) {
            iterId = new ProjectIterationId(oldProjectSlug.get(),
                    oldIterationSlug.get(), projectIteration.getProjectType());
        } else if (oldProjectSlug.isPresent()) {
            iterId = new ProjectIterationId(oldProjectSlug.get(), iterSlug,
                    projectIteration.getProjectType());
        } else if (oldIterationSlug.isPresent()) {
            iterId = new ProjectIterationId(projectSlug, oldIterationSlug.get(),
                    projectIteration.getProjectType());
        } else {
            iterId = new ProjectIterationId(projectSlug, iterSlug,
                    projectIteration.getProjectType());
        }
        return iterId;
    }

    private boolean projectIterationIsActive(EntityStatus projectStatus,
            EntityStatus iterStatus) {
        return (projectStatus.equals(EntityStatus.ACTIVE)
                && iterStatus.equals(EntityStatus.ACTIVE));
    }

    @PreDestroy
    public void stop() {
        log.info("stopping...");
        log.info("closing down {} workspaces: ", workspaceMap.size());
        // Stopping the listeners frees up the EventServiceImpl servlet, which
        // would otherwise prevent Apache Coyote from shutting down quickly.
        // Note that the servlet may still hang around up to the max timeout
        // configured in eventservice.properties.
        Set<String> registeredUserIds = eventRegistry.getRegisteredUserIds();
        int clientCount = registeredUserIds.size();
        log.info("Removing {} client(s)", clientCount);
        for (String userId : registeredUserIds) {
            log.debug("Removing client {}", userId);
            eventRegistry.unlisten(userId);
        }
        log.info(
                "Removed {} client(s).  Waiting for outstanding polls to time out...",
                clientCount);
        UserManager userManager =
                UserManagerFactory.getInstance().getUserManager();
        userManager.getUserActivityScheduler().stop();
    }

    @Override
    public TranslationWorkspace getOrRegisterWorkspace(WorkspaceId workspaceId)
            throws NoSuchWorkspaceException {
        TranslationWorkspace workspace = workspaceMap.get(workspaceId);
        if (workspace == null) {
            workspace =
                    translationWorkspaceFactory.createWorkspace(workspaceId);
            TranslationWorkspace prev =
                    workspaceMap.putIfAbsent(workspaceId, workspace);
            if (prev == null) {
                projIterWorkspaceMap.put(workspaceId.getProjectIterationId(),
                        workspace);
            }
            return prev == null ? workspace : prev;
        }
        return workspace;
    }

    @Override
    public Optional<TranslationWorkspace>
            tryGetWorkspace(WorkspaceId workspaceId) {
        return Optional.fromNullable(workspaceMap.get(workspaceId));
    }
}
