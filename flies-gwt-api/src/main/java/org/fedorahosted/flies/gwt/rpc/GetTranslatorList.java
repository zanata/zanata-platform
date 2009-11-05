package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class GetTranslatorList implements WorkspaceAction<GetTranslatorListResult> {

	private static final long serialVersionUID = -4430737335471654451L;
	private LocaleId localeId;
	private ProjectContainerId projectContainerId;

	@SuppressWarnings("unused")
	private GetTranslatorList() {
	}
	
	public GetTranslatorList (ProjectContainerId projectContainerId, LocaleId localeId) {
		this.localeId = localeId;
		this.projectContainerId = projectContainerId;
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

