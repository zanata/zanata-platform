package org.zanata.webtrans.client.ui;

import static org.zanata.webtrans.client.view.TargetContentsDisplay.EditingState.UNSAVED;

import java.util.List;

import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class Editor extends Composite implements ToggleEditor
{
   private static EditorUiBinder uiBinder = GWT.create(EditorUiBinder.class);

   private final int index;

   private final TransUnitId id;

   private TargetContentsDisplay.Listener listener;

   @UiField
   Styles style;

   @UiField
   FocusPanel rootContainer;

   @UiField
   HorizontalPanel topContainer, textAreaTable;

   @UiField
   TranslatorListWidget translatorList;

   @UiField
   InlineLabel copyIcon;
   
   @UiField
   HTMLPanel targetWrapper;

   @UiField(provided = true)
   TextAreaWrapper textArea;

   public Editor(String displayString, final int index, final TargetContentsDisplay.Listener listener, final TransUnitId id)
   {
      this.listener = listener;
      this.index = index;
      this.id = id;
      if (listener.getConfigState().isUseCodeMirrorEditor())
      {
         Command onCodeMirrorFocusCallback = new Command()
         {

            @Override
            public void execute()
            {
               listener.onEditorClicked(id, index);
            }
         };
         textArea = new CodeMirrorEditor(onCodeMirrorFocusCallback);
      }
      else
      {
         textArea = new EditorTextArea(displayString);
      }

      initWidget(uiBinder.createAndBindUi(this));
      // determine whether to show or hide buttons
      showCopySourceButton(listener.isDisplayButtons());

      if (!listener.isReadOnly())
      {
         setViewMode(ViewMode.EDIT);
      }
      setText(displayString);
   }

   @Override
   public void setEnableSpellCheck(Boolean enabled)
   {
      targetWrapper.getElement().setAttribute("contenteditable", enabled.toString());
      targetWrapper.getElement().setAttribute("spellcheck", enabled.toString());
   }

   private void fireValidationEvent()
   {
      if (getViewMode() == ViewMode.EDIT)
      {
         listener.validate(this);
      }
   }

   @UiHandler("textArea")
   public void onValueChange(ValueChangeEvent<String> event)
   {
      fireValidationEvent();
      listener.setEditingState(id, UNSAVED);
   }

   @UiHandler("textArea")
   public void onTextAreaFocus(FocusEvent event)
   {
      listener.onEditorClicked(id, index);
      textArea.setEditing(true);
      fireValidationEvent();
   }

   @UiHandler("textArea")
   public void onTextAreaBlur(BlurEvent event)
   {
      textArea.setEditing(false);
   }

   @UiHandler("copyIcon")
   public void onCopySource(ClickEvent event)
   {
      listener.copySource(this, id);
   }

   @Override
   public ViewMode getViewMode()
   {
      if (textArea.isReadOnly())
      {
         return ViewMode.VIEW;
      }
      else
      {
         return ViewMode.EDIT;
      }
   }

   @Override
   public void setViewMode(ViewMode viewMode)
   {
      textArea.setReadOnly(viewMode == ViewMode.VIEW);
      translatorList.setVisible(viewMode == ViewMode.EDIT);
      toggleTranslatorList();
   }

   public void toggleTranslatorList()
   {
      if (translatorList.isVisible() && !translatorList.isEmpty())
      {
         textAreaTable.setCellWidth(translatorList, "60px");
      }
      else
      {
         textAreaTable.setCellWidth(translatorList, "0");
      }
   }

   @Override
   public void setTextAndValidate(String text)
   {
      setText(text);
      fireValidationEvent();
   }

   @Override
   public void setText(String text)
   {
      if (!Strings.isNullOrEmpty(text))
      {
         textArea.setText(text);
      }
      else
      {
         textArea.setText("");
      }
   }

   @Override
   public String getText()
   {
      return textArea.getText();
   }

   @Override
   public void setFocus()
   {
      textArea.setEditing(true);
   }

   @Override
   public void insertTextInCursorPosition(String suggestion)
   {
      String preCursor = textArea.getText().substring(0, textArea.getCursorPos());
      String postCursor = textArea.getText().substring(textArea.getCursorPos(), textArea.getText().length());

      textArea.setText(preCursor + suggestion + postCursor);
      textArea.setCursorPos(textArea.getText().indexOf(suggestion) + suggestion.length());
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this)
            .add("id", id)
//            .add("label", label.getText())
//            .add("textArea", textArea.getText())
            .add("isFocused", isFocused())
            .toString();
      // @formatter:on
   }

   @Override
   public int getIndex()
   {
      return index;
   }

   @Override
   public void showCopySourceButton(boolean displayButtons)
   {
      copyIcon.setVisible(displayButtons);
   }

   @Override
   public void updateValidationWarning(List<String> errors)
   {
      if (!errors.isEmpty())
      {
         targetWrapper.addStyleName(style.hasValidationError());
      }
      else
      {
         targetWrapper.removeStyleName(style.hasValidationError());
      }
   }

   @Override
   public void addTranslator(String name, String color)
   {
      translatorList.addTranslator(name, color);
      toggleTranslatorList();
   }

   @Override
   public void clearTranslatorList()
   {
      translatorList.clearTranslatorList();
      toggleTranslatorList();
   }

   @Override
   public void highlightSearch(String findMessage)
   {
      textArea.highlight(findMessage);
   }

   @Override
   public void refresh()
   {
      textArea.refresh();
   }

   @Override
   public void removeTranslator(String name, String color)
   {
      translatorList.removeTranslator(name, color);
      toggleTranslatorList();
   }

   @Override
   public boolean isFocused()
   {
      return textArea.isEditing();
   }

   interface EditorUiBinder extends UiBinder<Widget, Editor>
   {
   }

   interface Styles extends CssResource
   {

      String rootContainer();

      String hasValidationError();

      String copyButton();

      String targetContainer();
   }
}
