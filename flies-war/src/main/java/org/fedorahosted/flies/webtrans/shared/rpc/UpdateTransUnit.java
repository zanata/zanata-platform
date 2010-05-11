package org.fedorahosted.flies.webtrans.shared.rpc;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.webtrans.shared.common.WorkspaceId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class UpdateTransUnit extends AbstractWorkspaceAction<UpdateTransUnitResult> {
	
	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private String content;
	private ContentState contentState;
	
	@SuppressWarnings("unused")
	private UpdateTransUnit() {
	}
	
	public UpdateTransUnit(TransUnitId transUnitId, String content, ContentState contentState) {
		this.transUnitId = transUnitId;
		this.content = content;
		this.contentState = contentState;
	}
	
	public String getContent() {
		return content;
	}
	
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}

	public ContentState getContentState() {
		return contentState;
	}
}
