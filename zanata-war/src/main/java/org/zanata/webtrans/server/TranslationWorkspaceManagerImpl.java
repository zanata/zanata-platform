package org.zanata.webtrans.server;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.web.ServletContexts;
import org.zanata.ZanataInit;
import org.zanata.action.ProjectHome;
import org.zanata.action.ProjectIterationHome;
import org.zanata.common.EntityStatus;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.WorkspaceContextUpdate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.ibm.icu.util.ULocale;

import de.novanic.eventservice.service.registry.EventRegistry;
import de.novanic.eventservice.service.registry.EventRegistryFactory;

@Scope(ScopeType.APPLICATION)
@Name("translationWorkspaceManager")
@Slf4j
public class TranslationWorkspaceManagerImpl implements TranslationWorkspaceManager
{
   @In
   private AccountDAO accountDAO;

   @In
   private GravatarService gravatarServiceImpl;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private LocaleService localeServiceImpl;

   private static final String EVENT_WORKSPACE_CREATED = "webtrans.WorkspaceCreated";

   private final ConcurrentHashMap<WorkspaceId, TranslationWorkspace> workspaceMap;
   private final Multimap<ProjectIterationId, TranslationWorkspace> projIterWorkspaceMap;
   private final EventRegistry eventRegistry;

   public TranslationWorkspaceManagerImpl()
   {
      this.workspaceMap = new ConcurrentHashMap<WorkspaceId, TranslationWorkspace>();
      Multimap<ProjectIterationId, TranslationWorkspace> piwm = HashMultimap.create();
      this.projIterWorkspaceMap = Multimaps.synchronizedMultimap(piwm);
      this.eventRegistry = EventRegistryFactory.getInstance().getEventRegistry();
   }

   @Observer(ZanataInit.EVENT_Zanata_Startup)
   public void start()
   {
      log.info("starting...");

      Runtime.getRuntime().addShutdownHook(new Thread()
      {
         @Override
         public void run()
         {
            stopListeners();
         }
      });
   }

   @Observer(ZanataIdentity.USER_LOGOUT_EVENT)
   public void exitWorkspace(String username)
   {
      String httpSessionId = getSessionId();
      if (httpSessionId == null)
      {
         log.debug("Logout: null session");
         return;
      }
      log.info("Logout: Removing user {} from all workspaces, session: {}", username, httpSessionId);
      String personName = "<unknown>";
      String personEmail = "<unknown>";
      HAccount account = accountDAO.getByUsername(username);
      if (account != null)
      {
         HPerson person = account.getPerson();
         if (person != null)
         {
            personName = person.getName();
            personEmail = person.getEmail();
         }
      }
      ImmutableSet<TranslationWorkspace> workspaceSet = ImmutableSet.copyOf(workspaceMap.values());
      for (TranslationWorkspace workspace : workspaceSet)
      {
         Collection<EditorClientId> editorClients = workspace.removeEditorClients(httpSessionId);
         for (EditorClientId editorClientId : editorClients)
         {
            log.info("Publishing ExitWorkspace event for user {} with editorClientId {} from workspace {}", new Object[]{username, editorClientId, workspace.getWorkspaceContext()});
            // Send GWT Event to client to update the userlist
            ExitWorkspace event = new ExitWorkspace(editorClientId, new Person(new PersonId(username), personName, gravatarServiceImpl.getUserImageUrl(16, personEmail)));
            workspace.publish(event);
         }
      }
   }

   protected String getSessionId()
   {
      HttpServletRequest request = ServletContexts.instance().getRequest();
      if (request == null)
      {
         return null;
      }
      return request.getSession().getId();
   }

   @Observer(ProjectHome.PROJECT_UPDATE)
   public void projectUpdate(HProject project)
   {
      String projectSlug = project.getSlug();
      log.info("Project {} updated, status={}", projectSlug, project.getStatus());

      for (HProjectIteration iter : project.getProjectIterations())
      {
         projectIterationUpdate(iter);
      }
   }

