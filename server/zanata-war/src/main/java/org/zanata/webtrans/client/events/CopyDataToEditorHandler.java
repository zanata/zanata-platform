package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface CopyDataToEditorHandler extends EventHandler
{
   void onTransMemoryCopy(CopyDataToEditorEvent event);
}