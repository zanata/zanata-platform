package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectIterationId;

public class GetStatusCount extends AbstractWorkspaceAction<GetStatusCountResult> {

	private DocumentId documentId;

	@SuppressWarnings("unused")
	private GetStatusCount(){
	}
	
	public GetStatusCount(DocumentId id) {
		this.documentId = id;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}

}
