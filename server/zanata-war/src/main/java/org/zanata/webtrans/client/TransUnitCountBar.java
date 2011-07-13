package org.zanata.webtrans.client;

import org.zanata.common.ContentState;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.editor.HasTranslationStats;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
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

   LabelFormat labelFormat = LabelFormat.DEFAULT_FORMAT;

   @UiField
   LayoutPanel layoutPanel;

   @UiField
   FlowPanel approvedPanel, needReviewPanel, untranslatedPanel, undefinedPanel;

   @UiField
   Label label;

   private final TranslationStats stats = new TranslationStats();

   private final WebTransMessages messages;
   
   private int totalWidth = 100;

   @Inject
   public TransUnitCountBar(WebTransMessages messages)
   {
      this.messages = messages;
      initWidget(uiBinder.createAndBindUi(this));
   }

   public TransUnitCountBar(WebTransMessages messages, int totalWidth)
   {
      this.messages = messages;
      this.totalWidth = totalWidth;
   }

   protected LayoutPanel setupLayoutPanel(double undefinedLeft, double undefinedWidth, double approvedLeft, double approvedWidth, double needReviewLeft, double needReviewWidth, double untranslatedLeft, double untranslatedWidth)
   {
      layoutPanel.setWidgetLeftWidth(undefinedPanel, undefinedLeft, Unit.PX, undefinedWidth, Unit.PX);
      layoutPanel.setWidgetLeftWidth(approvedPanel, approvedLeft, Unit.PX, approvedWidth, Unit.PX);
      layoutPanel.setWidgetLeftWidth(needReviewPanel, needReviewLeft, Unit.PX, needReviewWidth, Unit.PX);
      layoutPanel.setWidgetLeftWidth(untranslatedPanel, untranslatedLeft, Unit.PX, untranslatedWidth, Unit.PX);
      return layoutPanel;
   }

   public void refresh()
   {
      int approved = getUnitApproved();
      int needReview = getUnitNeedReview();
      int untranslated = getUnitUntranslated();
      int total = getUnitTotal();
      int width = getOffsetWidth();
      layoutPanel.forceLayout();
      if (total == 0)
      {
         setupLayoutPanel(0.0, 100, 0.0, 0, 0.0, 0, 0.0, 0);
         label.setText("");
      }
      else
      {
         if (labelFormat != LabelFormat.MESSAGE_COUNTS)
         {
            approved = getWordsApproved();
            needReview = getWordsNeedReview();
            untranslated = getWordsUntranslated();
            total = getWordsTotal();
         }
         int completePx = approved * 100 / total * width / totalWidth;
         int inProgressPx = needReview * 100 / total * width / totalWidth;
         int unfinishedPx = untranslated * 100 / total * width / totalWidth;

         setupLayoutPanel(0.0, 0, 0.0, completePx, completePx, inProgressPx, completePx + inProgressPx, unfinishedPx);

         switch (labelFormat)
         {
         case PERCENT_COMPLETE:
            label.setText(messages.statusBarLabelPercentage(approved * 100 / total, needReview * 100 / total, untranslated * 100 / total));
            break;
         case HOURS_REMAIN:
            double remainHours = remainingHours(needReview, untranslated);
            label.setText(messages.statusBarLabelWork(remainHours));
            break;
         case WORD_COUNTS:
            label.setText(messages.statusBarLabelWords(approved, needReview, untranslated));
            break;
         case MESSAGE_COUNTS:
            label.setText(messages.statusBarLabelUnits(approved, needReview, untranslated));
            break;
         default:
            label.setText("error: " + labelFormat.name());
         }
      }
      refreshPopupPanel();
      layoutPanel.animate(1000);
   }

   public void refreshPopupPanel()
   {
   }

   public int getWordsTotal()
   {
      return getWordsApproved() + getWordsNeedReview() + getWordsUntranslated();
   }

   public int getWordsApproved()
   {
      return stats.getWordCount().get(ContentState.Approved);
   }

   public int getWordsNeedReview()
   {
      return stats.getWordCount().get(ContentState.NeedReview);
   }

   public int getWordsUntranslated()
   {
      return stats.getWordCount().get(ContentState.New);
   }

   public int getUnitTotal()
   {
      return getUnitApproved() + getUnitNeedReview() + getUnitUntranslated();
   }

   public int getUnitApproved()
   {
      return stats.getUnitCount().get(ContentState.Approved);
   }

   public int getUnitNeedReview()
   {
      return stats.getUnitCount().get(ContentState.NeedReview);
   }

   public int getUnitUntranslated()
   {
      return stats.getUnitCount().get(ContentState.New);
   }

   protected String getRemainingWordsHours()
   {
      double remainingHours = remainingHours(getWordsNeedReview(), getWordsUntranslated());
      return NumberFormat.getFormat("#").format(remainingHours);
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
