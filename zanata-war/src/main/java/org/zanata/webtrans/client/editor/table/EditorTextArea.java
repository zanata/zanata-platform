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
package org.zanata.webtrans.client.editor.table;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextArea;

public class EditorTextArea extends TextArea
{
   // TODO this should go into UserConfigHolder and be part of user config
   private boolean useCodeMirrorFlag = true;

   private JavaScriptObject codeMirrorEditor;

   public EditorTextArea()
   {
      super();
      sinkEvents(Event.ONPASTE);
   }

   public native JavaScriptObject initCodeMirror(Element element) /*-{
      // TODO add onChange, event handler into codemirror editor
      // see http://codemirror.net/doc/manual.html#usage

      var self = this;
      var codeMirrorEditor = $wnd.CodeMirror.fromTextArea(element, {
         lineNumbers: true,
         lineWrapping: true,
         mode: "htmlmixed",
         value: element.value,
         onFocus: function() {
            self.@org.zanata.webtrans.client.editor.table.EditorTextArea::onFocus()();
         },
         onBlur: function() {
            self.@org.zanata.webtrans.client.editor.table.EditorTextArea::onBlur()();
         },
         onChange: function() {
            self.@org.zanata.webtrans.client.editor.table.EditorTextArea::onChange()();
         }

      });

      return codeMirrorEditor;

   }-*/;


   @Override
   public void setText(String text)
   {
      super.setText(text);
      if (useCodeMirrorFlag)
      {
         setCodeMirrorContent(text);
      }
   }

   @Override
   public String getText()
   {
      if (useCodeMirrorFlag)
      {
         return getCodeMirrorContent();
      }
      return super.getText();
   }

   @Override
   protected void onLoad()
   {
      super.onLoad();
      if (useCodeMirrorFlag)
      {
         codeMirrorEditor = initCodeMirror(getElement());
      }
   }

   // callback function for the code mirror instance. Gets called when code mirror editor is on focus.
   private void onFocus()
   {
      NativeEvent focusEvent = Document.get().createFocusEvent();
      FocusEvent.fireNativeEvent(focusEvent, this, this.getElement());
   }

   // callback function for the code mirror instance. Gets called when code mirror editor is on blur.
   private void onBlur()
   {
      NativeEvent blurEvent = Document.get().createBlurEvent();
      BlurEvent.fireNativeEvent(blurEvent, this, this.getElement());
   }

   // callback function for the code mirror instance. Gets called when code mirror editor content has changed.
   private void onChange()
   {
      ValueChangeEvent.fire(this, getCodeMirrorContent());
   }

   @Override
   public String getValue()
   {
      if (useCodeMirrorFlag)
      {
         return getCodeMirrorContent();
      }
      return super.getValue();
   }

   @Override
   public void setValue(String value, boolean fireEvents)
   {
      if (useCodeMirrorFlag)
      {
         setCodeMirrorContent(value);
      }
      super.setValue(value, fireEvents);
   }

   private native String getCodeMirrorContent() /*-{
      var editor = this.@org.zanata.webtrans.client.editor.table.EditorTextArea::codeMirrorEditor;
      return editor.getValue();
   }-*/;

   private native void setCodeMirrorContent(String text) /*-{
      var editor = this.@org.zanata.webtrans.client.editor.table.EditorTextArea::codeMirrorEditor;
      if (editor)
      {
         editor.setValue(text);
      }
   }-*/;

   @Override
   public void setFocus(boolean focused)
   {
      if (focused && useCodeMirrorFlag)
      {
         focusEditor();
      }
      else
      {
         super.setFocus(focused);
      }
   }

   private native void focusEditor() /*-{
      var editor = this.@org.zanata.webtrans.client.editor.table.EditorTextArea::codeMirrorEditor;
      editor.focus();
   }-*/;

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

