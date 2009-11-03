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

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.HPerson;
import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.rpc.SessionEvent;
import org.fedorahosted.flies.gwt.rpc.SessionEventData;
import org.richfaces.model.LastElementAware;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

public class TranslationWorkspace {
	
	private final LocaleId locale;
	private final Long projectContainerId;
	
	private final ConcurrentMap<SessionId, PersonId> sessions = new MapMaker().makeMap();
	
	private final Deque<SessionEvent<?>> events = new ArrayDeque<SessionEvent<?>>();
	
	//private final ConcurrentLinkedQueue<SessionEvent<?>> events = 
	//	new ConcurrentLinkedQueue<SessionEvent<?>>();
	private int sequence;
	
	
	public TranslationWorkspace(Long projectContainerId, LocaleId locale) {
		if(projectContainerId == null)
			throw new IllegalArgumentException("projectId");
		if(locale == null)
			throw new IllegalArgumentException("locale");
		this.locale = locale;
		this.projectContainerId = projectContainerId;
	}
	
	public LocaleId getLocale() {
		return locale;
	}

	public Long getProjectContainerId() {
		return projectContainerId;
	}
	
	public ImmutableSet<SessionId> getSessions(){
		return ImmutableSet.copyOf(sessions.keySet());
	}
	
	public int getUserCount(){
		return sessions.size();
	}
	
	public ImmutableSet<PersonId> getUsers() {
		return ImmutableSet.copyOf(sessions.values());
	}
	
	public void registerTranslator(SessionId sessionId, PersonId personId){
		sessions.putIfAbsent(sessionId, personId);
	}

	public <T extends SessionEventData> SessionEvent<T> publish(T eventData) {
		synchronized (events) {
			SessionEvent<T> event = new SessionEvent<T>(eventData, ++sequence );
			events.offer(event);
			return event;
		}
	}
	
	public ArrayList<SessionEvent<?>> getEventsSince(int latestSequence) {
		ArrayList<SessionEvent<?>> eventsSince = new ArrayList<SessionEvent<?>>();
		for (Iterator<SessionEvent<?>> it = events.iterator(); it.hasNext();) {
			SessionEvent<?> event = (SessionEvent<?>) it.next();
			if(event.getSequence() == latestSequence) {
				break;
			}
			eventsSince.add(event);
		}
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
		return ( other.locale.equals(locale) 
				&& other.projectContainerId.equals(projectContainerId));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.hashCode();
	    hash = hash * 31 + projectContainerId.hashCode();
	    return hash;
	}
}
