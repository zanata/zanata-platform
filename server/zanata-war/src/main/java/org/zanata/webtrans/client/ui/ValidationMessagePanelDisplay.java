package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

public interface ValidationMessagePanelDisplay extends IsWidget
{
   void clear();

   void setContent(List<String> errors);
}
