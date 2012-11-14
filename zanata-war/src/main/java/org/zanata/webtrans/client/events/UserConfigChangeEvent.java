package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.presenter.MainView;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class UserConfigChangeEvent extends GwtEvent<UserConfigChangeHandler>
{
   public static Type<UserConfigChangeHandler> TYPE = new Type<UserConfigChangeHandler>();

   private final MainView view;

   public UserConfigChangeEvent(MainView view)
   {
      this.view = view;
   }

   public MainView getView()
   {
      return view;
   }

   @Override
   protected void dispatch(UserConfigChangeHandler handler)
   {
      handler.onUserConfigChanged(this);
   }

   @Override
   public Type<UserConfigChangeHandler> getAssociatedType()
   {
      return TYPE;
   }

}