package org.zanata.webtrans.client.editor.table;


public interface EditRowCallback
{
   void gotoNextRow(int row);

   void gotoPrevRow(int row);

   void gotoNextFuzzyNewRow(int row);

   void gotoPrevFuzzyNewRow(int row);

   void gotoNextFuzzyRow(int row);

   void gotoPrevFuzzyRow(int row);

   void gotoNextNewRow(int row);

   void gotoPrevNewRow(int row);
}
