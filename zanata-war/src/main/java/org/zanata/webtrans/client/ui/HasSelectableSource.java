package org.zanata.webtrans.client.ui;

import com.google.gwt.event.dom.client.HasClickHandlers;

public interface HasSelectableSource extends HasClickHandlers
{
   String getSource();

   void setSelected(boolean selected);

   void refresh();
}
