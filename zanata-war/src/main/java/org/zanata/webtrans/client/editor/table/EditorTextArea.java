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

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.TextArea;

public class EditorTextArea extends TextArea
{
   private boolean useCodeMirrorFlag = true;

   private JavaScriptObject codeMirrorEditor;

   public EditorTextArea(boolean isUseCodeMirror)
   {
      super();
      useCodeMirrorFlag = isUseCodeMirror;
   }

   // see http://codemirror.net/doc/manual.html#usage
   public native JavaScriptObject initCodeMirror(Element element) /*-{
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
      return useCodeMirrorFlag ? getCodeMirrorContent() : super.getText();
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
      return useCodeMirrorFlag ? getCodeMirrorContent() : super.getValue();
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
   public void setReadOnly(boolean readOnly)
   {
      if (useCodeMirrorFlag)
      {
         if (readOnly)
         {
            setEditorOption("readOnly", "nocursor");
         }
         else
         {
            setEditorOption("readOnly", "false");
         }
      }
      super.setReadOnly(readOnly);
   }

   private native void setEditorOption(String option, String value) /*-{
      var editor = this.@org.zanata.webtrans.client.editor.table.EditorTextArea::codeMirrorEditor;
      if (editor)
      {
         editor.setOption(option, value);
      }
   }-*/;

   private native String getEditorOption(String option, String defaultValue) /*-{
      var editor = this.@org.zanata.webtrans.client.editor.table.EditorTextArea::codeMirrorEditor;
      if (editor)
      {
         return '' + editor.getOption(option);
      }
      return defaultValue;
   }-*/;

   @Override
   public boolean isReadOnly()
   {
      return useCodeMirrorFlag ? Boolean.parseBoolean(getEditorOption("readOnly", "false")) : super.isReadOnly();
   }

   @Override
   public int getCursorPos()
   {
      return useCodeMirrorFlag ? getCodeMirrorCursorPos() : super.getCursorPos();
   }

   private native int getCodeMirrorCursorPos() /*-{
      var editor = this.@org.zanata.webtrans.client.editor.table.EditorTextArea::codeMirrorEditor;
      var pos = editor.getCursor();
      return editor.indexFromPos(pos);
   }-*/;

   @Override
   public void setCursorPos(int pos)
   {
      if (useCodeMirrorFlag)
      {
         setCodeMirrorCursorPos(pos);
      }
      super.setCursorPos(pos);
   }

   private native void setCodeMirrorCursorPos(int cursorIndex) /*-{
      var editor = this.@org.zanata.webtrans.client.editor.table.EditorTextArea::codeMirrorEditor;
      var pos = editor.posFromIndex(cursorIndex);
      editor.setCursor(pos);
   }-*/;

   public void highlight(String term)
   {
      if (useCodeMirrorFlag && !Strings.isNullOrEmpty(term))
      {
         codeMirrorHighlight(term);
      }
   }

   private native void codeMirrorHighlight(String term) /*-{
      var editor = this.@org.zanata.webtrans.client.editor.table.EditorTextArea::codeMirrorEditor;
      var searchCursor = editor.getSearchCursor(term, {line: 0, ch: 0}, true);
      while(searchCursor.findNext())
      {
         editor.markText(searchCursor.from(), searchCursor.to(), "CodeMirror-searching");
      }
   }-*/;
}
