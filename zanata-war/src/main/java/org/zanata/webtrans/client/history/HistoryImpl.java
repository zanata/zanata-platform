package org.zanata.webtrans.client.history;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

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
   public HistoryToken getHistoryToken()
   {
      return HistoryToken.fromTokenString(getToken());
   }

   @Override
   public void newItem(String historyToken)
   {
      com.google.gwt.user.client.History.newItem(historyToken);
   }

   @Override
   public void newItem(HistoryToken historyToken)
   {
      newItem(historyToken.toTokenString());
   }

   @Override
   public void newItem(String historyToken, boolean issueEvent)
   {
      com.google.gwt.user.client.History.newItem(historyToken, issueEvent);
   }

   @Override
   public void newItem(HistoryToken historyToken, boolean issueEvent)
   {
      newItem(historyToken.toTokenString(), issueEvent);
   }

}
