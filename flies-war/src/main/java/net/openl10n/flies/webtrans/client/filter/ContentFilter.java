package net.openl10n.flies.webtrans.client.filter;

public interface ContentFilter<T>
{
   boolean accept(T value);
}
