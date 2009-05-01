package org.fedorahosted.flies.webtrans.action;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.repository.model.Document;
import org.fedorahosted.flies.repository.model.DocumentTarget;
import org.fedorahosted.flies.repository.model.TextUnitTarget;
import org.fedorahosted.flies.webtrans.NoSuchWorkspaceException;
import org.fedorahosted.flies.webtrans.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;

@Name("translateAction")
@Scope(ScopeType.CONVERSATION)
public class TranslateAction {

	@RequestParameter("wid")
	private String workspaceId;
	
	@In(required=true)
	private TranslationWorkspaceManager translationWorkspaceManager;
	
	@In
	private EntityManager entityManager;

	@Logger
	private Log log;

	private FliesLocale locale;
	private ProjectTarget projectTarget;

	private DocumentTarget selectedDocument;
	
	private List<TextUnitTarget> preContext;
	private TextUnitTarget selectedTextUnitTarget;
	private List<TextUnitTarget> postContext;

	public String getWorkspaceId() {
		return workspaceId;
	}
	
	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}
	
	public ProjectTarget getProjectTarget() {
		return projectTarget;
	}
	
	public void setProjectTarget(ProjectTarget projectTarget) {
		this.projectTarget = projectTarget;
	}
	
	public boolean isConversationActive(){
		return projectTarget != null & locale != null; 
	}
	
	public DocumentTarget getSelectedDocument() {
		return selectedDocument;
	}
	
	public void initialize() {
		if(isConversationActive()) return;
		
		log.info("Initializing workspace for '{0}'", workspaceId);
		if(workspaceId == null){
			throw new NoSuchWorkspaceException();
		}
		else{
			String [] ws = workspaceId.split("/");
			if(ws.length != 2){
				throw new NoSuchWorkspaceException(workspaceId);
			}
			try{
				Long projectTargetId = Long.parseLong(ws[0]);
				String localeId = ws[1];
				projectTarget = entityManager.find(ProjectTarget.class, projectTargetId);
				locale = entityManager.find(FliesLocale.class, localeId);
				getWorkspace().registerTranslator("myself"+ new Date());
			}
			catch(Exception e){
				throw new NoSuchWorkspaceException(workspaceId);
			}
		}
	}

	public TranslationWorkspace getWorkspace(){
		return translationWorkspaceManager.getOrRegisterWorkspace(projectTarget, locale);
	}
	
	@End
	@Destroy
	public void destroy() {

	}
}
