package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class OpenEditorEvent extends GwtEvent<OpenEditorEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<OpenEditorEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<OpenEditorEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<OpenEditorEventHandler>();
      }
      return TYPE;
   }

   private int rowIndex;

   public OpenEditorEvent(int rowIndex)
   {
      this.rowIndex = rowIndex;
   }

   public int getRowIndex()
   {
      return rowIndex;
   }

   @Override
   protected void dispatch(OpenEditorEventHandler handler)
   {
      handler.onOpenEditor(this);
   }

   @Override
   public Type<OpenEditorEventHandler> getAssociatedType()
   {
      return getType();
   }

}