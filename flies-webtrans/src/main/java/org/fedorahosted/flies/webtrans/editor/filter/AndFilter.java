package org.fedorahosted.flies.webtrans.editor.filter;

public class AndFilter<T> implements ContentFilter<T>{

	private final ContentFilter<T> filter1;
	private final ContentFilter<T> filter2;
	
	public AndFilter(ContentFilter<T> filter1, ContentFilter<T> filter2) {
		this.filter1 = filter1;
		this.filter2 = filter2;
	}
	
	public static <T> AndFilter<T> of(ContentFilter<T> filter1, ContentFilter<T> filter2){
		return new AndFilter<T>(filter1, filter2); 
	}
	
	@Override
	public boolean accept(T value) {
		return filter1.accept(value) && filter2.accept(value);
	}
	
}
