package org.zanata.webtrans.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.SessionEventData;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.EventExecutorService;
import de.novanic.eventservice.service.EventExecutorServiceFactory;
import de.novanic.eventservice.service.UserTimeoutListener;
import de.novanic.eventservice.service.registry.user.UserInfo;
import de.novanic.eventservice.service.registry.user.UserManager;
import de.novanic.eventservice.service.registry.user.UserManagerFactory;

public class TranslationWorkspaceImpl implements TranslationWorkspace
{
   private static final Log log = Logging.getLog(TranslationWorkspaceImpl.class);
   private final WorkspaceContext workspaceContext;
   private final Domain domain;
   private final ConcurrentMap<EditorClientId, PersonSessionDetails> sessions = new MapMaker().makeMap();
   private final Multimap<String, EditorClientId> httpSessionToEditorClientId;
   private final Map<String, EditorClientId> connectionIdToEditorClientId;
   private final ConcurrentMap<TransUnitId, String> editstatus = new MapMaker().makeMap();

   private final EventExecutorService eventExecutorService;

   {
      ArrayListMultimap<String, EditorClientId> almm = ArrayListMultimap.create();
      httpSessionToEditorClientId = Multimaps.synchronizedListMultimap(almm);
      Map<String, EditorClientId> connMap = Maps.newHashMap();
      connectionIdToEditorClientId = Collections.synchronizedMap(connMap);
   }

   public TranslationWorkspaceImpl(WorkspaceContext workspaceContext)
   {
      Preconditions.checkNotNull(workspaceContext, "workspaceContext is null");

      this.workspaceContext = workspaceContext;
      final String workspaceId = workspaceContext.getWorkspaceId().toString();
      this.domain = DomainFactory.getDomain(workspaceId);
      EventExecutorServiceFactory factory = EventExecutorServiceFactory.getInstance();
      this.eventExecutorService = factory.getEventExecutorService(workspaceId);

      UserManager userManager = UserManagerFactory.getInstance().getUserManager();
      // this will notify us of all user timeouts (whether part of this workspace or not)
      userManager.getUserActivityScheduler().addTimeoutListener(new UserTimeoutListener()
      {
         @Override
         public void onTimeout(UserInfo userInfo)
         {
            String connectionId = userInfo.getUserId();
            EditorClientId editorClientId = connectionIdToEditorClientId.remove(connectionId);
            if (editorClientId != null)
            {
               log.info("Timeout for GWTEventService connectionId {0}; removing EditorClientId {1} from workspace {2}", connectionId, editorClientId, workspaceId);
               removeEditorClient(editorClientId);
            }
            // else EditorClientId belonged to a different workspace
         }
      });
   }

   @Override
   public WorkspaceContext getWorkspaceContext()
   {
      return workspaceContext;
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
            {
               editstatus.remove(transId, sessionId);
            }
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

   @Override
   public Map<EditorClientId, PersonSessionDetails> getUsers()
   {
      Map<EditorClientId, PersonSessionDetails> list = new HashMap<EditorClientId, PersonSessionDetails>();
      for (EditorClientId editorClientId : sessions.keySet())
      {
         list.put(editorClientId, sessions.get(editorClientId));
      }
      return list;
   }

   public void removeTransUnit(TransUnitId transUnitId, String sessionId)
   {
      editstatus.remove(transUnitId, sessionId);
   }

   @Override
   public void addEditorClient(String httpSessionId, EditorClientId editorClientId, PersonId personId)
   {
      log.info("Added user {0} with editorClientId {1} to workspace {2}", personId.getId(), editorClientId, workspaceContext);
      sessions.putIfAbsent(editorClientId, new PersonSessionDetails(new Person(personId, "", ""), null));
      httpSessionToEditorClientId.put(httpSessionId, editorClientId);
   }

   @Override
   public void onEventServiceConnected(EditorClientId editorClientId, String connectionId)
   {
      log.info("EditorClientId {0} has connectionId {1}", editorClientId, connectionId);
      connectionIdToEditorClientId.put(connectionId, editorClientId);
   }

   @Override
   public Collection<EditorClientId> removeEditorClients(String httpSessionId)
   {
      Collection<EditorClientId> editorClients = httpSessionToEditorClientId.removeAll(httpSessionId);
      for (EditorClientId editorClientId : editorClients)
      {
         removeEditorClient(editorClientId);
      }
      return editorClients;
   }

   @Override
   public boolean removeEditorClient(EditorClientId editorClientId)
   {
      PersonSessionDetails details = sessions.remove(editorClientId);
      if (details != null)
      {
         String httpSessionId = editorClientId.getHttpSessionId();
         Collection<EditorClientId> clientIds = httpSessionToEditorClientId.get(httpSessionId);
         if (clientIds != null)
         {
            clientIds.remove(editorClientId);

            // Send GWT Event to clients to update the user list
            ExitWorkspace event = new ExitWorkspace(editorClientId, details.getPerson());
            publish(event);

            log.info("Removed user {0} with editorClientId {1} from workspace {2}", details.getPerson().getId(), editorClientId, workspaceContext);
            return true;
         }
         else
         {
            log.warn("Unable to remove user {0} with editorClientId {1} from workspace {2}", details.getPerson().getId(), editorClientId, workspaceContext);
         }
      }
      else
      {
         log.debug("EditorClientId {0} not found in workspace {1}", editorClientId, workspaceContext);
         return false;
      }
      return false;
   }

   @Override
   public <T extends SessionEventData> void publish(T eventData)
   {
      eventExecutorService.addEvent(domain, eventData);
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof TranslationWorkspaceImpl))
      {
         return false;
      }
      TranslationWorkspaceImpl other = (TranslationWorkspaceImpl) obj;
      return other.workspaceContext.getWorkspaceId().equals(workspaceContext.getWorkspaceId());
   }

   @Override
   public int hashCode()
   {
      int hash = 1;
      hash = hash * 31 + workspaceContext.getWorkspaceId().hashCode();
      return hash;
   }

   @Override
   public void updateUserSelection(EditorClientId editorClientId, TransUnitId selectedTransUnitId)
   {
      if (sessions.containsKey(editorClientId))
      {
         sessions.get(editorClientId).setSelectedTransUnitId(selectedTransUnitId);
      }
   }

   @Override
   public TransUnitId getUserSelection(EditorClientId editorClientId)
   {
      if (sessions.containsKey(editorClientId))
      {
         return sessions.get(editorClientId).getSelectedTransUnitId();
      }

      return null;
   }

}
