package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.inject.internal.SourceProvider;

public class PhraseFilter extends OrFilter<TransUnit>{
	
	public PhraseFilter(SourcePhraseFilter sourceFilter, TargetPhraseFilter targetFilter) {
		super(sourceFilter, targetFilter);
	}
	
	public static PhraseFilter from(String phrase) {
		return new PhraseFilter(SourcePhraseFilter.from(phrase), TargetPhraseFilter.from(phrase));
	}

}
