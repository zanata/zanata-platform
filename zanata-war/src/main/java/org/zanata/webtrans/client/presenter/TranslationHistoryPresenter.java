package org.zanata.webtrans.client.presenter;

import java.util.Collections;
import java.util.List;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.ImmutableList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TranslationHistoryPresenter extends WidgetPresenter<TranslationHistoryDisplay>
{
   private final TranslationHistoryDisplay display;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;

   @Inject
   public TranslationHistoryPresenter(TranslationHistoryDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
   }

   @Override
   protected void onBind()
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }

   public void showTranslationHistory(TransUnitId transUnitId)
   {
      //TODO selection model
      //TODO compare history with current
      display.center();
      AsyncDataProvider<TransHistoryItem> dataProvider = new TransHistoryItemAsyncDataProvider(transUnitId);
      display.setDataProvider(dataProvider);
   }

   private class TransHistoryItemAsyncDataProvider extends AsyncDataProvider<TransHistoryItem>
   {

      private final TransUnitId transUnitId;
      private List<TransHistoryItem> transHistoryItems = Collections.emptyList();

      public TransHistoryItemAsyncDataProvider(TransUnitId transUnitId)
      {
         this.transUnitId = transUnitId;
      }

      @Override
      protected void onRangeChanged(final HasData<TransHistoryItem> historyTable)
      {
         int start = historyTable.getVisibleRange().getStart();
         int length = historyTable.getVisibleRange().getLength();
         int end = Math.min(start + length, historyTable.getRowCount());

         if (transHistoryItems.isEmpty())
         {
            loadHistoryFromServer(historyTable, length);
         }
         else
         {
            Log.info("start " + start + " end " + end + " items " + transHistoryItems.size());
            historyTable.setRowData(start, transHistoryItems.subList(start, end));
         }
      }

      private void loadHistoryFromServer(final HasData<TransHistoryItem> historyTable, final int length)
      {
         dispatcher.execute(new GetTranslationHistoryAction(transUnitId), new AsyncCallback<GetTranslationHistoryResult>()
         {
            @Override
            public void onFailure(Throwable caught)
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, caught.getMessage()));
               display.hide();
            }

            @Override
            public void onSuccess(GetTranslationHistoryResult result)
            {
               Log.info("get back " + result.getHistoryItems().size() + " items for " + transUnitId);
               transHistoryItems = ImmutableList.copyOf(result.getHistoryItems());
               updateRowCount(result.getHistoryItems().size(), true);
               historyTable.setVisibleRangeAndClearData(new Range(0, length), false);
               historyTable.setRowData(0, transHistoryItems.subList(0, length));
            }
         });
      }
   }
}
