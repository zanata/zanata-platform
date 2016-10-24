package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.shared.model.HasTransUnitId;
import com.google.gwt.event.dom.client.HasClickHandlers;

public interface HasSelectableSource extends HasClickHandlers, HasTransUnitId {
    String getSource();

    void setSelected(boolean selected);

    void refresh();

    void clickSelf();
}
