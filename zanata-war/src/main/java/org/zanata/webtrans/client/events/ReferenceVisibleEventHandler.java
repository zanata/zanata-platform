package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface ReferenceVisibleEventHandler extends EventHandler
{
   void onShowHideReference(ReferenceVisibleEvent event);
}
