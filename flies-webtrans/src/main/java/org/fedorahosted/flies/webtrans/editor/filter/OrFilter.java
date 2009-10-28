package org.fedorahosted.flies.webtrans.editor.filter;

import java.util.ArrayList;

public class OrFilter<T> extends ArrayList<ContentFilter<T>> implements ContentFilter<T> {

	private static final long serialVersionUID = 1L;

	public OrFilter(ContentFilter<T> ... filters) {
		for(ContentFilter<T> filter : filters) {
			add(filter);
		}
	}
	
	public static <T> OrFilter<T> of(ContentFilter<T> ... filters){
		return new OrFilter<T>(filters); 
	}
	
	@Override
	public boolean accept(T value) {
		for (ContentFilter<T> filter : this) {
			if(filter.accept(value)) return true;
		}
		return false;
	}

}
