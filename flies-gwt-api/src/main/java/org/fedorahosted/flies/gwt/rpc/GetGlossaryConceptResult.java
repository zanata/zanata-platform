package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;
import java.util.List;

import org.fedorahosted.flies.gwt.model.Concept;

import net.customware.gwt.dispatch.shared.Result;



public class GetGlossaryConceptResult implements Result {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<Concept> concepts;
	
	@SuppressWarnings("unused")
	private GetGlossaryConceptResult() {
	}
	
	public GetGlossaryConceptResult(ArrayList<Concept> concepts) {
		this.concepts = concepts;
	}

	public ArrayList<Concept> getConcepts() {
		return concepts;
	}
	
	public void setConcepts(ArrayList<Concept> concepts) {
		this.concepts = concepts;
	}

}
