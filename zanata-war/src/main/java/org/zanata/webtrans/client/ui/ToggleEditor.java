package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface ToggleEditor extends IsWidget, HasText, HasUpdateValidationWarning
{

   ViewMode getViewMode();

   void setViewMode(ViewMode viewMode);

   void autoSize();

   void insertTextInCursorPosition(String suggestion);

   void addValidationMessagePanel(IsWidget validationMessagePanel);

   void removeValidationMessagePanel();

   int getIndex();

   void showCopySourceButton(boolean displayButtons);

   void autoSizePlusOne();

   void setFocus();
   
   void addTranslator(String name, String color);

   void clearTranslatorList();

   static enum ViewMode
   {
      VIEW, EDIT

   }

   void setTextAndValidate(String text);

   void removeTranslator(String name, String color);
}
