package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class GetStatusCount implements WorkspaceAction<GetStatusCountResult> {

	private static final long serialVersionUID = -1218943735746130251L;

	private DocumentId documentId;
	private LocaleId localeId;
	private ProjectContainerId projectContainerId;

	@SuppressWarnings("unused")
	private GetStatusCount(){
	}
	
	public GetStatusCount(DocumentId id, ProjectContainerId projectContainerId, LocaleId localeId) {
		this.documentId = id;
		this.localeId = localeId;
		this.projectContainerId = projectContainerId;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}

	@Override
	public LocaleId getLocaleId() {
		return localeId;
	}
	
	@Override
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}

}
