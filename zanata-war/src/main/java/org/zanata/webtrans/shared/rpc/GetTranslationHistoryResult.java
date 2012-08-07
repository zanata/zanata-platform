package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GetTranslationHistoryResult implements DispatchResult
{
   private List<TransHistoryItem> historyItems = Lists.newArrayList();
   private TransHistoryItem latest;


   @SuppressWarnings("unused")
   private GetTranslationHistoryResult()
   {
   }

   public GetTranslationHistoryResult(Iterable<TransHistoryItem> historyItems, TransHistoryItem latest)
   {
      this.latest = latest;
      this.historyItems = Lists.newArrayList(historyItems);
   }

   public List<TransHistoryItem> getHistoryItems()
   {
      return historyItems;
   }

   public TransHistoryItem getLatest()
   {
      return latest;
   }
}
