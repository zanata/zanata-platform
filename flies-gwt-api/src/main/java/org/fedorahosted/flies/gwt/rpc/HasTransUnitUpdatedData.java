package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public interface HasTransUnitUpdatedData {

	DocumentId getDocumentId();

	ContentState getNewStatus();

	ContentState getPreviousStatus();

	TransUnitId getTransUnitId();

}
