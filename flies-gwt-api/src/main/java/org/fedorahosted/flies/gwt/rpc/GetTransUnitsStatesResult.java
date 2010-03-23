package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public class GetTransUnitsStatesResult implements Result {

	private static final long serialVersionUID = 1L;

	private DocumentId documentId;
	private ArrayList<TransUnitId> units;

	@SuppressWarnings("unused")
	private GetTransUnitsStatesResult()	{
	}
	
	public GetTransUnitsStatesResult(DocumentId documentId, ArrayList<TransUnitId> units) {
		this.documentId = documentId;
		this.units = units;
	}
	
	public ArrayList<TransUnitId> getUnits() {
		return units;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}
}
