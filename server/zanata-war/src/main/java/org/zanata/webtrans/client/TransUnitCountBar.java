package org.zanata.webtrans.client;

import org.zanata.common.ContentState;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.editor.HasTranslationStats;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitCountBar extends Composite implements HasTranslationStats
{

   private static TransUnitCountBarUiBinder uiBinder = GWT.create(TransUnitCountBarUiBinder.class);

   interface TransUnitCountBarUiBinder extends UiBinder<Widget, TransUnitCountBar>
   {
   }

   private LabelFormat labelFormat = LabelFormat.DEFAULT_FORMAT;

   @UiField
   LayoutPanel layoutPanel;

   @UiField
   FlowPanel approvedPanel, needReviewPanel, untranslatedPanel, undefinedPanel;

   @UiField
   Label label;

   private final TranslationStats stats = new TranslationStats();

   private final WebTransMessages messages;

   @Inject
   public TransUnitCountBar(WebTransMessages messages)
   {
      this.messages = messages;
      initWidget(uiBinder.createAndBindUi(this));
   }

   public void refresh()
   {
      TransUnitCount count = stats.getUnitCount();
      TransUnitWords words = stats.getWordCount();
      int approved = count.get(ContentState.Approved);
      int needReview = count.get(ContentState.NeedReview);
      int untranslated = count.get(ContentState.New);
      int total = approved + needReview + untranslated;
      int width = getOffsetWidth();
      layoutPanel.forceLayout();
      if (total == 0)
      {
         layoutPanel.setWidgetLeftWidth(undefinedPanel, 0.0, Unit.PX, 100, Unit.PC);
         layoutPanel.setWidgetLeftWidth(approvedPanel, 0.0, Unit.PX, 0, Unit.PX);
         layoutPanel.setWidgetLeftWidth(needReviewPanel, 0.0, Unit.PX, 0, Unit.PX);
         layoutPanel.setWidgetLeftWidth(untranslatedPanel, 0.0, Unit.PX, 0, Unit.PX);
         label.setText("");
      }
      else
      {
         if (labelFormat != LabelFormat.MESSAGE_COUNTS)
         {
            approved = words.get(ContentState.Approved);
            needReview = words.get(ContentState.NeedReview);
            untranslated = words.get(ContentState.New);
            total = approved + needReview + untranslated;
         }
         int completePx = approved * 100 / total * width / 100;
         int inProgressPx = needReview * 100 / total * width / 100;
         ;
         int unfinishedPx = untranslated * 100 / total * width / 100;
         ;

         layoutPanel.setWidgetLeftWidth(undefinedPanel, 0.0, Unit.PX, 0, Unit.PX);

         layoutPanel.setWidgetLeftWidth(approvedPanel, 0.0, Unit.PX, completePx, Unit.PX);
         layoutPanel.setWidgetLeftWidth(needReviewPanel, completePx, Unit.PX, inProgressPx, Unit.PX);
         layoutPanel.setWidgetLeftWidth(untranslatedPanel, completePx + inProgressPx, Unit.PX, unfinishedPx, Unit.PX);

         switch (labelFormat)
         {
         case PERCENT_COMPLETE:
            label.setText(messages.statusBarLabelPercentage(approved * 100 / total, needReview * 100 / total, untranslated * 100 / total));
            break;
         case HOURS_REMAIN:
            double remainHours = remainingHours(needReview, untranslated);
            // label.setText(NumberFormat.getFormat("0.0").format(remainHours));
            label.setText(messages.statusBarLabelWork(remainHours));
            break;
         case WORD_COUNTS:
            label.setText(messages.statusBarLabelWords(approved, needReview, untranslated));
            break;
         case MESSAGE_COUNTS:
            label.setText(messages.statusBarLabelUnits(approved, needReview, untranslated));
            break;
         default:
            // error
            label.setText("error: " + labelFormat.name());
         }
      }

      layoutPanel.animate(1000);
   }

   protected double remainingHours(int fuzzyWords, int untranslatedWords)
   {
      double untransHours = untranslatedWords / 250.0;
      double fuzzyHours = fuzzyWords / 500.0;
      double remainHours = untransHours + fuzzyHours;
      return remainHours;
   }

   public void setLabelFormat(LabelFormat labelFormat)
   {
      this.labelFormat = labelFormat;
      refresh();
   }

   @UiHandler("label")
   public void onLabelClick(ClickEvent event)
   {
      setLabelFormat(labelFormat.next());
   }

   @Override
   public void setStats(TranslationStats stats)
   {
      this.stats.set(stats);
      refresh();
   }
}
