package org.fedorahosted.flies.webtrans.shared.rpc;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.webtrans.shared.common.WorkspaceId;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.ProjectIterationId;

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
