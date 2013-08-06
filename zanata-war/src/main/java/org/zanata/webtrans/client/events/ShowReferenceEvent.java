package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;
import org.zanata.webtrans.shared.model.Locale;

public class ShowReferenceEvent extends GwtEvent<ShowReferenceEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<ShowReferenceEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ShowReferenceEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<ShowReferenceEventHandler>();
      }
      return TYPE;
   }
   
   private Locale selectedLocale;

   public ShowReferenceEvent(Locale selectedLocale)
   {
       this.selectedLocale = selectedLocale;
   }
   
   public Locale getSelectedLocale() {
        return selectedLocale;
    }

   @Override
   public Type<ShowReferenceEventHandler> getAssociatedType()
   {
      return getType();
   }


   @Override
   protected void dispatch(ShowReferenceEventHandler handler)
   {
      handler.onShowReference(this);
   }

   
}