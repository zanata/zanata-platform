package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.Event;
import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.editor.table.EditorTextArea;
import org.zanata.webtrans.client.editor.table.TableResources;
import org.zanata.webtrans.client.editor.table.TargetContentsDisplay;
import org.zanata.webtrans.client.editor.table.ToggleEditor;
import org.zanata.webtrans.client.resources.NavigationMessages;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

   @UiField
   HorizontalPanel topContainer;

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

   // TableResources images = GWT.create(TableResources.class);

   public Editor(String displayString, String findMessage, TargetContentsDisplay.Listener listener)
   {
      this.listener = listener;
      initWidget(uiBinder.createAndBindUi(this));

      validateButton.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (Strings.isNullOrEmpty(getContent()))
            {
               // fireValidationEvent(eventBus);
            }
         }
      });

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
            // fireValidationEvent(eventBus);
         }

      });
   }

   @UiHandler("copySourceButton")
   public void onCopySource(ClickEvent event)
   {
      listener.copySource(this);
   }

   @UiHandler("validateButton")
   public void onValidation(ClickEvent event)
   {
      listener.validate(this);
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
      buttons.setVisible(viewMode == ViewMode.EDIT);
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

   private void toggleView()
   {
      if (label.isVisible())
      {
         textArea.setVisible(true);
         buttons.setVisible(true);
         label.setVisible(false);
      }
      else
      {
         textArea.setVisible(false);
         buttons.setVisible(false);
         label.setVisible(true);
      }
   }

   private void fireValidationEvent(final EventBus eventBus)
   {
      // eventBus.fireEvent(new RunValidationEvent(cellValue.getId(),
      // cellValue.getSource(), textArea.getText(), false));
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
}
