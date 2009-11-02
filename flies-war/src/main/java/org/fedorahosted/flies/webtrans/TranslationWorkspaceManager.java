package org.fedorahosted.flies.webtrans;

import java.util.concurrent.ConcurrentHashMap;

import org.fedorahosted.flies.FliesInit;
import org.fedorahosted.flies.LocaleId;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Scope(ScopeType.APPLICATION)
@Name("translationWorkspaceManager")
public class TranslationWorkspaceManager {

	@Logger
	private Log log;

	private final ConcurrentHashMap<WorkspaceKey, TranslationWorkspace> workspaceMap;
	private final Multimap<Long, LocaleId> projectIterationLocaleMap;
	private final Multimap<LocaleId, TranslationWorkspace> localeWorkspaceMap;

	public TranslationWorkspaceManager() {
		this.workspaceMap = new ConcurrentHashMap<WorkspaceKey, TranslationWorkspace>();

		Multimap<Long, LocaleId> projectIterationLocaleMap = HashMultimap.create();
		this.projectIterationLocaleMap = Multimaps.synchronizedMultimap(projectIterationLocaleMap);

		Multimap<LocaleId, TranslationWorkspace> localeWorkspaceMap = HashMultimap.create();
		this.localeWorkspaceMap = Multimaps.synchronizedMultimap(localeWorkspaceMap);
		
	}
	
	@Observer(FliesInit.EVENT_Flies_Startup)
	public void start(){
		log.info("starting...");
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

	public TranslationWorkspace getOrRegisterWorkspace(WorkspaceKey workspaceKey) {
		TranslationWorkspace workspace = workspaceMap.get(workspaceKey);
		if(workspace == null){
			workspace = new TranslationWorkspace(workspaceKey.getProjectContainerId(), workspaceKey.getLocaleId());
			TranslationWorkspace prev = workspaceMap.putIfAbsent(workspaceKey, workspace);
			
			if(prev == null){
				projectIterationLocaleMap.put(workspaceKey.getProjectContainerId(), workspaceKey.getLocaleId());
				localeWorkspaceMap.put(workspaceKey.getLocaleId(), workspace);
			}
			
			return prev == null ? workspace : prev;
		}
		return workspace;
	}
	public TranslationWorkspace getOrRegisterWorkspace(Long projectContainerId, LocaleId localeId){
		WorkspaceKey key = new WorkspaceKey(projectContainerId, localeId);
		return getOrRegisterWorkspace(key);
	}
	
	public ImmutableSet<TranslationWorkspace> getWorkspaces(LocaleId locale){
		return ImmutableSet.copyOf(localeWorkspaceMap.get(locale));
	}
	
	public ImmutableSet<Long> getProjects(){
		return ImmutableSet.copyOf(projectIterationLocaleMap.keySet());
	}
	
	
}
