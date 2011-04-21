package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.shared.GwtEvent;

public class TransUnitSelectionEvent extends GwtEvent<TransUnitSelectionHandler>
{

   /**
    * Handler type.
    */
   private static Type<TransUnitSelectionHandler> TYPE;
   private final TransUnit selection;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<TransUnitSelectionHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<TransUnitSelectionHandler>());
   }

   public TransUnitSelectionEvent(TransUnit selection)
   {
      this.selection = selection;
   }

   public TransUnit getSelection()
   {
      return selection;
   }

   @Override
   protected void dispatch(TransUnitSelectionHandler handler)
   {
      handler.onTransUnitSelected(this);
   }

   @Override
   public GwtEvent.Type<TransUnitSelectionHandler> getAssociatedType()
   {
      return getType();
   }

}
