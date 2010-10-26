package net.openl10n.flies.webtrans.client.editor.table;

import net.openl10n.flies.common.ContentState;

public interface EditRowCallback
{
   void gotoNextRow(int row);

   void gotoPrevRow(int row);

   void gotoNextFuzzy(int row, ContentState state);

   void gotoPrevFuzzy(int row, ContentState state);
}
