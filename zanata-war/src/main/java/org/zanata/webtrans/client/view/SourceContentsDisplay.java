package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.shared.model.HasTransUnitId;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.dom.client.ClickHandler;
import org.zanata.webtrans.shared.model.TextFlowTarget;

public interface SourceContentsDisplay extends HasTransUnitId, NeedsRefresh
{
   void setValue(TransUnit value);

   List<HasSelectableSource> getSourcePanelList();

   void setValue(TransUnit value, boolean fireEvents);

   void highlightSearch(String search);

   void setSourceSelectionHandler(ClickHandler clickHandler);

   void toggleTransUnitDetails(boolean showTransUnitDetails);

   void updateTransUnitDetails(TransUnit transUnit);

   void showReference(TextFlowTarget reference);

   void hideReference();
}
