package org.fedorahosted.flies.webtrans.server;

import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.SessionEventData;
import org.jboss.seam.annotations.In;
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
import de.novanic.eventservice.service.registry.user.UserActivityScheduler;
import de.novanic.eventservice.service.registry.user.UserInfo;
import de.novanic.eventservice.service.registry.user.UserManager;
import de.novanic.eventservice.service.registry.user.UserManagerFactory;

public class TranslationWorkspace {
	
	private static final String EVENT_TRANSLATOR_ENTER_WORKSPACE = "webtrans.TranslatorEnterWorkspace"; 

	private static final Log log = Logging.getLog(TranslationWorkspace.class);
	private final WorkspaceKey workspaceKey;
	
	private final ConcurrentMap<SessionId, PersonId> sessions = new MapMaker().makeMap();
	private final ConcurrentMap<TransUnitId, String> editstatus = new MapMaker().makeMap();
	private final Domain domain;
	@In
	private HttpSession httpSession;
	private final EventExecutorService eventExecutorService;

	public TranslationWorkspace(WorkspaceKey workspaceKey) {
		try {
			if(workspaceKey == null)
				throw new IllegalArgumentException("workspaceKey");
			this.workspaceKey = workspaceKey;
			this.domain = DomainFactory.getDomain(workspaceKey.toString());
			this.eventExecutorService = EventExecutorServiceFactory.getInstance().getEventExecutorService(httpSession);
			UserManager userManager = UserManagerFactory.getInstance().getUserManager();
			UserActivityScheduler scheduler = userManager.getUserActivityScheduler();
			scheduler.addTimeoutListener(new UserTimeoutListener() {
				@Override
				public void onTimeout(UserInfo userInfo) {
					final String sessionId = userInfo.getUserId();
					TranslationWorkspace.this.onTimeout(sessionId);
				}
			});
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			throw e;
		} catch (Error e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public LocaleId getLocale() {
		return workspaceKey.getLocaleId();
	}

	public Long getProjectContainerId() {
		return workspaceKey.getProjectContainerId();
	}
	
	public ImmutableSet<SessionId> getSessions(){
		return ImmutableSet.copyOf(sessions.keySet());
	}
	
	public String getTransUnitStatus(TransUnitId unitId) {
		return editstatus.get(unitId);
	}
	
	public void addTransUnit(TransUnitId unitId, String sessionId) {
		//Make sure this session only link to one TransUnit
		if(editstatus.containsValue(sessionId)) {
			ImmutableSet<TransUnitId> transIdSet = ImmutableSet.copyOf(editstatus.keySet());
			for(TransUnitId transId:transIdSet) {
				if(editstatus.get(transId).equals(sessionId))
					editstatus.remove(transId, sessionId);
			}
		}
		
		if(!editstatus.containsKey(unitId)) {
			editstatus.put(unitId, sessionId);
		} 
			

	}
	
	public boolean containTransUnit(TransUnitId unitId) {
		return editstatus.containsKey(unitId);
	}
	
	public ImmutableSet<String> getEditSessions() {
		return ImmutableSet.copyOf(editstatus.values());
	}
	
	public int getUserCount(){
		return sessions.size();
	}
	
	public ImmutableSet<PersonId> getUsers() {
		return ImmutableSet.copyOf(sessions.values());
	}
	
	public void removeTransUnit(TransUnitId transUnitId, String sessionId) {
		editstatus.remove(transUnitId, sessionId);
	}
	
	private void onTimeout(final String sessionId) {
		// remove user session from workspace
		PersonId personId = sessions.remove(sessionId);
		if (personId != null) {
			log.info("Timeout: Removed user '{0}' in session '{1}' from workspace {2}", 
					personId.getId(), sessionId, workspaceKey);
		} else {
			log.debug("Timeout: Unknown user for session '{0}' in workspace {1} (already logged out?)", 
					sessionId, workspaceKey);
		}
	}
	
	public boolean removeTranslator(PersonId personId) {
		ImmutableSet<SessionId> sessionIdSet = getSessions();
		for(SessionId sessionId:sessionIdSet) {
			if(sessions.get(sessionId).equals(personId)) {
				final boolean removed = sessions.remove(sessionId, personId);
				if (removed)
					log.info("Removed user '{0}' in session '{1}' from workspace {2}", 
							personId.getId(), sessionId, workspaceKey);
				return removed;
			}
		}
		return false;
	}
	
	public void registerTranslator(SessionId sessionId, PersonId personId){
		log.info("Added user '{0}' in session '{1}' to workspace {2}", 
				personId.getId(), sessionId.getValue(), workspaceKey);
		PersonId pId = sessions.putIfAbsent(sessionId, personId);
		if(pId == null && Events.exists()) Events.instance().raiseEvent(EVENT_TRANSLATOR_ENTER_WORKSPACE, workspaceKey, personId);
	}

	public <T extends SessionEventData> void publish(T eventData) {
		eventExecutorService.addEvent(domain, eventData);
	}

	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if( !(obj instanceof TranslationWorkspace) ) return false;
		TranslationWorkspace other = (TranslationWorkspace) obj;
		return other.workspaceKey.equals(workspaceKey);
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + workspaceKey.hashCode();
	    return hash;
	}
	
	public String getId() {
		return workspaceKey.toString();
	}

}
