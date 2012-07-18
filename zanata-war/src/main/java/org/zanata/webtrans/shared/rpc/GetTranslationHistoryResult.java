package org.zanata.webtrans.shared.rpc;

import java.util.Collection;
import java.util.List;

import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.common.collect.Lists;

import net.customware.gwt.dispatch.shared.Result;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GetTranslationHistoryResult implements DispatchResult
{
   private List<TransHistoryItem> historyItems = Lists.newArrayList();


   @SuppressWarnings("unused")
   private GetTranslationHistoryResult()
   {
   }

   public GetTranslationHistoryResult(Collection<TransHistoryItem> historyItems)
   {
      this.historyItems = Lists.newArrayList(historyItems);
   }

   public List<TransHistoryItem> getHistoryItems()
   {
      return historyItems;
   }
}
