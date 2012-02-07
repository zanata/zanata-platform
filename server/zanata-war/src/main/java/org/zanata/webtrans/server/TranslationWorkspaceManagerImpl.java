package org.zanata.webtrans.server;

import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.ZanataInit;
import org.zanata.action.ProjectHome;
import org.zanata.action.ProjectIterationHome;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.model.HIterationProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.ProjectIterationUpdate;
import org.zanata.webtrans.shared.rpc.ProjectUpdate;

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

   public static final String EVENT_WORKSPACE_CREATED = "webtrans.WorkspaceCreated";

   private Log log = Logging.getLog(TranslationWorkspaceManagerImpl.class);

   private final ConcurrentHashMap<WorkspaceId, TranslationWorkspace> workspaceMap;
   private final Multimap<ProjectIterationId, LocaleId> projectIterationLocaleMap;
   private final Multimap<LocaleId, TranslationWorkspace> localeWorkspaceMap;

   public TranslationWorkspaceManagerImpl()
   {
      this.workspaceMap = new ConcurrentHashMap<WorkspaceId, TranslationWorkspace>();

      Multimap<ProjectIterationId, LocaleId> projectIterationLocaleMap = HashMultimap.create();
      this.projectIterationLocaleMap = Multimaps.synchronizedMultimap(projectIterationLocaleMap);

      Multimap<LocaleId, TranslationWorkspace> localeWorkspaceMap = HashMultimap.create();
      this.localeWorkspaceMap = Multimaps.synchronizedMultimap(localeWorkspaceMap);

   }

   @Observer(ZanataInit.EVENT_Zanata_Startup)
   public void start()
   {
      log.info("starting...");
   }

   @Observer(ZanataIdentity.USER_LOGOUT_EVENT)
   public void exitWorkspace(String username)
   {
      log.info("User logout: Removing {0} from all workspaces", username);
      ImmutableSet<TranslationWorkspace> workspaceSet = ImmutableSet.copyOf(workspaceMap.values());
      for (TranslationWorkspace workspace : workspaceSet)
      {
         if (workspace.removeTranslator(new PersonId(username)))
         {
            log.info("Removing user {0} from workspace {1}", username, workspace.getWorkspaceContext());
            // Send GWT Event to client to update the userlist
            ExitWorkspace event = new ExitWorkspace(new PersonId(username));
            workspace.publish(event);
         }
      }
   }

   @Observer(ProjectHome.PROJECT_UPDATE)
   public void projectUpdate(HIterationProject project)
   {
      log.info("Project {0} updated", project.getSlug());
      ImmutableSet<TranslationWorkspace> workspaceSet = ImmutableSet.copyOf(workspaceMap.values());
      for (TranslationWorkspace workspace : workspaceSet)
      {
         if (workspace.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getProjectSlug().equals(project.getSlug()))
         {
            ProjectUpdate event = new ProjectUpdate(project.getSlug(), project.getStatus());
            workspace.publish(event);
         }
      }
   }

   @Observer(ProjectIterationHome.PROJECT_ITERATION_UPDATE)
   public void projectIterationUpdate(HProjectIteration projectIteration)
   {
      log.info("Project iteration {0} updated", projectIteration.getSlug());
      ImmutableSet<TranslationWorkspace> workspaceSet = ImmutableSet.copyOf(workspaceMap.values());
      for (TranslationWorkspace workspace : workspaceSet)
      {
         if (workspace.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getProjectSlug().equals(projectIteration.getProject().getSlug()) &&
               workspace.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getIterationSlug().equals(projectIteration.getSlug()))
         {
            ProjectIterationUpdate event = new ProjectIterationUpdate(projectIteration.getProject().getSlug(), projectIteration.getProject().getStatus(), projectIteration.getSlug(), projectIteration.getStatus());
            workspace.publish(event);
         }
      }
   }

   @Destroy
   public void stop()
   {
      log.info("stopping...");
      log.info("closing down {0} workspaces: ", workspaceMap.size());
   }

   public ImmutableSet<LocaleId> getLocales(ProjectIterationId projectIterationId)
   {
      return ImmutableSet.copyOf(projectIterationLocaleMap.get(projectIterationId));
   }

   public ImmutableSet<LocaleId> getLocales()
   {
      return ImmutableSet.copyOf(localeWorkspaceMap.keySet());
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
            projectIterationLocaleMap.put(workspaceId.getProjectIterationId(), workspaceId.getLocaleId());
            localeWorkspaceMap.put(workspaceId.getLocaleId(), workspace);
            if (Events.exists())
               Events.instance().raiseEvent(EVENT_WORKSPACE_CREATED, workspaceId);
         }

         return prev == null ? workspace : prev;
      }

      validateAndGetWorkspaceContext(workspaceId);

      return workspace;
   }

   private WorkspaceContext validateAndGetWorkspaceContext(WorkspaceId workspaceId) throws NoSuchWorkspaceException
   {
      Session session = (Session) Component.getInstance("session");

      EntityStatus projectStatus = (EntityStatus) session.createQuery("select p.status from HProject as p where p.slug = :slug").setParameter("slug", workspaceId.getProjectIterationId().getProjectSlug()).uniqueResult();
      if (projectStatus.equals(EntityStatus.Obsolete))
      {
         throw new NoSuchWorkspaceException("Project is obsolete");
      }

      EntityStatus projectIterationStatus = (EntityStatus) session.createQuery("select it.status from HProjectIteration it where it.slug = :slug and it.project.slug = :pslug").setParameter("slug", workspaceId.getProjectIterationId().getIterationSlug()).setParameter("pslug", workspaceId.getProjectIterationId().getProjectSlug()).uniqueResult();
      if (projectIterationStatus.equals(EntityStatus.Obsolete))
      {
         throw new NoSuchWorkspaceException("Project Iteration is obsolete");
      }

      String workspaceName = (String) session.createQuery("select it.project.name || ' (' || it.slug || ')' " + "from HProjectIteration it " + "where it.slug = :slug " + "and it.project.slug = :pslug " + "and it.status <> :status").setParameter("slug", workspaceId.getProjectIterationId().getIterationSlug()).setParameter("pslug", workspaceId.getProjectIterationId().getProjectSlug()).setParameter("status", EntityStatus.Obsolete).uniqueResult();
      if (workspaceName == null)
      {
         throw new NoSuchWorkspaceException("Invalid workspace Id");
      }

      if (projectStatus.equals(EntityStatus.Retired) || projectIterationStatus.equals(EntityStatus.Retired))
      {
         return new WorkspaceContext(workspaceId, workspaceName, ULocale.getDisplayName(workspaceId.getLocaleId().toJavaName(), ULocale.ENGLISH), true);
      }

      return new WorkspaceContext(workspaceId, workspaceName, ULocale.getDisplayName(workspaceId.getLocaleId().toJavaName(), ULocale.ENGLISH), false);
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

   public TranslationWorkspace getWorkspace(WorkspaceId workspaceId)
   {
      return workspaceMap.get(workspaceId);
   }

   public ImmutableSet<TranslationWorkspace> getWorkspaces(LocaleId locale)
   {
      return ImmutableSet.copyOf(localeWorkspaceMap.get(locale));
   }

   public ImmutableSet<ProjectIterationId> getProjects()
   {
      return ImmutableSet.copyOf(projectIterationLocaleMap.keySet());
   }

}
