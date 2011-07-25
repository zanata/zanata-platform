package org.zanata.webtrans.client.editor.table;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextArea;

public class EditorTextArea extends TextArea
{
   private Timer timer;

   public EditorTextArea()
   {
      super();
      sinkEvents(Event.ONPASTE);

   }

   public void addTimer(String initValue)
   {
      final String initContent = initValue;
      timer = new Timer()
      {
         
         public void run()
         {
            String content = getText();
            Log.debug(initContent);
            Log.debug(content);
            if (!content.equals(initContent))
               ValueChangeEvent.fire(EditorTextArea.this, content);
         }
      };

      // Schedule the timer to run once in 1 seconds.
      timer.scheduleRepeating(1000);
   }

   public void stopTimer()
   {
      if (timer != null)
         timer.cancel();
   }

   @Override
   public void onBrowserEvent(Event event)
   {
      super.onBrowserEvent(event);
      switch (DOM.eventGetType(event)) {
      case Event.ONPASTE:
         Scheduler.get().scheduleDeferred(new ScheduledCommand()
         {
                @Override
                  public void execute() {
                      ValueChangeEvent.fire(EditorTextArea.this, getText());
                  }
         });
         break;
      }
   }

}

