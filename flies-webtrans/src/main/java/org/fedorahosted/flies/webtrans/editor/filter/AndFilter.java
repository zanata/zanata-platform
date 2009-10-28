package org.fedorahosted.flies.webtrans.editor.filter;

import java.util.ArrayList;

public class AndFilter<T> extends ArrayList<ContentFilter<T>> implements ContentFilter<T> {

	private static final long serialVersionUID = 1L;

	public AndFilter(ContentFilter<T> ... filters) {
		for(ContentFilter<T> filter : filters) {
			add(filter);
		}
	}
	
	public static <T> AndFilter<T> of(ContentFilter<T> ... filters){
		return new AndFilter<T>(filters); 
	}
	
	@Override
	public boolean accept(T value) {
		boolean accept = false;
		for (ContentFilter<T> filter : this) {
			accept &= filter.accept(value);
		}
		return accept;
	}

}
