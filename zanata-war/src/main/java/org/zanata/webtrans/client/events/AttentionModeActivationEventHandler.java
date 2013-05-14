package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface AttentionModeActivationEventHandler extends EventHandler
{
   void onAttentionModeActivationChanged(AttentionModeActivationEvent event);
}