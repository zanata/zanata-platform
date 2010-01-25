package org.fedorahosted.flies.webtrans;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.model.HPerson;
import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.SessionEvent;
import org.fedorahosted.flies.gwt.rpc.SessionEventData;
import org.jboss.seam.core.Events;
import org.richfaces.model.LastElementAware;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

public class TranslationWorkspace {
	
	private static final String EVENT_TRANSLATOR_ENTER_WORKSPACE = "webtrans.TranslatorEnterWorkspace"; 

	private final WorkspaceKey workspaceKey;
	
	private final ConcurrentMap<SessionId, PersonId> sessions = new MapMaker().makeMap();
	private final ConcurrentMap<TransUnitId, String> editstatus = new MapMaker().makeMap();
	private final Deque<SessionEvent<?>> events = new ArrayDeque<SessionEvent<?>>();
	
	private int sequence;
	
	
	public TranslationWorkspace(WorkspaceKey workspaceKey) {
		if(workspaceKey == null)
			throw new IllegalArgumentException("workspaceKey");
		this.workspaceKey = workspaceKey;
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

	public <T extends SessionEventData> SessionEvent<T> publish(T eventData) {
		synchronized (events) {
			SessionEvent<T> event = new SessionEvent<T>(eventData, ++sequence );
			events.add(event);
			// FIXME we never remove events! (memory leak)
			return event;
		}
	}
	
	public ArrayList<SessionEvent<?>> getEventsSince(int latestSequence) {
		ArrayList<SessionEvent<?>> eventsSince = new ArrayList<SessionEvent<?>>();
		for (Iterator<SessionEvent<?>> it = events.descendingIterator(); it.hasNext();) {
			SessionEvent<?> event = (SessionEvent<?>) it.next();
			if(event.getSequence() == latestSequence) {
				break;
			}
			eventsSince.add(event);
		}
		// reverse the order of the array list so that events are in chrono order
		Collections.reverse(eventsSince);
		return eventsSince;
	}
	
	public int getSequence() {
		return sequence;
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
