package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface ToggleEditor extends IsWidget, HasText
{

   ViewMode getViewMode();

   void setViewMode(ViewMode viewMode);

   void autoSize();

   void insertTextInCursorPosition(String suggestion);

   void setSaveButtonTitle(String title);

   void addValidationMessagePanel(IsWidget validationMessagePanel);

   int getIndex();

   void showButtons(boolean displayButtons);

   static enum ViewMode
   {
      VIEW, EDIT

   }

}
