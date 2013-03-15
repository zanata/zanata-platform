package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class InitEditorEvent extends GwtEvent<InitEditorEventHandler>
{
   public static Type<InitEditorEventHandler> TYPE = new Type<InitEditorEventHandler>();

   public Type<InitEditorEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(InitEditorEventHandler handler)
   {
      handler.onInitEditor(this);
   }
}
