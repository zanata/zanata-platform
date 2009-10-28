package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.inject.internal.SourceProvider;

public class PhraseFilter implements ContentFilter<TransUnit>{
	private String phrase;
	
	public PhraseFilter(String phrase) {
		this.phrase = phrase;
	}
	
	public static PhraseFilter from(String phrase) {
		return new PhraseFilter(phrase);
	}
	
	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
	
	@Override
	public boolean accept(TransUnit value) {
		return value.getSource().contains(phrase) || value.getTarget().contains(phrase);
	}


}
