package org.zanata.webtrans.client.events;

/**
 * @author David Mason, damason@redhat.com
 */
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewHandler;

public class NativeEventImpl implements NativeEvent
{
   public HandlerRegistration addNativePreviewHandler(NativePreviewHandler handler)
   {
      return Event.addNativePreviewHandler(handler);
   }


}
