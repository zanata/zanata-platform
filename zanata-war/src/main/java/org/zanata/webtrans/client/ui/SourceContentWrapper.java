package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface SourceContentWrapper extends HasText, IsWidget
{
   void setTitle(String title);

   void refresh();

   void highlight(String searchTerm);
}
