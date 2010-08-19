package org.fedorahosted.flies.webtrans.server;

import java.util.concurrent.ConcurrentMap;

import org.fedorahosted.flies.webtrans.shared.auth.SessionId;
import org.fedorahosted.flies.webtrans.shared.model.PersonId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;
import org.fedorahosted.flies.webtrans.shared.model.WorkspaceContext;
import org.fedorahosted.flies.webtrans.shared.rpc.SessionEventData;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.EventExecutorService;
import de.novanic.eventservice.service.EventExecutorServiceFactory;
import de.novanic.eventservice.service.UserTimeoutListener;
import de.novanic.eventservice.service.registry.user.UserInfo;
import de.novanic.eventservice.service.registry.user.UserManager;
import de.novanic.eventservice.service.registry.user.UserManagerFactory;

public class TranslationWorkspace
{

   private static final String EVENT_TRANSLATOR_ENTER_WORKSPACE = "webtrans.TranslatorEnterWorkspace";

   private static final Log log = Logging.getLog(TranslationWorkspace.class);
   private final WorkspaceContext workspaceContext;
   private final Domain domain;
   private final ConcurrentMap<SessionId, PersonId> sessions = new MapMaker().makeMap();
   private final ConcurrentMap<TransUnitId, String> editstatus = new MapMaker().makeMap();

   private final EventExecutorService eventExecutorService;

   public TranslationWorkspace(WorkspaceContext workspaceContext)
   {
      if (workspaceContext == null)
         throw new IllegalArgumentException("workspaceContext is null");
      this.workspaceContext = workspaceContext;
      this.domain = DomainFactory.getDomain(workspaceContext.getWorkspaceId().toString());
      this.eventExecutorService = EventExecutorServiceFactory.getInstance().getEventExecutorService(workspaceContext.getWorkspaceId().toString());
      UserManager userManager = UserManagerFactory.getInstance().getUserManager();
      userManager.getUserActivityScheduler().addTimeoutListener(new UserTimeoutListener()
      {
         @Override
         public void onTimeout(UserInfo userInfo)
         {
            final String sessionId = userInfo.getUserId();
            TranslationWorkspace.this.onTimeout(sessionId);
         }
      });
   }

   public WorkspaceContext getWorkspaceContext()
   {
      return workspaceContext;
   }

   public ImmutableSet<SessionId> getSessions()
   {
      return ImmutableSet.copyOf(sessions.keySet());
   }

   public String getTransUnitStatus(TransUnitId unitId)
   {
      return editstatus.get(unitId);
   }

   public void addTransUnit(TransUnitId unitId, String sessionId)
   {
      // Make sure this session only link to one TransUnit
      if (editstatus.containsValue(sessionId))
      {
         ImmutableSet<TransUnitId> transIdSet = ImmutableSet.copyOf(editstatus.keySet());
         for (TransUnitId transId : transIdSet)
         {
            if (editstatus.get(transId).equals(sessionId))
               editstatus.remove(transId, sessionId);
         }
      }

      if (!editstatus.containsKey(unitId))
      {
         editstatus.put(unitId, sessionId);
      }

   }

   public boolean containTransUnit(TransUnitId unitId)
   {
      return editstatus.containsKey(unitId);
   }

   public ImmutableSet<String> getEditSessions()
   {
      return ImmutableSet.copyOf(editstatus.values());
   }

   public int getUserCount()
   {
      return sessions.size();
   }

   public ImmutableSet<PersonId> getUsers()
   {
      return ImmutableSet.copyOf(sessions.values());
   }

   public void removeTransUnit(TransUnitId transUnitId, String sessionId)
   {
      editstatus.remove(transUnitId, sessionId);
   }

   private void onTimeout(final String sessionId)
   {
      // remove user session from workspace
      PersonId personId = sessions.remove(sessionId);
      if (personId != null)
      {
         log.info("Timeout: Removed user '{0}' in session '{1}' from workspace {2}", personId.getId(), sessionId, workspaceContext);
      }
      else
      {
         log.debug("Timeout: Unknown user for session '{0}' in workspace {1} (already logged out?)", sessionId, workspaceContext);
      }
   }

   public boolean removeTranslator(PersonId personId)
   {
      ImmutableSet<SessionId> sessionIdSet = getSessions();
      for (SessionId sessionId : sessionIdSet)
      {
         if (sessions.get(sessionId).equals(personId))
         {
            final boolean removed = sessions.remove(sessionId, personId);
            if (removed)
               log.info("Removed user '{0}' in session '{1}' from workspace {2}", personId.getId(), sessionId, workspaceContext);
            return removed;
         }
      }
      return false;
   }

   public void registerTranslator(SessionId sessionId, PersonId personId)
   {
      log.info("Added user '{0}' in session '{1}' to workspace {2}", personId.getId(), sessionId.getValue(), workspaceContext);
      PersonId pId = sessions.putIfAbsent(sessionId, personId);
      if (pId == null && Events.exists())
         Events.instance().raiseEvent(EVENT_TRANSLATOR_ENTER_WORKSPACE, workspaceContext.getWorkspaceId(), personId);
   }

   public <T extends SessionEventData> void publish(T eventData)
   {
      eventExecutorService.addEvent(domain, eventData);
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (!(obj instanceof TranslationWorkspace))
         return false;
      TranslationWorkspace other = (TranslationWorkspace) obj;
      return other.workspaceContext.getWorkspaceId().equals(workspaceContext.getWorkspaceId());
   }

   @Override
   public int hashCode()
   {
      int hash = 1;
      hash = hash * 31 + workspaceContext.getWorkspaceId().hashCode();
      return hash;
   }

}
