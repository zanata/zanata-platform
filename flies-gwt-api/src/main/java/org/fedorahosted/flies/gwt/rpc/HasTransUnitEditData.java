package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public interface HasTransUnitEditData {
		
	DocumentId getDocumentId();
	TransUnitId getTransUnitId();
	String getSessionId();
}
