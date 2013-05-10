package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface AliasKeyChangedEventHandler extends EventHandler
{
   void onAliasKeyChanged(AliasKeyChangedEvent event);
}