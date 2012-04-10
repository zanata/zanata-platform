package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface ToggleEditor extends IsWidget, HasText, HasUpdateValidationWarning
{

   ViewMode getViewMode();

   void setViewMode(ViewMode viewMode);

   void autoSize();

   void insertTextInCursorPosition(String suggestion);

   void setSaveButtonTitle(String title);

   void addValidationMessagePanel(IsWidget validationMessagePanel);

   void removeValidationMessagePanel();

   int getIndex();

   void showCopySourceButton(boolean displayButtons);

   void shrinkSize(boolean forceShrink);

   void growSize();

   void setFocus();

   static enum ViewMode
   {
      VIEW, EDIT

   }

   void setTextAndValidate(String text);
}
