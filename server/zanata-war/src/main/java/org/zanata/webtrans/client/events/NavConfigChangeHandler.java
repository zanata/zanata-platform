package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface NavConfigChangeHandler extends EventHandler
{
   void onValueChanged(NavConfigChangeEvent event);
}