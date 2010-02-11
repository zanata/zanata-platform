package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.LocaleId;

public class GetTranslationMemory implements Action<GetTranslationMemoryResult> {

	private static final long serialVersionUID = 1L;
	private LocaleId localeId;
	private String query;
	private boolean fuzzy;
	
	@SuppressWarnings("unused")
	private GetTranslationMemory(){
	}
	
	public GetTranslationMemory(String query, LocaleId localeId, boolean fuzzy) {
		this.query = query;
		this.localeId = localeId;
		this.fuzzy = fuzzy;
	}
	
	public boolean getFuzzy() {
		return this.fuzzy;
	}
	
	public void setLocaleId(LocaleId localeId) {
		this.localeId = localeId;
	}

	public LocaleId getLocaleId() {
		return localeId;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

}
