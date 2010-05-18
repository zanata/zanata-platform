package org.fedorahosted.flies.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.webtrans.shared.model.TransMemory;

public class GetTranslationMemoryResult implements Result {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<TransMemory> transmemories;
	
	@SuppressWarnings("unused")
	private GetTranslationMemoryResult() {
	}
	
	public GetTranslationMemoryResult(ArrayList<TransMemory> transmemories) {
		this.transmemories = transmemories;
	}

	public ArrayList<TransMemory> getMemories() {
		return transmemories;
	}
	
	public void setConcepts(ArrayList<TransMemory> transmemories) {
		this.transmemories = transmemories;
	}
}
