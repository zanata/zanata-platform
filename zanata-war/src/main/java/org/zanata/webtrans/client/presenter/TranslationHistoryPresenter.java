package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
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
   private final ListDataProvider<TransHistoryItem> listDataProvider;
   private final SingleSelectionModel<TransHistoryItem> selectionModel;

   @Inject
   public TranslationHistoryPresenter(TranslationHistoryDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      listDataProvider = new ListDataProvider<TransHistoryItem>(TranslationHistoryDisplay.HISTORY_ITEM_PROVIDES_KEY);
      listDataProvider.addDataDisplay(display.getHistoryTable());

      selectionModel = new SingleSelectionModel<TransHistoryItem>(TranslationHistoryDisplay.HISTORY_ITEM_PROVIDES_KEY);
      selectionModel.addSelectionChangeHandler(this);
      display.getHistoryTable().setSelectionModel(selectionModel);
   }

   public void showTranslationHistory(final TransUnitId transUnitId)
   {
      //TODO compare history with current
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
            listDataProvider.setList(result.getHistoryItems());
            display.resetPage();
         }
      });
   }

   @Override
   public void onSelectionChange(SelectionChangeEvent event)
   {
      TransHistoryItem historyItem = selectionModel.getSelectedObject();
      if (historyItem == null)
      {
         return;
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
}
