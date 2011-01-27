package net.openl10n.flies.webtrans.client.editor.table;

import net.openl10n.flies.webtrans.client.ui.HighlightingLabel;
import net.openl10n.flies.webtrans.shared.model.TransUnit;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

public class SourcePanel extends Composite implements HasValue<TransUnit>
{

   private final FlowPanel panel;
   private final Label sourceLabel;
   private final TextArea textarea;
   private TransUnit value;
   private String phrase;

   private final NavigationMessages messages;

   public SourcePanel(TransUnit value, NavigationMessages messages, String phrase)
   {
      this.value = value;
      this.messages = messages;
      this.phrase = phrase;
      panel = new FlowPanel();
      panel.setSize("100%", "100%");
      initWidget(panel);
      setStylePrimaryName("TableEditorSource");

      sourceLabel = new HighlightingLabel(value.getSource());
      sourceLabel.setStylePrimaryName("TableEditorContent");
      sourceLabel.setTitle(messages.sourceCommentLabel() + value.getSourceComment());
      if (phrase != null)
      {
         ((HighlightingLabel) sourceLabel).findMessage(phrase);
      }

      panel.add(sourceLabel);
      textarea = new TextArea();
      textarea.getSelectedText();
      refresh();
   }

   public void refresh()
   {
   }

   @Override
   public TransUnit getValue()
   {
      return value;
   }

   @Override
   public void setValue(TransUnit value)
   {
      setValue(value, true);
   }

   @Override
   public void setValue(TransUnit value, boolean fireEvents)
   {
      if (this.value != value)
      {
         this.value = value;
         if (fireEvents)
         {
            ValueChangeEvent.fire(this, value);
         }
         refresh();
      }
   }

   @Override
   public HandlerRegistration addValueChangeHandler(ValueChangeHandler<TransUnit> handler)
   {
      return addHandler(handler, ValueChangeEvent.getType());
   }
}
