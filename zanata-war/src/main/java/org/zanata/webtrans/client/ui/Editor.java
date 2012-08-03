package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.client.editor.table.EditorTextArea;
import org.zanata.webtrans.client.editor.table.TableResources;
import org.zanata.webtrans.client.editor.table.TargetContentsDisplay;
import org.zanata.webtrans.client.resources.NavigationMessages;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class Editor extends Composite implements ToggleEditor
{
   private String findMessage;
   private TargetContentsDisplay.Listener listener;

   interface EditorUiBinder extends UiBinder<Widget, Editor>
   {
   }

   interface Styles extends CssResource
   {
      String userLabel();

      String rootContainer();

      String translatorList();

      String textArea();

      String bottomContainer();
   }

   private static EditorUiBinder uiBinder = GWT.create(EditorUiBinder.class);

   private static final int INITIAL_LINES = 3;

   private static final int TYPING_TIMER_INTERVAL = 500; // ms
   private static final int TYPING_TIMER_RECURRENT_VALIDATION_PERIOD = 5; // intervals

   private final int index;

   private boolean isFocused;

   @UiField
   Styles style;

   @UiField
   FocusPanel rootContainer;

   @UiField
   HorizontalPanel topContainer;

   @UiField
   HorizontalPanel bottomContainer;

   @UiField
   FlowPanel validationMessagePanelContainer;

   @UiField
   HorizontalPanel translatorList;

   @UiField
   TableResources images;

   NavigationMessages messages = GWT.create(NavigationMessages.class);

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
      showCopySourceButton(listener.isDisplayButtons());

      setLabelText(displayString);

      if (!listener.isReadOnly())
      {
         label.setTitle(messages.clickHere());
         setViewMode(ViewMode.EDIT);
      }
   }

   private void setLabelText(String displayString)
   {
      if (Strings.isNullOrEmpty(displayString))
      {
         if (listener.isReadOnly())
         {
            label.setText(messages.noContent());
         }
         else
         {
            label.setText(messages.clickHere());
         }
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
      if (getViewMode() == ViewMode.EDIT)
      {
         listener.validate(this);
      }
   }

   @UiHandler("textArea")
   public void onValueChange(ValueChangeEvent<String> event)
   {
      autoSize();
      fireValidationEvent();
      setLabelText(event.getValue());
   }

   @UiHandler("textArea")
   public void onTextAreaFocus(FocusEvent event)
   {
      listener.setValidationMessagePanel(this);
      listener.toggleView(this);
      event.stopPropagation();
      isFocused = true;
   }

   @UiHandler("textArea")
   public void onTextAreaBlur(BlurEvent event)
   {
      isFocused = false;
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
      translatorList.setVisible(viewMode == ViewMode.EDIT);
      if (viewMode == ViewMode.EDIT)
      {
         autoSize();
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
      setLabelText(text);
   }

   @Override
   public String getText()
   {
      String textAreaText = textArea.getText();
      if (!label.getText().equals(textAreaText))
      {
         setLabelText(textAreaText);
      }
      return textAreaText;
   }

   @Override
   public void autoSize()
   {
      textArea.setVisibleLines(INITIAL_LINES);
      while (textArea.getElement().getScrollHeight() > textArea.getElement().getClientHeight())
      {
         textArea.setVisibleLines(textArea.getVisibleLines() + 1);
      }
   }

   /**
    * when user press enter, it will autosize first and then the enter itself
    * will increase one line
    * 
    */
   @Override
   public void autoSizePlusOne()
   {
      autoSize();
      textArea.setVisibleLines(textArea.getVisibleLines() + 1);
   }

   @Override
   public void setFocus()
   {
      textArea.setFocus(true);
   }

   @Override
   public void addValidationMessagePanel(IsWidget validationMessagePanel)
   {
      removeValidationMessagePanel();
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
      textArea.setCursorPos(textArea.getText().indexOf(suggestion) + suggestion.length());
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this)
//            .add("label", label.getText())
//            .add("textArea", textArea.getText())
            .add("isOpen", textArea.isVisible())
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
      copySourceButton.setVisible(displayButtons);
   }

   @Override
   public void updateValidationWarning(List<String> errors)
   {
      if (!errors.isEmpty())
      {
         textArea.addStyleName("HasValidationError");
      }
      else
      {
         textArea.removeStyleName("HasValidationError");
      }
   }

   @Override
   public void addTranslator(String name, String color)
   {
      Label nameLabel = new Label(name);
      nameLabel.setStyleName(style.userLabel());

      nameLabel.getElement().getStyle().setProperty("backgroundColor", color);
      nameLabel.getElement().getStyle().setProperty("borderColor", color);
      nameLabel.getElement().getStyle().setProperty("borderWidth", "1px");
      nameLabel.getElement().getStyle().setProperty("borderStyle", "solid");

      translatorList.add(nameLabel);
   }

   @Override
   public void clearTranslatorList()
   {
      translatorList.clear();
   }

   @Override
   public void removeTranslator(String name, String color)
   {
      for (int i = 0; i < translatorList.getWidgetCount(); i++)
      {
         Label translatorLabel = (Label) translatorList.getWidget(i);

         if (translatorLabel.getText().equals(name) && removeFormat(translatorLabel.getElement().getStyle().getProperty("backgroundColor")).equals(removeFormat(color)))
         {
            translatorList.remove(i);
         }
      }
   }
   

   /**
    * Color string return from userSessionService rgb(xx,xx,xx), Color string
    * return from browser is formatted rgb(xx, xx, xx). Method needed to
    * unformat all color
    * 
    * @param color
    * @return
    */
   private String removeFormat(String color)
   {
      return color.replace(" ", "");
   }

   @Override
   public boolean isFocused()
   {
      return isFocused;
   }
}
