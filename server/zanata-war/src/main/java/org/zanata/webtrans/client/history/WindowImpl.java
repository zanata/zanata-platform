package org.zanata.webtrans.client.history;

import com.google.gwt.user.client.Window;

public class WindowImpl implements org.zanata.webtrans.client.history.Window
{
   @Override
   public void setTitle(String title)
   {
      Window.setTitle(title);
   }
}
