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
   public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler);

   public void back();

   public void fireCurrentHistoryState();

   public void forward();

   public String getToken();

   public void newItem(String historyToken);

   public void newItem(String historyToken, boolean issueEvent);
}
