package org.fedorahosted.flies.webtrans.server;

import java.util.concurrent.ConcurrentHashMap;

import org.fedorahosted.flies.FliesInit;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.common.WorkspaceContext;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspace;
import org.fedorahosted.flies.security.FliesIdentity;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import de.novanic.eventservice.client.event.domain.DefaultDomain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

@Scope(ScopeType.APPLICATION)
@Name("translationWorkspaceManager")
public class TranslationWorkspaceManager {

	public static final String EVENT_WORKSPACE_CREATED = "webtrans.WorkspaceCreated";
	
	@Logger
	private Log log;
	
	@In Session session;
	
	private final ConcurrentHashMap<WorkspaceId, TranslationWorkspace> workspaceMap;
	private final Multimap<Long, LocaleId> projectIterationLocaleMap;
	private final Multimap<LocaleId, TranslationWorkspace> localeWorkspaceMap;

	public TranslationWorkspaceManager() {
		this.workspaceMap = new ConcurrentHashMap<WorkspaceId, TranslationWorkspace>();

		Multimap<Long, LocaleId> projectIterationLocaleMap = HashMultimap.create();
		this.projectIterationLocaleMap = Multimaps.synchronizedMultimap(projectIterationLocaleMap);

		Multimap<LocaleId, TranslationWorkspace> localeWorkspaceMap = HashMultimap.create();
		this.localeWorkspaceMap = Multimaps.synchronizedMultimap(localeWorkspaceMap);
		
	}
	
	@Observer(FliesInit.EVENT_Flies_Startup)
	public void start(){
		log.info("starting...");
	}
	
	@Observer(FliesIdentity.USER_LOGOUT_EVENT)
	public void exitWorkspace(String username){
		log.info("User logout: Removing {0} from all workspaces", username);
		ImmutableSet<TranslationWorkspace> workspaceSet=ImmutableSet.copyOf(workspaceMap.values());
		for(TranslationWorkspace workspace : workspaceSet) {
			if(workspace.removeTranslator(new PersonId(username))) {
				log.info("Removing user {0} from workspace {1}", username, workspace.getWorkspaceContext());
				//Send GWT Event to client to update the userlist
				ExitWorkspace event = new ExitWorkspace(new PersonId(username));
				workspace.publish(event);
			}
		}
	}
	
	@Destroy
	public void stop(){
		log.info("stopping...");
		log.info("closing down {0} workspaces: ", workspaceMap.size());
	}
	
	public ImmutableSet<LocaleId> getLocales(Long projectContainerId){
		return ImmutableSet.copyOf(projectIterationLocaleMap.get(projectContainerId));
	}

	public ImmutableSet<LocaleId> getLocales(){
		return ImmutableSet.copyOf(localeWorkspaceMap.keySet());
	}
	
	public int getWorkspaceCount(){
		return workspaceMap.size();
	}

	public TranslationWorkspace getOrRegisterWorkspace(WorkspaceId workspaceId) {
		TranslationWorkspace workspace = workspaceMap.get(workspaceId);
		if(workspace == null){
			workspace = createWorkspace(workspaceId);
			TranslationWorkspace prev = workspaceMap.putIfAbsent(workspaceId, workspace);
			
			if(prev == null){
				projectIterationLocaleMap.put(workspaceId.getProjectContainerId().getId(), workspaceId.getLocaleId());
				localeWorkspaceMap.put(workspaceId.getLocaleId(), workspace);
				if(Events.exists()) Events.instance().raiseEvent(EVENT_WORKSPACE_CREATED, workspaceId);
			}
			
			return prev == null ? workspace : prev;
		}
		return workspace;
	}
	
	private TranslationWorkspace createWorkspace(WorkspaceId workspaceId) {
		String iterationName = (String) session.createQuery(
				"select it.name " +
				"from HProjectIteration it " +
				"where it.container.id = :containerId "
				)
				.setParameter("containerId", workspaceId.getProjectContainerId().getId())
				.uniqueResult();
		
		String projectName = (String) session.createQuery(
				"select it.project.name " +
				"from HProjectIteration it " +
				"where it.container.id = :containerId "
				)
				.setParameter("containerId", workspaceId.getProjectContainerId().getId())
				.uniqueResult();

		WorkspaceContext workspaceContext = 
			new WorkspaceContext(workspaceId, 
					projectName + " (" + iterationName + ")",
					"Locale Name");
		return new TranslationWorkspace(workspaceContext);
	}
	
	public TranslationWorkspace getWorkspace(ProjectContainerId projectContainerId, LocaleId localeId) {
		WorkspaceId workspaceId = new WorkspaceId(projectContainerId, localeId);
		return getWorkspace(workspaceId);
	}
	
	public TranslationWorkspace getWorkspace(WorkspaceId workspaceId) {
		return workspaceMap.get(workspaceId);
	}
	
	public ImmutableSet<TranslationWorkspace> getWorkspaces(LocaleId locale){
		return ImmutableSet.copyOf(localeWorkspaceMap.get(locale));
	}
	
	public ImmutableSet<Long> getProjects(){
		return ImmutableSet.copyOf(projectIterationLocaleMap.keySet());
	}
	
	
}
