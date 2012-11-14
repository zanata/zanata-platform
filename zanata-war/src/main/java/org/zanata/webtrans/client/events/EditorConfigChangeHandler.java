package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface EditorConfigChangeHandler extends EventHandler
{
   void onUserConfigChanged(EditorConfigChangeEvent event);
}