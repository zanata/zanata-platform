package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.Person;

public class GetTranslatorListResult implements Result {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<Person> translatorlist;
	
	@SuppressWarnings("unused")
	private GetTranslatorListResult() {
	}
	
	public GetTranslatorListResult (ArrayList<Person> translatorlist) {
		this.translatorlist = translatorlist;
	}
	
	public ArrayList<Person> getTranslatorList() {
		return translatorlist;
	}
}
