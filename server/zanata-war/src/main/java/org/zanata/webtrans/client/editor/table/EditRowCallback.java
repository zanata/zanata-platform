package org.zanata.webtrans.client.editor.table;


public interface EditRowCallback
{
   void gotoNextRow();

   void gotoPrevRow();

   void gotoFirstRow();

   void gotoLastRow();

   void gotoNextFuzzyNewRow();

   void gotoPrevFuzzyNewRow();

   void gotoNextFuzzyRow();

   void gotoPrevFuzzyRow();

   void gotoNextNewRow();

   void gotoPrevNewRow();
}
