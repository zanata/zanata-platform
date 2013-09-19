package org.zanata.webtrans.client.ui;

import com.allen_sauer.gwt.log.client.*;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CodeMirrorEditor extends Composite implements TextAreaWrapper
{
   private static CodeMirrorEditorUiBinder ourUiBinder = GWT.create(CodeMirrorEditorUiBinder.class);

   @UiField
   TextAreaElement textArea;

   private JavaScriptObject codeMirror;
   private boolean valueChangeHandlerInitialized;
   private boolean editing;
   private final Command onFocusCallback;


   public CodeMirrorEditor(Command onFocusCallback)
   {
      this.onFocusCallback = onFocusCallback;
      initWidget(ourUiBinder.createAndBindUi(this));
   }

   // see http://codemirror.net/doc/manual.html#usage
   private native JavaScriptObject initCodeMirror(TextAreaElement element) /*-{
      var self = this;

      var codeMirrorEditor = $wnd.CodeMirror.fromTextArea(element, {
         lineNumbers: true,
         lineWrapping: true,
         disableSpellcheck: false,
         mode: "visibleSpace",
         value: element.value
      });
      codeMirrorEditor.on("focus", function() {
           self.@org.zanata.webtrans.client.ui.CodeMirrorEditor::onFocus()();
      });

      codeMirrorEditor.on("blur", function() {
           self.@org.zanata.webtrans.client.ui.CodeMirrorEditor::onBlur()();
      });

      codeMirrorEditor.on("change", function() {
           self.@org.zanata.webtrans.client.ui.CodeMirrorEditor::onChange()();
      });
      return codeMirrorEditor;

   }-*/;

   @Override
   public void setText(String text)
   {
      if (codeMirror == null)
      {
         textArea.setValue(text);
         codeMirror = initCodeMirror(textArea);
      }
      setCodeMirrorContent(text);
   }

   @Override
   public String getText()
   {
      return getCodeMirrorContent();
   }

   @Override
   protected void onLoad()
   {
      super.onLoad();
      if (codeMirror == null)
      {
         codeMirror = initCodeMirror(textArea);
      }
   }

   // callback function for the code mirror instance. Gets called when code mirror editor is on focus.
   private void onFocus()
   {
      if (!editing)
      {
         editing = true;
         // this is to ensure row selection (on right click)
         onFocusCallback.execute();
      }
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

   private native String getCodeMirrorContent() /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;
      return editor.getValue();
   }-*/;

   private native void setCodeMirrorContent(String text) /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;
      if (editor)
      {
         editor.setValue(text);
      }
   }-*/;

   private native void focusEditor() /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;
      editor.focus();
   }-*/;

   @Override
   public void setReadOnly(boolean readOnly)
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

   private native void setEditorOption(String option, String value) /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;
      if (editor)
      {
         editor.setOption(option, value);
      }
   }-*/;

   private native String getEditorOption(String option, String defaultValue) /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;
      if (editor)
      {
         return '' + editor.getOption(option);
      }
      return defaultValue;
   }-*/;

   @Override
   public boolean isReadOnly()
   {
      return Boolean.parseBoolean(getEditorOption("readOnly", "false"));
   }

   @Override
   public int getCursorPos()
   {
      return getCodeMirrorCursorPos();
   }

   private native int getCodeMirrorCursorPos() /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;
      var pos = editor.getCursor();
      return editor.indexFromPos(pos);
   }-*/;

   @Override
   public void setCursorPos(int pos)
   {
      setCodeMirrorCursorPos(pos);
   }

   private native void setCodeMirrorCursorPos(int cursorIndex) /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;
      var pos = editor.posFromIndex(cursorIndex);
      editor.setCursor(pos);
   }-*/;

   @Override
   public void highlight(String term)
   {
      if (!Strings.isNullOrEmpty(term))
      {
         codeMirrorHighlight(term);
      }
   }

   private native void codeMirrorHighlight(String term) /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;
      var searchCursor = editor.getSearchCursor(term, {line: 0, ch: 0}, true);
      while(searchCursor.findNext())
      {
         editor.markText(searchCursor.from(), searchCursor.to(), "CodeMirror-searching");
      }
   }-*/;

   @Override
   public void refresh()
   {
      refreshCodeMirror();
   }

   private native void refreshCodeMirror() /*-{
      var editor = this.@org.zanata.webtrans.client.ui.CodeMirrorEditor::codeMirror;

      if (editor)
      {
         editor.refresh();
      }
   }-*/;

   @Override
   public HandlerRegistration addChangeHandler(ChangeHandler handler) {
      return addDomHandler(handler, ChangeEvent.getType());
   }

   @Override
   public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
      // Initialization code
      if (!valueChangeHandlerInitialized) {
         valueChangeHandlerInitialized = true;
         addChangeHandler(new ChangeHandler()
         {
            public void onChange(ChangeEvent event)
            {
               ValueChangeEvent.fire(CodeMirrorEditor.this, getText());
            }
         });
      }
      return addHandler(handler, ValueChangeEvent.getType());
   }

   @Override
   public void setEditing(boolean isEditing)
   {
      // if set for editing and is not already editing, we want to focus code mirror editor
      if (isEditing && !editing)
      {
         focusEditor();
      }
      editing = isEditing;
   }

   @Override
   public boolean isEditing()
   {
      return editing;
   }

   @Override
   public HandlerRegistration addBlurHandler(BlurHandler handler)
   {
      return addHandler(handler, BlurEvent.getType());
   }

   @Override
   public HandlerRegistration addFocusHandler(FocusHandler handler)
   {
      return addHandler(handler, FocusEvent.getType());
   }

   interface CodeMirrorEditorUiBinder extends UiBinder<Widget, CodeMirrorEditor>
   {
   }
}