package org.zanata.webtrans.client.history;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * JRE-safe class that wraps GWT's {@link com.google.gwt.user.client.History} class, allowing non-GWT testing of code that uses gwt's History mechanism.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public interface History
{
   /** @see com.google.gwt.user.client.History#addValueChangeHandler(ValueChangeHandler) */
   public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler);
   /** @see com.google.gwt.user.client.History#back() */
   public void back();
   /** @see com.google.gwt.user.client.History#fireCurrentHistoryState() */
   public void fireCurrentHistoryState();
   /** @see com.google.gwt.user.client.History#forward() */
   public void forward();
   /** @see com.google.gwt.user.client.History#getToken() */
   public String getToken();
   /** @see com.google.gwt.user.client.History#newItem(String) */
   public void newItem(String historyToken);
   /** @see com.google.gwt.user.client.History#newItem(String, boolean) */
   public void newItem(String historyToken, boolean issueEvent);
}
