package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public interface HasTransUnitUpdatedData {

	DocumentId getDocumentId();

	TransUnitStatus getNewStatus();

	TransUnitStatus getPreviousStatus();

	TransUnitId getTransUnitId();

}
