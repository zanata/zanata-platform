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
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
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

   public static final String EVENT_WORKSPACE_CREATED = "webtrans.WorkspaceCreated";

   private Log log = Logging.getLog(TranslationWorkspaceManagerImpl.class);

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
      String projectSlug = project.getSlug();
      log.info("Project {0} updated, status={1}", projectSlug, project.getStatus());

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
      boolean readOnly = !isProjectIterationActive(project.getStatus(), projectIteration.getStatus());
      log.info("Project {0} iteration {1} updated, status={2}, readOnly={3}", projectSlug, iterSlug, projectIteration.getStatus(), readOnly);

      ProjectIterationId iterId = new ProjectIterationId(projectSlug, iterSlug);
      for (TranslationWorkspace workspace : projIterWorkspaceMap.get(iterId))
      {
         if (readOnly != workspace.getWorkspaceContext().isReadOnly())
         {
            workspace.getWorkspaceContext().setReadOnly(readOnly);
            WorkspaceContextUpdate event = new WorkspaceContextUpdate(readOnly);
            workspace.publish(event);
         }
      }
   }

   private boolean isProjectIterationActive(EntityStatus projectStatus, EntityStatus iterStatus)
   {
      return projectStatus.equals(EntityStatus.ACTIVE) && iterStatus.equals(EntityStatus.ACTIVE);
   }
   
   private boolean checkPermission(HProject project, HLocale locale)
   {
      return ZanataIdentity.instance().hasPermission("modify-translation", project, locale);
   }
   
   private boolean isReadOnly(HProject project, EntityStatus iterStatus, HLocale locale)
   {
      // There must be permissions and the project iteration must be in the correct state
      return !this.checkPermission(project, locale) || !this.isProjectIterationActive(project.getStatus(), iterStatus);
   }

   @Destroy
   public void stop()
   {
      log.info("stopping...");
      log.info("closing down {0} workspaces: ", workspaceMap.size());
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

      validateAndGetWorkspaceContext(workspaceId);

      return workspace;
   }

   private WorkspaceContext validateAndGetWorkspaceContext(WorkspaceId workspaceId) throws NoSuchWorkspaceException
   {
      Session session = (Session) Component.getInstance("session");

      HProject project = (HProject) session.createQuery("select p from HProject as p where p.slug = :slug").setParameter("slug", workspaceId.getProjectIterationId().getProjectSlug()).uniqueResult();
      if (project.getStatus() == EntityStatus.OBSOLETE)
      {
         throw new NoSuchWorkspaceException("Project is obsolete");
      }

      EntityStatus projectIterationStatus = (EntityStatus) session.createQuery("select it.status from HProjectIteration it where it.slug = :slug and it.project.slug = :pslug").setParameter("slug", workspaceId.getProjectIterationId().getIterationSlug()).setParameter("pslug", workspaceId.getProjectIterationId().getProjectSlug()).uniqueResult();
      if (projectIterationStatus == EntityStatus.OBSOLETE )
      {
         throw new NoSuchWorkspaceException("Project Iteration is obsolete");
      }

      String workspaceName = (String) session.createQuery("select it.project.name || ' (' || it.slug || ')' " + "from HProjectIteration it " + "where it.slug = :slug " + "and it.project.slug = :pslug " + "and it.status <> :status").setParameter("slug", workspaceId.getProjectIterationId().getIterationSlug()).setParameter("pslug", workspaceId.getProjectIterationId().getProjectSlug()).setParameter("status", EntityStatus.OBSOLETE).uniqueResult();
      if (workspaceName == null)
      {
         throw new NoSuchWorkspaceException("Invalid workspace Id");
      }
      
      HLocale locale = (HLocale) session.createQuery("select l from HLocale l where localeId = :localeId").setParameter("localeId", workspaceId.getLocaleId()).uniqueResult(); 
      if (locale == null)
      {
         throw new NoSuchWorkspaceException("Invalid Workspace Locale");
      }
      
      boolean readOnly = isReadOnly(project, projectIterationStatus, locale);
      String localeDisplayName = ULocale.getDisplayName(workspaceId.getLocaleId().toJavaName(), ULocale.ENGLISH);
      return new WorkspaceContext(workspaceId, workspaceName, localeDisplayName, readOnly);
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

}
