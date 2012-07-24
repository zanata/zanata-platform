package org.zanata.webtrans.server;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.core.Events;
import org.jboss.seam.web.ServletContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ZanataInit;
import org.zanata.action.ProjectHome;
import org.zanata.action.ProjectIterationHome;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationProject;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
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

@Scope(ScopeType.APPLICATION)
@Name("translationWorkspaceManager")
@Synchronized
public class TranslationWorkspaceManagerImpl implements TranslationWorkspaceManager
{
   private static final Logger LOGGER = LoggerFactory.getLogger(TranslationWorkspaceManagerImpl.class);

   @In
   private AccountDAO accountDAO;

   @In
   private GravatarService gravatarServiceImpl;

   @In
   private ProjectDAO projectDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private LocaleService localeServiceImpl;

   private static final String EVENT_WORKSPACE_CREATED = "webtrans.WorkspaceCreated";

   private final ConcurrentHashMap<WorkspaceId, TranslationWorkspace> workspaceMap;
   private final Multimap<ProjectIterationId, TranslationWorkspace> projIterWorkspaceMap;

   public TranslationWorkspaceManagerImpl()
   {
      this.workspaceMap = new ConcurrentHashMap<WorkspaceId, TranslationWorkspace>();
      Multimap<ProjectIterationId, TranslationWorkspace> piwm = HashMultimap.create();
      this.projIterWorkspaceMap = Multimaps.synchronizedMultimap(piwm);
   }

   @Observer(ZanataInit.EVENT_Zanata_Startup)
   public void start()
   {
      LOGGER.info("starting...");
   }

   @Observer(ZanataIdentity.USER_LOGOUT_EVENT)
   public void exitWorkspace(String username)
   {
      String httpSessionId = getSessionId();
      if (httpSessionId == null)
      {
         LOGGER.debug("Logout: null session");
         return;
      }
      LOGGER.info("Logout: Removing user {} from all workspaces, session: {}", username, httpSessionId);
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
            LOGGER.info("Publishing ExitWorkspace event for user {} with editorClientId {} from workspace {}", new Object[] { username, editorClientId, workspace.getWorkspaceContext() });
            // Send GWT Event to client to update the userlist
            ExitWorkspace event = new ExitWorkspace(editorClientId, new Person(new PersonId(username), personName, gravatarServiceImpl.getUserImageUrl(16, personEmail)));
            workspace.publish(event);
         }
      }
   }

   private String getSessionId()
   {
      HttpServletRequest request = ServletContexts.instance().getRequest();
      if (request == null)
      {
         return null;
      }
      return request.getSession().getId();
   }

   @Observer(ProjectHome.PROJECT_UPDATE)
   public void projectUpdate(HIterationProject project)
   {
      String projectSlug = project.getSlug();
      LOGGER.info("Project {} updated, status={}", projectSlug, project.getStatus());

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
      LOGGER.info("Project {} iteration {} updated, status={}, isProjectActive={}", new Object[] { projectSlug, iterSlug, projectIteration.getStatus(), isProjectActive });

      ProjectIterationId iterId = new ProjectIterationId(projectSlug, iterSlug);
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
      LOGGER.info("stopping...");
      LOGGER.info("closing down {} workspaces: ", workspaceMap.size());
   }

   public int getWorkspaceCount()
   {
      return workspaceMap.size();
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
               Events.instance().raiseEvent(EVENT_WORKSPACE_CREATED, workspaceId);
         }

         return prev == null ? workspace : prev;
      }
      else
      {
         validateAndGetWorkspaceContext(workspaceId);
      }
      return workspace;
   }

   private WorkspaceContext validateAndGetWorkspaceContext(WorkspaceId workspaceId) throws NoSuchWorkspaceException
   {
      HProject project = projectDAO.getBySlug(workspaceId.getProjectIterationId().getProjectSlug());

      if (project == null)
      {
         throw new NoSuchWorkspaceException("Invalid workspace Id");
      }
      if (project.getStatus() == EntityStatus.OBSOLETE)
      {
         throw new NoSuchWorkspaceException("Project is obsolete");
      }

      HProjectIteration projectIteration = projectIterationDAO.getBySlug(workspaceId.getProjectIterationId().getProjectSlug(), workspaceId.getProjectIterationId().getIterationSlug());

      if (projectIteration == null)
      {
         throw new NoSuchWorkspaceException("Invalid workspace Id");
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

      String workspaceName = project.getName() + " (" + projectIteration.getSlug() + ")";
      String localeDisplayName = ULocale.getDisplayName(workspaceId.getLocaleId().toJavaName(), ULocale.ENGLISH);

      return new WorkspaceContext(workspaceId, workspaceName, localeDisplayName);
   }

   private TranslationWorkspace createWorkspace(WorkspaceId workspaceId) throws NoSuchWorkspaceException
   {
      WorkspaceContext workspaceContext = validateAndGetWorkspaceContext(workspaceId);
      return new TranslationWorkspaceImpl(workspaceContext);
   }

   public TranslationWorkspace getWorkspace(ProjectIterationId projectIterationId, LocaleId localeId)
   {
      WorkspaceId workspaceId = new WorkspaceId(projectIterationId, localeId);
      return getWorkspace(workspaceId);
   }

   private TranslationWorkspace getWorkspace(WorkspaceId workspaceId)
   {
      return workspaceMap.get(workspaceId);
   }

}
