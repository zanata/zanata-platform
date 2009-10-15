package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class GetTransUnitsResult implements Result, IsSerializable {

	private static final long serialVersionUID = 3481107839585398632L;

	private DocumentId documentId;
	private int totalCount;
	private ArrayList<TransUnit> units;

	@SuppressWarnings("unused")
	private GetTransUnitsResult()	{
	}
	
	public GetTransUnitsResult(DocumentId documentId, ArrayList<TransUnit> units, int totalCount) {
		this.documentId = documentId;
		this.units = units;
		this.totalCount = totalCount;
	}
	
	public ArrayList<TransUnit> getUnits() {
		return units;
	}
	
	public int getTotalCount() {
		return totalCount;
	}
	
	public DocumentId getDocumentId() {
		return documentId;
	}
	
	
}
