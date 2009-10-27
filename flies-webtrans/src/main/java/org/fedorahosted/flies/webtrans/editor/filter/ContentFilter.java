package org.fedorahosted.flies.webtrans.editor.filter;

public interface ContentFilter<T> {
	boolean accept(T value);
}
