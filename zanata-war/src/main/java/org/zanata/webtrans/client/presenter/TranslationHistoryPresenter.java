package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

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
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
   private final TransHistoryDataProvider listDataProvider;
   private final TransHistorySelectionModel selectionModel;
   private TargetContentsPresenter targetContentsPresenter;

   @Inject
   public TranslationHistoryPresenter(TranslationHistoryDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, WebTransMessages messages, TransHistorySelectionModel selectionModel, TransHistoryDataProvider dataProvider)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.messages = messages;
      this.selectionModel = selectionModel;

      listDataProvider = dataProvider;
      this.display.setDataProvider(listDataProvider);

      ColumnSortEvent.ListHandler<TransHistoryItem> sortHandler = new ColumnSortEvent.ListHandler<TransHistoryItem>(listDataProvider.getList());
      this.display.addVersionSortHandler(sortHandler);

      this.selectionModel.addSelectionChangeHandler(this);
      this.display.setSelectionModel(this.selectionModel);
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
            Log.error("failure getting translation history", caught);
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
               ArrayList<String> newTargets = targetContentsPresenter.getNewTargets();
               if (!Objects.equal(latest.getContents(), newTargets))
               {
                  listDataProvider.getList().add(new TransHistoryItem(messages.unsaved(), newTargets, ContentState.New, "", ""));
               }
               listDataProvider.getList().add(latest);
            }
            listDataProvider.getList().addAll(result.getHistoryItems());
            Comparator<TransHistoryItem> reverseComparator = Collections.reverseOrder(TransHistoryVersionComparator.COMPARATOR);
            Collections.sort(listDataProvider.getList(), reverseComparator);
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

   public void setCurrentValueHolder(TargetContentsPresenter targetContentsPresenter)
   {
      this.targetContentsPresenter = targetContentsPresenter;
   }

}
