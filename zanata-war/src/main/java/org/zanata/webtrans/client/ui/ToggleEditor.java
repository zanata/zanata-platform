package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface ToggleEditor extends IsWidget, HasText,
        HasUpdateValidationMessage {

    ViewMode getViewMode();

    void setViewMode(ViewMode viewMode);

    void insertTextInCursorPosition(String suggestion);

    int getIndex();

    void showCopySourceButton(boolean displayButtons);

    void setFocus();

    void addTranslator(String name, String color);

    void clearTranslatorList();

    void highlightSearch(String findMessage);

    void refresh();

    void setEnableSpellCheck(Boolean enabled);

    TransUnitId getId();

    static enum ViewMode {
        VIEW, EDIT

    }

    void setTextAndValidate(String text);

    void removeTranslator(String name, String color);

    boolean isFocused();
}
