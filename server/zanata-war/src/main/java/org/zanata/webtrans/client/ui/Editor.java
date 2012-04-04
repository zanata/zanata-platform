package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.client.editor.table.EditorTextArea;
import org.zanata.webtrans.client.editor.table.TableResources;
import org.zanata.webtrans.client.editor.table.TargetContentsDisplay;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.resources.NavigationMessages;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class Editor extends Composite implements ToggleEditor, HasUpdateValidationWarning
{
   private String findMessage;
   private TargetContentsDisplay.Listener listener;

   interface EditorUiBinder extends UiBinder<Widget, Editor>
   {
   }

   private static EditorUiBinder uiBinder = GWT.create(EditorUiBinder.class);

   private static final int INITIAL_LINES = 3;
   private static final int HEIGHT_PER_LINE = 16;

   private static final int TYPING_TIMER_INTERVAL = 500; // ms
   private static final int TYPING_TIMER_RECURRENT_VALIDATION_PERIOD = 5; // intervals

   private final int index;

   @UiField
   FocusPanel rootContainer;

   @UiField
   HorizontalPanel topContainer;

   @UiField
   HorizontalPanel bottomContainer;

   @UiField
   FlowPanel validationMessagePanelContainer;

   @UiField
   HorizontalPanel buttons;

   @UiField
   PushButton validateButton, saveButton, fuzzyButton, cancelButton;

   @UiField
   TableResources images;

   @UiField
   NavigationMessages messages;

   @UiField
   EditorTextArea textArea;

   @UiField
   HighlightingLabel label;

   @UiField
   PushButton copySourceButton;

   private boolean keypressed;
   private boolean typing;
   private int typingCycles;

   private final Timer typingTimer = new Timer()
   {
      @Override
      public void run()
      {
         if (keypressed)
         {
            // still typing, validate periodically
            keypressed = false;
            typingCycles++;
            if (typingCycles % TYPING_TIMER_RECURRENT_VALIDATION_PERIOD == 0)
            {
               fireValidationEvent();
            }
         }
         else
         {
            // finished, validate immediately
            this.cancel();
            typing = false;
            fireValidationEvent();
         }
      }
   };

   public Editor(String displayString, String findMessage, int index, final TargetContentsDisplay.Listener listener)
   {
      this.findMessage = findMessage;
      this.listener = listener;
      this.index = index;
      initWidget(uiBinder.createAndBindUi(this));

      // determine whether to show or hide buttons
      showButtons(listener.isDisplayButtons());

      setLabelText(displayString);

      label.setTitle(messages.clickHere());
      textArea.setVisible(false);
   }

   private void setLabelText(String displayString)
   {
      if (Strings.isNullOrEmpty(displayString))
      {
         label.setText(messages.clickHere());
         label.setStylePrimaryName("TableEditorContent-Empty");
      }
      else
      {
         label.setText(displayString);
         label.setStylePrimaryName("TableEditorContent");
      }

      if (!Strings.isNullOrEmpty(findMessage))
      {
         label.highlightSearch(findMessage);
      }
   }

   private void fireValidationEvent()
   {
      listener.validate(this);
   }

   @UiHandler("rootContainer")
   public void onKeyDown(KeyDownEvent event)
   {
      listener.onEditorKeyDown(event, this);
   }

   @UiHandler("textArea")
   public void onValueChange(ValueChangeEvent<String> event)
   {
      autoSize();
      fireValidationEvent();
      setLabelText(event.getValue());
   }

   @UiHandler("textArea")
   public void onKeyDownTextArea(KeyDownEvent event)
   {
      // used to determine whether user is still typing
      if (typing)
      {
         keypressed = true;
      }
      else
      {
         // set false so that next keypress is detectable
         keypressed = false;
         typing = true;
         typingCycles = 0;
         typingTimer.scheduleRepeating(TYPING_TIMER_INTERVAL);
      }
   }

   @UiHandler("copySourceButton")
   public void onCopySource(ClickEvent event)
   {
      listener.copySource(this);
   }

   @UiHandler("validateButton")
   public void onValidation(ClickEvent event)
   {
      fireValidationEvent();
   }

   @UiHandler("saveButton")
   public void onSaveAsApproved(ClickEvent event)
   {
      listener.saveAsApprovedAndMoveNext();
      event.stopPropagation();
   }

   @UiHandler("fuzzyButton")
   public void onSaveAsFuzzy(ClickEvent event)
   {
      listener.saveAsFuzzy();
      event.stopPropagation();
   }

   @UiHandler("cancelButton")
   public void onCancel(ClickEvent event)
   {
      listener.onCancel(this);
      event.stopPropagation();
   }

   @UiHandler("label")
   public void onLabelClick(MouseDownEvent event)
   {
      listener.toggleView(Editor.this);
   }

   @Override
   public ViewMode getViewMode()
   {
      if (label.isVisible())
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
      label.setVisible(viewMode == ViewMode.VIEW);
      textArea.setVisible(viewMode == ViewMode.EDIT);
      if (viewMode == ViewMode.EDIT)
      {
         listener.setValidationMessagePanel(this);
         fireValidationEvent();
         autoSize();
         Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand()
         {
            @Override
            public void execute()
            {
               textArea.setFocus(true);
            }
         });
      }
      else
      {
         removeValidationMessagePanel();
      }
      buttons.setVisible(viewMode == ViewMode.EDIT && listener.isDisplayButtons());
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
      setLabelText(text);
   }

   @Override
   public String getText()
   {
      return textArea.getText();
   }

   @Override
   public void setSaveButtonTitle(String title)
   {
      saveButton.setTitle(title);
   }

   @Override
   public void autoSize()
   {
      shrinkSize(true);
      growSize();
   }

   /**
    * forceShrink will resize the textArea to initialLines(3 lines) and growSize
    * according to the scroll height
    * 
    * @param forceShrink
    */
   @Override
   public void shrinkSize(boolean forceShrink)
   {
      if (forceShrink)
      {
         textArea.setVisibleLines(INITIAL_LINES);
      }
      else
      {
         if (textArea.getElement().getScrollHeight() <= (INITIAL_LINES * HEIGHT_PER_LINE))
         {
            textArea.setVisibleLines(INITIAL_LINES);
         }
         else
         {
            if (textArea.getElement().getScrollHeight() >= textArea.getElement().getClientHeight())
            {
               int newHeight = textArea.getElement().getScrollHeight() - textArea.getElement().getClientHeight() > 0 ? textArea.getElement().getScrollHeight() - textArea.getElement().getClientHeight() : HEIGHT_PER_LINE;
               int newLine = (newHeight / HEIGHT_PER_LINE) - 1 > INITIAL_LINES ? (newHeight / HEIGHT_PER_LINE) - 1 : INITIAL_LINES;
               int lines = textArea.getVisibleLines() - newLine;
               if (lines > 0)
               {
                  textArea.setVisibleLines(lines);
               }
            }
            growSize();
         }
      }
   }

   @Override
   public void growSize()
   {
      if (textArea.getElement().getScrollHeight() > textArea.getElement().getClientHeight())
      {
         int newHeight = textArea.getElement().getScrollHeight() - textArea.getElement().getClientHeight();
         int newLine = (newHeight / HEIGHT_PER_LINE) + 1;
         textArea.setVisibleLines(textArea.getVisibleLines() + newLine);
      }
   }

   @Override
   public void addValidationMessagePanel(IsWidget validationMessagePanel)
   {
      validationMessagePanelContainer.clear();
      validationMessagePanelContainer.add(validationMessagePanel);
   }

   @Override
   public void removeValidationMessagePanel()
   {
      validationMessagePanelContainer.clear();
   }

   @Override
   public void insertTextInCursorPosition(String suggestion)
   {
      String preCursor = textArea.getText().substring(0, textArea.getCursorPos());
      String postCursor = textArea.getText().substring(textArea.getCursorPos(), textArea.getText().length());

      textArea.setText(preCursor + suggestion + postCursor);
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).add("label", label.getText()).add("textArea", textArea.getText()).add("isOpen", textArea.isVisible()).toString();
   }

   @Override
   public int getIndex()
   {
      return index;
   }

   @Override
   public void showButtons(boolean displayButtons)
   {
      copySourceButton.setVisible(displayButtons);
      buttons.setVisible(getViewMode() == ViewMode.EDIT && displayButtons);
   }

   @Override
   public void setAsLastEditor()
   {
      saveButton.getUpFace().setImage(new Image(images.cellEditorAccept()));
   }

   @Override
   public void updateValidationWarning(List<String> errors)
   {
      // TODO update label css is errors is empty
   }
}
