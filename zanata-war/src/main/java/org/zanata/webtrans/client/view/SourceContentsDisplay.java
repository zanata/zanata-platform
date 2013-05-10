package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.shared.model.HasTransUnitId;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface SourceContentsDisplay extends IsWidget, HasTransUnitId
{
   void setValue(TransUnit value);

   List<HasSelectableSource> getSourcePanelList();

   void setValue(TransUnit value, boolean fireEvents);

   void highlightSearch(String search);

   void setSourceSelectionHandler(ClickHandler clickHandler);

   void refresh();

   void toggleTransUnitDetails(boolean showTransUnitDetails);

   void updateTransUnitDetails(TransUnit transUnit);
}
