package org.zanata.webtrans.client.ui;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitCountBar extends Composite implements HasTranslationStats, HasMouseOverHandlers, HasMouseOutHandlers, HasClickHandlers
{

   private static TransUnitCountBarUiBinder uiBinder = GWT.create(TransUnitCountBarUiBinder.class);

   protected final TooltipPopupPanel tooltipPanel;
   private static final int TOTAL_WIDTH = 100;

   interface TransUnitCountBarUiBinder extends UiBinder<Widget, TransUnitCountBar>
   {
   }

   @UiField
   LayoutPanel layoutPanel;

   @UiField
   FlowPanel approvedPanel, draftPanel, untranslatedPanel, undefinedPanel, translatedPanel;

   @UiField
   Label label;

   private final LabelFormat labelFormat;

   private ContainerTranslationStatistics stats = new ContainerTranslationStatistics();

   private final WebTransMessages messages;

   private final LocaleId localeId;

   private boolean statsByWords = true;

   @Inject
   public TransUnitCountBar(UserWorkspaceContext userworkspaceContext, WebTransMessages messages, LabelFormat labelFormat, boolean enableClickToggle, boolean projectRequireReview)
   {
      this.messages = messages;
      this.labelFormat = labelFormat;
      localeId = userworkspaceContext.getWorkspaceContext().getWorkspaceId().getLocaleId();

      tooltipPanel = new TooltipPopupPanel(projectRequireReview);

      initWidget(uiBinder.createAndBindUi(this));

      this.addMouseOutHandler(new MouseOutHandler()
      {
         @Override
         public void onMouseOut(MouseOutEvent event)
         {
            tooltipPanel.hide(true);
         }
      });

      this.addMouseOverHandler(new MouseOverHandler()
      {

         @Override
         public void onMouseOver(MouseOverEvent event)
         {
            tooltipPanel.showRelativeTo(layoutPanel);
         }
      });

      if (enableClickToggle)
      {
         this.addClickHandler(new ClickHandler()
         {
            @Override
            public void onClick(ClickEvent event)
            {
               setStatOption(!statsByWords);
            }
         });
      }

      sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONCLICK);
   }

   public void setStatOption(boolean statsByWords)
   {
      this.statsByWords = statsByWords;
      refresh();
   }

   private void setupLayoutPanel(double undefinedLeft, double undefinedWidth, double approvedLeft, double approvedWidth, double savedLeft, double savedWidth, double needReviewLeft, double needReviewWidth, double untranslatedLeft, double untranslatedWidth)
   {
      layoutPanel.forceLayout();
      layoutPanel.setWidgetLeftWidth(undefinedPanel, undefinedLeft, Unit.PX, undefinedWidth, Unit.PX);
      layoutPanel.setWidgetLeftWidth(approvedPanel, approvedLeft, Unit.PX, approvedWidth, Unit.PX);
      layoutPanel.setWidgetLeftWidth(translatedPanel, savedLeft, Unit.PX, savedWidth, Unit.PX);
      layoutPanel.setWidgetLeftWidth(draftPanel, needReviewLeft, Unit.PX, needReviewWidth, Unit.PX);
      layoutPanel.setWidgetLeftWidth(untranslatedPanel, untranslatedLeft, Unit.PX, untranslatedWidth, Unit.PX);
   }

   public void refresh()
   {
      int approved, draft, untranslated, translated, total;
      if (statsByWords)
      {
         approved = getWordsApproved();
         draft = getWordsDraft();
         untranslated = getWordsUntranslated();
         translated = getWordsTranslated();
         total = getWordsTotal();
      }
      else
      {
         approved = getUnitApproved();
         draft = getUnitDraft();
         untranslated = getUnitUntranslated();
         translated = getUnitTranslated();
         total = getUnitTotal();
      }
      int width = getOffsetWidth();
      if (total == 0)
      {
         undefinedPanel.clear();
         undefinedPanel.add(new Label(messages.noContent()));
         setupLayoutPanel(0.0, width, 0, 0, 0.0, 0, 0.0, 0, 0.0, 0);
         label.setText("");
      }
      else
      {
         int completePx = approved * 100 / total * width / TOTAL_WIDTH;
         int savedPx = translated * 100 / total * width / TOTAL_WIDTH;
         int inProgressPx = draft * 100 / total * width / TOTAL_WIDTH;
         int unfinishedPx = untranslated * 100 / total * width / TOTAL_WIDTH;

         int needReviewLeft = savedPx + completePx;
         int untranslatedLeft = needReviewLeft + inProgressPx;
         setupLayoutPanel(0.0, 0, 0.0, completePx, completePx, savedPx, needReviewLeft, inProgressPx, untranslatedLeft, unfinishedPx);
         setLabelText();
      }

      int duration = 600;

      tooltipPanel.refreshData(this);
      layoutPanel.animate(duration);
   }

   private void setLabelText()
   {
      // TODO rhbz953734 - remaining hours
      switch (labelFormat)
      {
      case PERCENT_COMPLETE_HRS:
         TranslationStatistics wordStats = stats.getStats(localeId.getId(), StatUnit.WORD);
         if (statsByWords)
         {
            label.setText(messages.statusBarPercentageHrs(wordStats.getPercentTranslated(), wordStats.getRemainingHours(), "Words"));
         }
         else
         {
            TranslationStatistics msgStats = stats.getStats(localeId.getId(), StatUnit.MESSAGE);
            label.setText(messages.statusBarPercentageHrs(msgStats.getPercentTranslated(), wordStats.getRemainingHours(), "Msg"));
         }
         break;
      case PERCENT_COMPLETE:
         if (statsByWords)
         {
            label.setText(messages.statusBarLabelPercentage(stats.getStats(localeId.getId(), StatUnit.WORD).getPercentTranslated()));
         }
         else
         {
            label.setText(messages.statusBarLabelPercentage(stats.getStats(localeId.getId(), StatUnit.MESSAGE).getPercentTranslated()));
         }
         break;
      default:
         label.setText("error: " + labelFormat.name());
      }
   }

   private TranslationStatistics getWordStats()
   {
      return stats.getStats(localeId.getId(), StatUnit.WORD);
   }

   private TranslationStatistics getMessageStats()
   {
      return stats.getStats(localeId.getId(), StatUnit.MESSAGE);
   }

   public int getWordsTotal()
   {
      return getWordsApproved() + getWordsDraft() + getWordsUntranslated() + getWordsTranslated();
   }

   public int getWordsApproved()
   {
      TranslationStatistics stats = getWordStats();
      if (stats != null)
      {
         return (int) stats.getApproved();
      }
      return 0;
   }

   public int getWordsDraft()
   {
      TranslationStatistics stats = getWordStats();
      if (stats != null)
      {
         return (int) stats.getDraft();
      }
      return 0;
   }

   public int getWordsUntranslated()
   {
      TranslationStatistics stats = getWordStats();
      if (stats != null)
      {
         return (int) stats.getUntranslated();
      }
      return 0;
   }

   public int getWordsTranslated()
   {
      TranslationStatistics stats = getWordStats();
      if (stats != null)
      {
         return (int) stats.getReadyForReview();
      }
      return 0;
   }

   public int getUnitTotal()
   {
      return getUnitApproved() + getUnitDraft() + getUnitTranslated() + getUnitUntranslated();
   }

   public int getUnitApproved()
   {
      TranslationStatistics stats = getMessageStats();
      if (stats != null)
      {
         return (int) stats.getApproved();
      }
      return 0;
   }

   public int getUnitDraft()
   {
      TranslationStatistics stats = getMessageStats();
      if (stats != null)
      {
         return (int) stats.getDraft();
      }
      return 0;
   }

   public int getUnitUntranslated()
   {
      TranslationStatistics stats = getMessageStats();
      if (stats != null)
      {
         return (int) stats.getUntranslated();
      }
      return 0;
   }

   public int getUnitTranslated()
   {
	  TranslationStatistics stats = getMessageStats();
      if (stats != null)
      {
         return (int) stats.getReadyForReview();
      }
      return 0;
   }

   @Override
   public void setStats(ContainerTranslationStatistics stats, boolean statsByWords)
   {
      this.stats.copyFrom(stats);
      this.statsByWords = statsByWords;

      refresh();
   }

   @Override
   public int getOffsetWidth()
   {
      int offsetWidth = super.getOffsetWidth();
      return offsetWidth == 0 || offsetWidth > 100 ? 100 : offsetWidth;
   }

   @Override
   public HandlerRegistration addMouseOutHandler(MouseOutHandler handler)
   {
      return addDomHandler(handler, MouseOutEvent.getType());
   }

   @Override
   public HandlerRegistration addMouseOverHandler(MouseOverHandler handler)
   {
      return addDomHandler(handler, MouseOverEvent.getType());
   }

   @Override
   public HandlerRegistration addClickHandler(ClickHandler handler)
   {
      return addDomHandler(handler, ClickEvent.getType());
   }
}
