package org.zanata.webtrans.client.presenter;

import java.util.Collections;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.ComparableByDate;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TranslationHistoryPresenter extends WidgetPresenter<TranslationHistoryDisplay> implements TranslationHistoryDisplay.Listener
{
   private final TranslationHistoryDisplay display;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final WebTransMessages messages;
   private final GetTransUnitActionContextHolder contextHolder;
   private TargetContentsPresenter targetContentsPresenter;
   private TransUnitId transUnitId;

   @Inject
   public TranslationHistoryPresenter(TranslationHistoryDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, WebTransMessages messages, GetTransUnitActionContextHolder contextHolder)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.messages = messages;
      this.contextHolder = contextHolder;

      display.setListener(this);
   }

   public void showTranslationHistory(final TransUnitId transUnitId)
   {
      this.transUnitId = transUnitId;
      popupAndShowLoading(messages.translationHistory());
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
            Log.info("get back " + result.getHistoryItems().size() + " trans history for id:" + transUnitId);
            displayEntries(result.getLatest(), result.getHistoryItems(), result.getReviewComments());
         }
      });
   }

   protected void popupAndShowLoading(String title)
   {
      //here we CANNOT use listDataProvider.setList() because we need to retain the same list reference which is used by ColumnSortEvent.ListHandler
//      listDataProvider.getList().clear();
//      listDataProvider.setLoading(true);
//      selectionModel.clear();
      display.setTitle(title);
      display.resetView();
      display.center();
   }

   protected void displayEntries(TransHistoryItem latest, List<TransHistoryItem> otherEntries, List<ReviewComment> reviewComments)
   {
      List<ComparableByDate> all = Lists.newArrayList();
      if (latest != null)
      {
         all.add(latest);
      }
      all.addAll(otherEntries);
      all.addAll(reviewComments);
      Collections.sort(all, Collections.reverseOrder());
      display.setData(all);
      // TODO implement this
//      if (latest != null)
//      {
//         //add indicator for latest version
//         latest.setVersionNum(messages.latestVersion(latest.getVersionNum()));
//         List<String> newTargets = targetContentsPresenter.getNewTargets();
//         if (!Objects.equal(latest.getContents(), newTargets))
//         {
//            listDataProvider.getList().add(new TransHistoryItem(messages.unsaved(), newTargets, ContentState.New, "", null));
//         }
//         listDataProvider.getList().add(latest);
//      }
//      listDataProvider.getList().addAll(otherEntries);
//      Comparator<TransHistoryItem> reverseComparator = Collections.reverseOrder(TransHistoryVersionComparator.COMPARATOR);
//      Collections.sort(listDataProvider.getList(), reverseComparator);
//      listDataProvider.setLoading(false);
   }

   public void onSelectionChange(SelectionChangeEvent event)
   {
//      Set<TransHistoryItem> historyItems = selectionModel.getSelectedSet();
//      if (historyItems.size() == 2)
//      {
//         //selected two. Compare against each other
//         Iterator<TransHistoryItem> iterator = historyItems.iterator();
//         TransHistoryItem one = iterator.next();
//         TransHistoryItem two = iterator.next();
//         display.showDiff(one, two, messages.translationHistoryComparison(one.getVersionNum(), two.getVersionNum()));
//      }
//      else
//      {
//         display.disableComparison();
//      }
   }

   @Override
   public void addComment(String commentContent)
   {
      dispatcher.execute(new AddReviewCommentAction(transUnitId, commentContent, contextHolder.getContext().getDocument().getId()), new AbstractAsyncCallback<AddReviewCommentResult>()
      {
         @Override
         public void onSuccess(AddReviewCommentResult result)
         {
            display.addCommentToList(result.getComment());
            display.clearInput();
         }
      });
   }

   @Override
   public void copyIntoEditor(List<String> contents)
   {
      eventBus.fireEvent(new CopyDataToEditorEvent(contents));
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
