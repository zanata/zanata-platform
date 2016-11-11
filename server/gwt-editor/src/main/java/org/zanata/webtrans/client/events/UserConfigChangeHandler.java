package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface UserConfigChangeHandler extends EventHandler {
    void onUserConfigChanged(UserConfigChangeEvent event);
}
