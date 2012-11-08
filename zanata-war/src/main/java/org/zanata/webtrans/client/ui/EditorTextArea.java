/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextArea;

public class EditorTextArea extends TextArea implements TextAreaWrapper
{
   private static final int INITIAL_LINE_NUMBER = 2;
   private static final int CHECK_INTERVAL = 50;
   // this timer is used to fire validation and change editing state in a 50 millisecond interval
   // @see setEditing(boolean)
   private final Timer typingTimer = new Timer()
   {
      @Override
      public void run()
      {
         ValueChangeEvent.fire(EditorTextArea.this, getText());
      }
   };
   private boolean editing;

   public EditorTextArea()
   {
      super();

      addKeyDownHandler(new KeyDownHandler()
      {
         @Override
         public void onKeyDown(KeyDownEvent keyDownEvent)
         {
            autoSize();
            if (keyDownEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER)
            {
               setVisibleLines(getVisibleLines() + 1);
            }
         }
      });
   }

   private void autoSize()
   {
      setVisibleLines(INITIAL_LINE_NUMBER);
      while (getElement().getScrollHeight() > getElement().getClientHeight())
      {
         setVisibleLines(getVisibleLines() + 1);
      }
   }

   @Override
   public void setText(String text)
   {
      super.setText(text);
      Splitter splitter = Splitter.on("\n");
      Iterable<String> lines = splitter.split(text);
      setVisibleLines(Iterables.size(lines));
   }

   @Override
   public void highlight(String term)
   {
      // plain textarea won't support highlight
   }

   @Override
   public void refresh()
   {
      // plain textarea doesn't need refresh
   }

   @Override
   public void setEditing(boolean isEditing)
   {
      editing = isEditing;
      if (isEditing)
      {
         typingTimer.scheduleRepeating(CHECK_INTERVAL);
      }
      else
      {
         typingTimer.cancel();
      }
   }

   @Override
   public boolean isEditing()
   {
      return editing;
   }
}
