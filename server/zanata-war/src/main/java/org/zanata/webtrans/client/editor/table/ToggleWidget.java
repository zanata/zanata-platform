package org.zanata.webtrans.client.editor.table;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface ToggleWidget extends IsWidget, HasText {
    
    ViewMode getViewMode();
    
    void setViewMode(ViewMode viewMode);

    static enum ViewMode {
        VIEW, EDIT

    }
}
