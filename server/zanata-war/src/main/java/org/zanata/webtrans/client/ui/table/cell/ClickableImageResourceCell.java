package org.zanata.webtrans.client.ui.table.cell;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;

public class ClickableImageResourceCell extends ImageResourceCell
{
   public ClickableImageResourceCell()
   {
      super();
   }

   @Override
   public Set<String> getConsumedEvents()
   {
      Set<String> consumedEvents = new HashSet<String>();
      consumedEvents.add("click");
      return consumedEvents;
   }

   @Override
   public void onBrowserEvent(Context context, Element parent, ImageResource value, NativeEvent event, ValueUpdater<ImageResource> valueUpdater)
   {
      String eventType = event.getType();
      if ("click".equals(eventType))
      {
         onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
   }

   @Override
   protected void onEnterKeyDown(Context context, Element parent, ImageResource value, NativeEvent event, ValueUpdater<ImageResource> valueUpdater)
   {
      if (valueUpdater != null)
      {
         valueUpdater.update(value);
      }
   }
}
