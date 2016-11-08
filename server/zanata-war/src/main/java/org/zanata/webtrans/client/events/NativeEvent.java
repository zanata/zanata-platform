/**
 *
 */
package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event.NativePreviewHandler;

/**
 * A wrapper around {@link com.google.gwt.user.client.Event} to allow mocking of
 * static method calls for testing.
 *
 * This implementation does not include all methods of Event (add them as
 * required).
 *
 * @author David Mason, damason@redhat.com
 *
 */
public interface NativeEvent {
    public HandlerRegistration addNativePreviewHandler(
            NativePreviewHandler handler);

}
