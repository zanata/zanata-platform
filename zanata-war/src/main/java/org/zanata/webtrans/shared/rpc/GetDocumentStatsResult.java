package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.common.CommonContainerTranslationStatistics;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;


public class GetDocumentStatsResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private Map<DocumentId, CommonContainerTranslationStatistics> statsMap;
   private Map<DocumentId, AuditInfo> lastTranslatedMap;

   @SuppressWarnings("unused")
   private GetDocumentStatsResult()
   {
   }

   public GetDocumentStatsResult(Map<DocumentId, CommonContainerTranslationStatistics> statsMap, Map<DocumentId, AuditInfo> lastTranslatedMap)
   {
      this.statsMap = statsMap;
      this.lastTranslatedMap = lastTranslatedMap;
   }

   public Map<DocumentId, CommonContainerTranslationStatistics> getStatsMap()
   {
      return statsMap;
   }

   public Map<DocumentId, AuditInfo> getLastTranslatedMap()
   {
      return lastTranslatedMap;
   }
}
