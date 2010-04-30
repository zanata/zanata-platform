package org.fedorahosted.flies.search;

import java.util.Locale;

import net.sf.okapi.lib.search.lucene.analysis.NgramAnalyzer;


public class DefaultNgramAnalyzer extends NgramAnalyzer {

	public DefaultNgramAnalyzer() {
		super(Locale.ENGLISH, 3);
	}

}
