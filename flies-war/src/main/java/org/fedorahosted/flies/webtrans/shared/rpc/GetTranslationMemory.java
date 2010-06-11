package org.fedorahosted.flies.webtrans.shared.rpc;

import org.fedorahosted.flies.common.LocaleId;

public class GetTranslationMemory 
	extends AbstractDispatchAction<GetTranslationMemoryResult> { 
	
	public static enum SearchType {
		EXACT,
		FUZZY,
		RAW
	}

	private static final long serialVersionUID = 1L;
	private LocaleId localeId;
	private String query;
	private SearchType searchType;
	
	@SuppressWarnings("unused")
	private GetTranslationMemory(){
	}
	
	public GetTranslationMemory(String query, LocaleId localeId, SearchType searchType) {
		this.query = query;
		this.localeId = localeId;
		this.searchType = searchType;
	}
	
	public SearchType getSearchType() {
		return searchType;
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
