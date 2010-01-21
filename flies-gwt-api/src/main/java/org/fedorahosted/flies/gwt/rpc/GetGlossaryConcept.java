package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.LocaleId;


public class GetGlossaryConcept  implements Action<GetGlossaryConceptResult> {

	private static final long serialVersionUID = 1L;
	private LocaleId localeId;
	private String term;
	private long glossaryId;
	
	@SuppressWarnings("unused")
	private GetGlossaryConcept(){
	}
	
	public GetGlossaryConcept(String term, LocaleId localeId, long glossaryId) {
		this.term = term;
		this.localeId = localeId;
		this.glossaryId = glossaryId;
	}
	
	public void setLocaleId(LocaleId localeId) {
		this.localeId = localeId;
	}

	public LocaleId getLocaleId() {
		return localeId;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	public void setGlossaryId(long glossaryId) {
		this.glossaryId = glossaryId;
	}

	public long getGlossaryId() {
		return glossaryId;
	}
	
}

