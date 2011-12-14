package org.zanata.webtrans.client.history;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

//TODO inject this

/**
 * @author David Mason, damason@redhat.com
 * 
 */
public class HistoryImpl implements History
{

   @Override
   public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler)
   {
      return com.google.gwt.user.client.History.addValueChangeHandler(handler);
   }

   @Override
   public void back()
   {
      com.google.gwt.user.client.History.back();
   }

   @Override
   public void fireCurrentHistoryState()
   {
      com.google.gwt.user.client.History.fireCurrentHistoryState();
   }

   @Override
   public void forward()
   {
      com.google.gwt.user.client.History.forward();
   }

   @Override
   public String getToken()
   {
      return com.google.gwt.user.client.History.getToken();
   }

   @Override
   public void newItem(String historyToken)
   {
      com.google.gwt.user.client.History.newItem(historyToken);
   }

   @Override
   public void newItem(String historyToken, boolean issueEvent)
   {
      com.google.gwt.user.client.History.newItem(historyToken, issueEvent);
   }

}
