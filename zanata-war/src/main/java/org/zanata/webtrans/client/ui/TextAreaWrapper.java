package org.zanata.webtrans.client.ui;

import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface TextAreaWrapper extends IsWidget, HasText, HasValueChangeHandlers<String>, HasBlurHandlers, HasChangeHandlers, HasFocusHandlers
{
   void setFocus(boolean focused);

   void setReadOnly(boolean readOnly);

   boolean isReadOnly();

   int getCursorPos();

   void setCursorPos(int pos);

   void highlight(String term);

   void refresh();

   void setEditing(boolean isEditing);

   boolean isEditing();
}
