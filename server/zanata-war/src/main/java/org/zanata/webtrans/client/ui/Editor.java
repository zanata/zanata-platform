package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.editor.table.EditorTextArea;
import org.zanata.webtrans.client.editor.table.TableResources;
import org.zanata.webtrans.client.editor.table.TargetContentsDisplay;
import org.zanata.webtrans.client.resources.NavigationMessages;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class Editor extends Composite implements ToggleEditor
{
   private TargetContentsDisplay.Listener listener;

   interface EditorUiBinder extends UiBinder<Widget, Editor>
   {
   }

   private static EditorUiBinder uiBinder = GWT.create(EditorUiBinder.class);

   private static final int INITIAL_LINES = 3;
   private static final int HEIGHT_PER_LINE = 16;

   private final int TYPING_TIMER_INTERVAL = 500; // ms
   private final int TYPING_TIMER_RECURRENT_VALIDATION_PERIOD = 5; // intervals

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

   public Editor(String displayString, String findMessage, final TargetContentsDisplay.Listener listener)
   {
      this.listener = listener;
      initWidget(uiBinder.createAndBindUi(this));

      saveButton.addClickHandler(acceptHandler);

      fuzzyButton.addClickHandler(fuzzyHandler);

      cancelButton.addClickHandler(cancelHandler);

      label.setText(displayString);
      if (displayString == null || displayString.isEmpty())
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

      label.setTitle(messages.clickHere());

      // textArea.setStyleName("TableEditorContent-Edit");
      textArea.setVisible(false);

      textArea.addValueChangeHandler(new ValueChangeHandler<String>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            autoSize();
            fireValidationEvent();
         }

      });

      final Timer typingTimer = new Timer()
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

      // used to determine whether user is still typing
      textArea.addKeyDownHandler(new KeyDownHandler()
      {
         @Override
         public void onKeyDown(KeyDownEvent event)
         {
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
      });
   }

   private void fireValidationEvent()
   {
      if (!Strings.isNullOrEmpty(getContent()))
      {
         listener.validate(this);
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
      listener.saveAsApproved(this);
      event.stopPropagation();
   }

   @UiHandler("label")
   public void onLabelClick(ClickEvent event)
   {
      listener.toggleView(this);
      // toggleView();
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
         textArea.setFocus(true);
         listener.setValidationMessagePanel(this);
         fireValidationEvent();
      }
      buttons.setVisible(viewMode == ViewMode.EDIT);
      //sync label and text area
      label.setText(textArea.getText());
   }

   @Override
   public void setText(String text)
   {
      label.setText(text);
      textArea.setText(text);
   }

   @Override
   public String getText()
   {
      return textArea.getText();
   }

   /**
    * The click listener used to save as fuzzy.
    */
   private ClickHandler fuzzyHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         // acceptFuzzyEdit();
      }
   };

   /**
    * The click listener used to cancel.
    */
   private ClickHandler cancelHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         // cancelEdit();
      }
   };

   /**
    * The click listener used to accept.
    */
   private ClickHandler acceptHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         // saveApprovedAndMoveRow(NavigationType.NextEntry);
      }
   };

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
   private void shrinkSize(boolean forceShrink)
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
               textArea.setVisibleLines(textArea.getVisibleLines() - newLine);
            }
            growSize();
         }
      }
   }

   private String getContent()
   {
      return textArea.getText();
   }

   private void growSize()
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
   public void insertTextInCursorPosition(String suggestion)
   {
      String preCursor = textArea.getText().substring(0, textArea.getCursorPos());
      String postCursor = textArea.getText().substring(textArea.getCursorPos(), textArea.getText().length());

      textArea.setText(preCursor + suggestion + postCursor);

   }
}
