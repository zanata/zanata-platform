package org.zanata.webtrans.client.editor.table;

import org.zanata.webtrans.client.events.TextChangeEvent;
import org.zanata.webtrans.client.events.TextChangeEventHandler;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextArea;

public class AutoSizeTextArea extends TextArea implements HasHandlers
{
   private int initLines;
   private int growLines;
   private SimpleEventBus handlerManager = new SimpleEventBus();


   public AutoSizeTextArea(int init, int grow)
   {
      super();
      sinkEvents(Event.ONPASTE);
      getElement().getStyle().setOverflow(Overflow.HIDDEN);
      getElement().getStyle().setProperty("resize", "none");
      setVisibleLines(initLines);
      initLines = init;
      growLines = grow;
   }

   public void autoSize()
   {
      Log.debug("autosize TextArea");
      int rows = getVisibleLines();

      while (rows > initLines)
      {
         setVisibleLines(--rows);
      }

      while (getElement().getScrollHeight() > getElement().getClientHeight())
      {
         setVisibleLines(getVisibleLines() + growLines);
      }
   }

   @SuppressWarnings("deprecation")
   public void onBrowserEvent(Event event)
   {
      super.onBrowserEvent(event);
      switch (DOM.eventGetType(event))
      {
      case Event.ONPASTE:
         DeferredCommand.addCommand(new Command()
         {
            @Override
            public void execute()
            {
               autoSize();
               handlerManager.fireEvent(new TextChangeEvent(""));
            }

         });
         break;
      }
   }

   @Override
   public void fireEvent(GwtEvent<?> event)
   {
      handlerManager.fireEvent(event);
   }

   public HandlerRegistration addTextChangeEventHandler(TextChangeEventHandler handler)
   {
      return handlerManager.addHandler(TextChangeEvent.getType(), handler);
   }

}
