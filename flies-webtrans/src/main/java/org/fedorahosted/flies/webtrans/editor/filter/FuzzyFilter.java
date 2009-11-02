package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.TransUnitStatus;

public class FuzzyFilter implements ContentFilter<TransUnit>{
	private final boolean fuzzy;
	
	private FuzzyFilter(boolean fuzzy) {
		this.fuzzy = fuzzy;
	}
	
	@Override
	public boolean accept(TransUnit value) {
		return value.getStatus() == TransUnitStatus.NeedReview;
	}

	private static final FuzzyFilter FUZZY = new FuzzyFilter(true);
	private static final FuzzyFilter NONFUZZY = new FuzzyFilter(false);
	
	public static FuzzyFilter fuzzy() {
		return FUZZY;
	}
	
	public static FuzzyFilter nonFuzzy() {
		return NONFUZZY;
	}
	
	public static FuzzyFilter from(boolean fuzzy){
		return fuzzy ? FUZZY : NONFUZZY;
	}

}
