package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public interface CheckStateHasChangedHandler extends EventHandler {
    void onCheckStateHasChanged(CheckStateHasChangedEvent event);
}