   @Observer(ProjectIterationHome.PROJECT_ITERATION_UPDATE)
   public void projectIterationUpdate(HProjectIteration projectIteration)
   {
      String projectSlug = projectIteration.getProject().getSlug();
      String iterSlug = projectIteration.getSlug();
      HProject project = projectIteration.getProject();
      Boolean isProjectActive = projectIterationIsActive(project.getStatus(), projectIteration.getStatus());
      log.info("Project {} iteration {} updated, status={}, isProjectActive={}", new Object[]{projectSlug, iterSlug, projectIteration.getStatus(), isProjectActive});

      ProjectIterationId iterId = new ProjectIterationId(projectSlug, iterSlug, projectIteration.getProjectType());
      for (TranslationWorkspace workspace : projIterWorkspaceMap.get(iterId))
      {
         WorkspaceContextUpdate event = new WorkspaceContextUpdate(isProjectActive);
         workspace.publish(event);
      }
   }

   private boolean projectIterationIsActive(EntityStatus projectStatus, EntityStatus iterStatus)
   {
      return (projectStatus.equals(EntityStatus.ACTIVE) && iterStatus.equals(EntityStatus.ACTIVE));
   }

   @Destroy
   public void stop()
   {
      log.info("stopping...");
      log.info("closing down {} workspaces: ", workspaceMap.size());
   }

   private void stopListeners()
   {
      // Stopping the listeners frees up the EventServiceImpl servlet, which
      // would otherwise prevent Apache Coyote from shutting down quickly.
      // Note that the servlet may still hang around up to the max timeout
      // configured in eventservice.properties.
      Set<String> registeredUserIds = eventRegistry.getRegisteredUserIds();
      int clientCount = registeredUserIds.size();
      log.info("Removing {} client(s)", clientCount);
      for (String userId : registeredUserIds)
      {
         log.debug("Removing client {}", userId);
         eventRegistry.unlisten(userId);
      }
      log.info("Removed {} client(s).  Waiting for outstanding polls to time out...", clientCount);
   }

   @Override
   public TranslationWorkspace getOrRegisterWorkspace(WorkspaceId workspaceId) throws NoSuchWorkspaceException
   {
      TranslationWorkspace workspace = workspaceMap.get(workspaceId);
      if (workspace == null)
      {
         workspace = createWorkspace(workspaceId);
         TranslationWorkspace prev = workspaceMap.putIfAbsent(workspaceId, workspace);

         if (prev == null)
         {
            projIterWorkspaceMap.put(workspaceId.getProjectIterationId(), workspace);
            if (Events.exists())
            {
               Events.instance().raiseEvent(EVENT_WORKSPACE_CREATED, workspaceId);
            }
         }

         return prev == null ? workspace : prev;
      }
      return workspace;
   }

   private WorkspaceContext validateAndGetWorkspaceContext(WorkspaceId workspaceId) throws NoSuchWorkspaceException
   {
      String projectSlug = workspaceId.getProjectIterationId().getProjectSlug();
      String iterationSlug = workspaceId.getProjectIterationId().getIterationSlug();
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

      if (projectIteration == null)
      {
         throw new NoSuchWorkspaceException("Invalid workspace Id");
      }
      HProject project = projectIteration.getProject();
      if (project.getStatus() == EntityStatus.OBSOLETE)
      {
         throw new NoSuchWorkspaceException("Project is obsolete");
      }
      if (projectIteration.getStatus() == EntityStatus.OBSOLETE)
      {
         throw new NoSuchWorkspaceException("Project Iteration is obsolete");
      }
      HLocale locale = localeServiceImpl.getByLocaleId(workspaceId.getLocaleId());
      if (locale == null)
      {
         throw new NoSuchWorkspaceException("Invalid Workspace Locale");
      }
      if (!locale.isActive())
      {
         throw new NoSuchWorkspaceException("Locale '" + locale.retrieveDisplayName() + "' disabled in server");
      }

      String workspaceName = project.getName() + " (" + projectIteration.getSlug() + ")";
      String localeDisplayName = ULocale.getDisplayName(workspaceId.getLocaleId().toJavaName(), ULocale.ENGLISH);

      return new WorkspaceContext(workspaceId, workspaceName, localeDisplayName);
   }

   protected TranslationWorkspace createWorkspace(WorkspaceId workspaceId) throws NoSuchWorkspaceException
   {
      WorkspaceContext workspaceContext = validateAndGetWorkspaceContext(workspaceId);
      return new TranslationWorkspaceImpl(workspaceContext);
   }
}
