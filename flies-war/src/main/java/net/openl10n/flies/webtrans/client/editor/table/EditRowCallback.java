package net.openl10n.flies.webtrans.client.editor.table;

import net.openl10n.flies.common.NavigationType;

public interface EditRowCallback
{
   void gotoNextRow(int row);

   void gotoPrevRow(int row);

   void gotoNextFuzzy(int row, NavigationType state);

   void gotoPrevFuzzy(int row, NavigationType state);
}
