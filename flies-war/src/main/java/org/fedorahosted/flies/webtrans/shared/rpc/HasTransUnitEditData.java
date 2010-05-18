package org.fedorahosted.flies.webtrans.shared.rpc;

import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;

public interface HasTransUnitEditData {
		
	DocumentId getDocumentId();
	TransUnitId getTransUnitId();
	String getSessionId();
}
