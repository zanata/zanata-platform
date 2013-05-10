package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface ReviewModeChangeEventHandler extends EventHandler
{
   void onReviewModeChange(ReviewModeChangeEvent event);
}
