package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.common.TranslationStats;
import org.zanata.webtrans.shared.model.DocumentId;


public class GetDocumentStatsResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private Map<DocumentId, TranslationStats> statsMap;

   @SuppressWarnings("unused")
   private GetDocumentStatsResult()
   {
   }

   public GetDocumentStatsResult(Map<DocumentId, TranslationStats> statsMap)
   {
      this.statsMap = statsMap;
   }

   public Map<DocumentId, TranslationStats> getStatsMap()
   {
      return statsMap;
   }
}
