package org.fedorahosted.flies.webtrans.editor.filter;

public class OrFilter<T> implements ContentFilter<T>{

	private final ContentFilter<T> filter1;
	private final ContentFilter<T> filter2;
	
	public OrFilter(ContentFilter<T> filter1, ContentFilter<T> filter2) {
		this.filter1 = filter1;
		this.filter2 = filter2;
	}
	
	public static <T> OrFilter<T> of(ContentFilter<T> filter1, ContentFilter<T> filter2){
		return new OrFilter<T>(filter1, filter2); 
	}
	
	@Override
	public boolean accept(T value) {
		return filter1.accept(value) || filter2.accept(value);
	}
	
	public ContentFilter<T> getFilter1() {
		return filter1;
	}
	
	public ContentFilter<T> getFilter2() {
		return filter2;
	}
	
}
