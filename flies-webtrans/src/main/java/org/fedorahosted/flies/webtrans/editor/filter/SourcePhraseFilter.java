package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.gwt.model.TransUnit;

public class SourcePhraseFilter implements ContentFilter<TransUnit> {

	private final String phrase;
	
	public SourcePhraseFilter(String phrase) {
		this.phrase = phrase;
	}
	
	@Override
	public boolean accept(TransUnit value) {
		return value.getSource().contains(phrase);
	}
	
	public static SourcePhraseFilter from(String phrase) {
		return new SourcePhraseFilter(phrase);
	}

}
