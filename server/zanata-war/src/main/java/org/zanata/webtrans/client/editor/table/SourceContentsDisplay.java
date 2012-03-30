package org.zanata.webtrans.client.editor.table;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;
import org.zanata.webtrans.shared.model.TransUnit;

import java.util.List;

public interface SourceContentsDisplay extends IsWidget
{
   void setValue(TransUnit value);

   List<HasClickHandlers> getSourcePanelList();

   void setValue(TransUnit value, boolean fireEvents);

   void highlightSearch(String search);
}
