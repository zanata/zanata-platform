package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.presenter.MainView;

import com.google.gwt.event.shared.GwtEvent;

public class RunDocValidationEvent extends GwtEvent<RunDocValidationEventHandler>
{
   private final MainView view;

   public RunDocValidationEvent(MainView view)
   {
      this.view = view;
   }

   /**
    * Handler type.
    */
   private static Type<RunDocValidationEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<RunDocValidationEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<RunDocValidationEventHandler>();
      }
      return TYPE;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<RunDocValidationEventHandler> getAssociatedType()
   {
      return getType();
   }

   public MainView getView()
   {
      return view;
   }

   @Override
   protected void dispatch(RunDocValidationEventHandler handler)
   {
      handler.onRunDocValidation(this);
   }
}
