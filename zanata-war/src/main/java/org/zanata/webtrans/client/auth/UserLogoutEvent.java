package org.zanata.webtrans.client.auth;

import com.google.gwt.event.shared.GwtEvent;

public class UserLogoutEvent extends GwtEvent<UserLogoutEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<UserLogoutEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<UserLogoutEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<UserLogoutEventHandler>();
      }
      return TYPE;
   }

   @Override
   protected void dispatch(UserLogoutEventHandler handler)
   {
      handler.onUserLogout(this);
   }

   @Override
   public Type<UserLogoutEventHandler> getAssociatedType()
   {
      return TYPE;
   }

}
