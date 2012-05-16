package org.zanata.webtrans.client.editor.table;


import org.zanata.webtrans.shared.model.TransUnit;

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

   void gotoCurrentRow(boolean andEdit);

   void setRowValueOverride(int row, TransUnit targetCell);
}
