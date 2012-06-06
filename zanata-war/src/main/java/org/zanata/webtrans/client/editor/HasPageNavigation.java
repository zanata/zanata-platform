package org.zanata.webtrans.client.editor;

public interface HasPageNavigation
{
   void gotoFirstPage();

   void gotoLastPage();

   void gotoNextPage();

   void gotoPreviousPage();

   void gotoPage(int page, boolean forced);

}
