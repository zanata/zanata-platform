package org.fedorahosted.flies.webtrans.client.filter;

public interface ContentFilter<T>
{
   boolean accept(T value);
}
