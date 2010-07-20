package org.fedorahosted.flies.webtrans.client.editor;

public interface HasPageNavigation
{
   public void gotoFirstPage();

   public void gotoLastPage();

   public void gotoNextPage();

   public void gotoPreviousPage();

   public void gotoPage(int page, boolean forced);

}
