package net.openl10n.flies.webtrans.client.editor.table;

import net.openl10n.flies.common.ContentState;

public interface StatesCacheCallback
{
   void nextFuzzy(ContentState state);

   void prevFuzzy(ContentState state);
}
