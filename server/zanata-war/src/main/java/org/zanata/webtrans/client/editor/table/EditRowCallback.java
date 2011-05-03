package org.zanata.webtrans.client.editor.table;


public interface EditRowCallback
{
   void gotoNextRow(int row);

   void gotoPrevRow(int row);

   void gotoNextFuzzy(int row);

   void gotoPrevFuzzy(int row);
}
