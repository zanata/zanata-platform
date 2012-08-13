package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TranslationHistoryPresenter extends WidgetPresenter<TranslationHistoryDisplay> implements SelectionChangeEvent.Handler
{
   private final TranslationHistoryDisplay display;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final WebTransMessages messages;
   private final ListDataProvider<TransHistoryItem> listDataProvider;
   private final MultiSelectionModel<TransHistoryItem> selectionModel;
   private TargetContentsPresenter targetContentsPresenter;

   @Inject
   public TranslationHistoryPresenter(TranslationHistoryDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, WebTransMessages messages)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.messages = messages;
      listDataProvider = new ListDataProvider<TransHistoryItem>(TranslationHistoryDisplay.HISTORY_ITEM_PROVIDES_KEY);
      listDataProvider.addDataDisplay(display.getHistoryTable());
      ColumnSortEvent.ListHandler<TransHistoryItem> sortHandler = new ColumnSortEvent.ListHandler<TransHistoryItem>(listDataProvider.getList());
      sortHandler.setComparator(display.getVersionColumn(), TransHistoryVersionComparator.COMPARATOR);
      display.addVersionSortHandler(sortHandler);

      selectionModel = new MultiSelectionModel<TransHistoryItem>(TranslationHistoryDisplay.HISTORY_ITEM_PROVIDES_KEY);
      selectionModel.addSelectionChangeHandler(this);
      display.setSelectionModel(selectionModel);
   }

   public void showTranslationHistory(final TransUnitId transUnitId)
   {
      display.resetView();
      display.center();
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
            //here we CANNOT use listDataProvider.setList() because we need to retain the same list reference which is used by ColumnSortEvent.ListHandler
            listDataProvider.getList().clear();
            TransHistoryItem latest = result.getLatest();
            if (latest != null)
            {
               //add indicator for latest version
               latest.setVersionNum(messages.latestVersion(latest.getVersionNum()));
               if (!Objects.equal(latest.getContents(), targetContentsPresenter.getNewTargets()))
               {
                  listDataProvider.getList().add(new TransHistoryItem(messages.current(), targetContentsPresenter.getNewTargets(), ContentState.New, "", ""));
               }
               listDataProvider.getList().add(latest);
            }
            listDataProvider.getList().addAll(result.getHistoryItems());
         }
      });
   }

   @Override
   public void onSelectionChange(SelectionChangeEvent event)
   {
      Set<TransHistoryItem> historyItems = selectionModel.getSelectedSet();
      if (historyItems.size() == 2)
      {
         //selected two. Compare against each other
         Iterator<TransHistoryItem> iterator = historyItems.iterator();
         TransHistoryItem one = iterator.next();
         TransHistoryItem two = iterator.next();
         display.showDiff(one, two, messages.translationHistoryComparison(one.getVersionNum(), two.getVersionNum()));
      }
      else
      {
         display.disableComparison();
      }
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

   public void addCurrentValueHolder(TargetContentsPresenter targetContentsPresenter)
   {
      this.targetContentsPresenter = targetContentsPresenter;
   }

   private static enum TransHistoryVersionComparator implements Comparator<TransHistoryItem>
   {
      COMPARATOR;

      @Override
      public int compare(TransHistoryItem one, TransHistoryItem two)
      {
         if (itemIsOldVersion(one) && itemIsOldVersion(two))
         {
            Integer verOne = Integer.parseInt(one.getVersionNum());
            Integer verTwo = Integer.parseInt(two.getVersionNum());
            return verOne.compareTo(verTwo);
         }
         if (itemIsCurrentValue(one))
         {
            //first is current value
            return 1;
         }
         if (itemIsLatestVersion(one) && itemIsCurrentValue(two))
         {
            //first is latest version but second is current value
            return -1;
         }
         //else first is old version and second is not old version
         return -1;
      }

      private static boolean itemIsOldVersion(TransHistoryItem one)
      {
         return one.getVersionNum().matches("\\d+");
      }

      private static boolean itemIsLatestVersion(TransHistoryItem one)
      {
         return one.getVersionNum().matches("\\d+\\w+");
      }

      private static boolean itemIsCurrentValue(TransHistoryItem one)
      {
         return one.getVersionNum().matches("\\w+");
      }
   }
}
