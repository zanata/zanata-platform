package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface CommentChangedEventHandler extends EventHandler {
    void onCommentChanged(CommentChangedEvent event);
}
