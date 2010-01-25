package org.fedorahosted.flies.webtrans;

import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.SessionEventData;
import org.jboss.seam.annotations.In;
import org.jboss.seam.core.Events;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.EventExecutorService;
import de.novanic.eventservice.service.EventExecutorServiceFactory;

public class TranslationWorkspace {
	
	private static final String EVENT_TRANSLATOR_ENTER_WORKSPACE = "webtrans.TranslatorEnterWorkspace"; 

	private final WorkspaceKey workspaceKey;
	
	private final ConcurrentMap<SessionId, PersonId> sessions = new MapMaker().makeMap();
	private final ConcurrentMap<TransUnitId, String> editstatus = new MapMaker().makeMap();
	private final Domain domain;
	@In
	private HttpSession httpSession;
	private final EventExecutorService eventExecutorService;
	
	public TranslationWorkspace(WorkspaceKey workspaceKey) {
		if(workspaceKey == null)
			throw new IllegalArgumentException("workspaceKey");
		this.workspaceKey = workspaceKey;
		this.domain = DomainFactory.getDomain(workspaceKey.toString());
		this.eventExecutorService = EventExecutorServiceFactory.getInstance().getEventExecutorService(httpSession);
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
	
	public boolean removeTranslator(PersonId personId) {
		ImmutableSet<SessionId> sessionIdSet = getSessions();
		for(SessionId sessionId:sessionIdSet) {
			if(sessions.get(sessionId).equals(personId))
				return sessions.remove(sessionId, personId);
		}
		return false;
	}
	
	public void registerTranslator(SessionId sessionId, PersonId personId){
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
}
