package net.openl10n.flies.webtrans.client.editor.filter;

public interface ContentFilter<T>
{
   boolean accept(T value);
}
