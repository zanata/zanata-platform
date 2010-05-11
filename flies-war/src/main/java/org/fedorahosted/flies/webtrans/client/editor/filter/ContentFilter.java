package org.fedorahosted.flies.webtrans.client.editor.filter;

public interface ContentFilter<T> {
	boolean accept(T value);
}
