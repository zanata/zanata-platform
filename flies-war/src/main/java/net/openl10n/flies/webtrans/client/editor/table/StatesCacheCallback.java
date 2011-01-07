package net.openl10n.flies.webtrans.client.editor.table;

import net.openl10n.flies.common.NavigationType;

public interface StatesCacheCallback
{
   void nextFuzzy(NavigationType state);

   void prevFuzzy(NavigationType state);
}
