package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.Person;

public class GetTranslatorListResult implements Result {

	private static final long serialVersionUID = 1L;
	
	private Person[] translatorlist;
	
	@SuppressWarnings("unused")
	private GetTranslatorListResult() {
	}
	
	public GetTranslatorListResult (Person[] translatorlist) {
		this.translatorlist = translatorlist;
	}
	
	public Person[] getTranslatorList() {
		return translatorlist;
	}
}
