package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface TextChangeEventHandler extends EventHandler
{
   void onTextChange(TextChangeEvent event);
}
