package org.fedorahosted.flies.webtrans.action;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.core.model.Person;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.repository.model.DocumentTarget;
import org.fedorahosted.flies.repository.model.TextUnitTarget;
import org.fedorahosted.flies.webtrans.NoSuchWorkspaceException;
import org.fedorahosted.flies.webtrans.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
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

	@DataModel
	private List<DocumentTarget> documentTargets;

	@DataModelSelection(value="documentTargets")
	@Out(required=false)
	private DocumentTarget selectedDocumentTarget;


	@DataModel
	private List<TextUnitTarget> textUnitTargets;
	
	@DataModelSelection(value="textUnitTargets")
	@Out(required=false)
	private TextUnitTarget selectedTextUnitTarget;
	
	
	public void selectDocumentTarget(){
		log.info("selected {0}", selectedDocumentTarget.getTemplate().getName());
		loadTextUnitTargets();
	}
	
	@Factory("documentTargets")
	public void loadDocumentTargets(){
		documentTargets = entityManager.createQuery("select d from DocumentTarget d " +
								"where d.locale = :locale and d.template.projectTarget = :target")
					.setParameter("locale", locale)
					.setParameter("target", projectTarget).getResultList();
	}

	@Factory("textUnitTargets")
	public void loadTextUnitTargets(){
		log.info("retrieving textUnitTargets...");
		if(selectedDocumentTarget == null) {
			log.info("none available...");
			textUnitTargets =  Collections.EMPTY_LIST;
		}
		else {
			log.info("retrieving entries. count: {0} ", selectedDocumentTarget.getEntries().size());
			textUnitTargets = selectedDocumentTarget.getEntries();
		}
	}
	
	public void selectTextUnitTarget(){
		log.info("selected {0}", selectedTextUnitTarget.getTemplate().getContent());
	}
	
	public boolean isConversationActive(){
		return projectTarget != null & locale != null; 
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
				Person translator = entityManager.find(Person.class, 1l);
				getWorkspace().registerTranslator(translator);
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
	
	public boolean isKeepAlive(){
		log.info("keepAlive {0}:{1} - {2}", this.projectTarget.getProject().getName(), this.projectTarget.getName(), this.locale.getId());
		Person translator = entityManager.find(Person.class, 1l);
		getWorkspace().registerTranslator(translator);
		return true;
	}
}
