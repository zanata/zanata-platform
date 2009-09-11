package org.fedorahosted.flies.webtrans.action;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.Person;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HDocumentTarget;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HResource;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
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
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.richfaces.model.selection.SimpleSelection;

@Name("translateAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class TranslateAction implements Serializable {

	private static final long serialVersionUID = -6246244099116248138L;

	@RequestParameter("wid")
	private String workspaceId;
	
	@In(required=true)
	private TranslationWorkspaceManager translationWorkspaceManager;
	
	@In
	private EntityManager entityManager;

	@Logger
	private Log log;

	@In(required=false, value=JpaIdentityStore.AUTHENTICATED_USER) 
	HAccount authenticatedAccount;

	private LocaleId locale;
	private HProjectContainer projectContainer;
	private ProjectIteration projectIteration;

	public String getWorkspaceId() {
		return workspaceId;
	}
	
	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}
	
	public HProjectContainer getProjectContainer() {
		return projectContainer;
	}
	
	public void setProjectContainer(HProjectContainer project) {
		this.projectContainer = project;
	}

	public ProjectIteration getProjectIteration() {
		return projectIteration;
	}
	
	public void setProjectIteration(ProjectIteration projectIteration) {
		this.projectIteration = projectIteration;
	}
	
	public LocaleId getLocale() {
		return locale;
	}

	@DataModel
	private List<HDocumentTarget> documentTargets;

	@DataModelSelection(value="documentTargets")
	@Out(required=false)
	private HDocumentTarget selectedDocumentTarget;

	@In(value="flies.tftDataModel")
	@MyDataModel
	private TextFlowTargetDataModel textFlowTargets;
	
	private HTextFlowTarget selectedTextFlowTarget;
	
	public HTextFlowTarget getSelectedTextFlowTarget() {
		return selectedTextFlowTarget;
	}
	
	private SimpleSelection selectedRow;
	
	public SimpleSelection getSelectedRow() {
		return selectedRow;
	}
	
	public void setSelectedRow(SimpleSelection selectedRow) {
		this.selectedRow = selectedRow;
		setSelectedTextFlowTarget((HTextFlowTarget) selectedRow.getKeys().next() );
	}
	
	public void setSelectedTextFlowTarget(HTextFlowTarget selectedTextFlowTarget) {
		this.selectedTextFlowTarget = selectedTextFlowTarget;
		log.info("set selected {0}", selectedTextFlowTarget.getId() );
	}
	
	public void selectDocumentTarget(){
		log.info("selected {0}", selectedDocumentTarget.getTemplate().getName());
		//loadTextFlowTargets();
	}
	
	@Factory("documentTargets")
	public void loadDocumentTargets(){
		documentTargets = entityManager.createQuery("select d from HDocumentTarget d " +
								"where d.locale = :locale and d.template.project = :project")
					.setParameter("locale", locale)
					.setParameter("project", projectContainer).getResultList();
	}

	//@Factory("textFlowTargets")
	public void loadTextFlowTargets(){
		log.info("retrieving textFlowTargets...");
		textFlowTargets = new TextFlowTargetDataModel();
		/*
		if(selectedDocumentTarget == null) {
			log.info("none available...");
			textFlowTargets =  Collections.EMPTY_LIST;
		}
		else {
			log.info("retrieving entries. count: {0} ", selectedDocumentTarget.getTargets().size());
			textFlowTargets =  new ArrayList<HTextFlowTarget>(selectedDocumentTarget.getTargets() );
		}
		*/
	}
	
	public void selectTextFlowTarget(){
		log.info("selected {0}", selectedTextFlowTarget.getId() );
	}

	
	public boolean isConversationActive(){
		return projectContainer != null && locale != null; 
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
				Long projectIterationId = Long.parseLong(ws[0]);
				String localeId = ws[1];
				projectIteration = entityManager.find(ProjectIteration.class, projectIterationId);
				projectContainer = projectIteration.getContainer();
				
				log.info("Initializing targets");
				locale = new LocaleId(localeId);

				for(HDocument doc : projectContainer.getDocuments() ){
					if(!doc.getTargets().containsKey(locale)){
						HDocumentTarget docTarget = new HDocumentTarget(doc, locale);
						entityManager.persist(docTarget);
						for (HResource res : doc.getResources().values() ){
							if(res instanceof HTextFlow){
								HTextFlowTarget target = new HTextFlowTarget(docTarget, (HTextFlow) res);
								target.setContent("");
								entityManager.persist(target);
							}
								
						}
					}
				}
				Person translator = entityManager.find(Person.class, authenticatedAccount.getPerson().getId());
				getWorkspace().registerTranslator(translator);
			}
			catch(Exception e){
				throw new NoSuchWorkspaceException(workspaceId);
			}
		}
	}

	public TranslationWorkspace getWorkspace(){
		return translationWorkspaceManager.getOrRegisterWorkspace(projectContainer.getId(), locale);
	}
	
	@End
	@Destroy
	public void destroy() {
	}
	
	public boolean ping(){
		Person translator = entityManager.find(Person.class, authenticatedAccount.getPerson().getId());
		log.info("ping {0}:{1} - {2} - {3}", 
				projectIteration.getProject().getSlug(),
				projectIteration.getSlug(), 
				this.locale,
				translator.getAccount().getUsername()
				);
		getWorkspace().registerTranslator(translator);
		return true;
	}
	public void persistChanges(){
		if(selectedTextFlowTarget != null){
			entityManager.flush();
		}
	}
}
