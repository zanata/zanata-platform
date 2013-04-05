package org.zanata.webtrans.server.rpc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.TranslationStats;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.impl.StatisticsServiceImpl;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.rpc.GetDocumentStats;
import org.zanata.webtrans.shared.rpc.GetDocumentStatsResult;

@Name("webtrans.gwt.GetDocumentStatsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetDocumentStats.class)
public class GetDocumentStatsHandler extends AbstractActionHandler<GetDocumentStats, GetDocumentStatsResult>
{
   @In
   private StatisticsServiceImpl statisticsServiceImpl;

   @In
   private TranslationStateCache translationStateCacheImpl;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;
   
   @In
   private DocumentDAO documentDAO;
   
   // TODO: fix problem with multiple RPC call from single client, client do single RPC call one at a time to solve this problem.
   private boolean USE_CACHE = true;

   @Override
   public GetDocumentStatsResult execute(GetDocumentStats action, ExecutionContext context) throws ActionException
   {
      Map<DocumentId, TranslationStats> statsMap = new HashMap<DocumentId, TranslationStats>();
      Map<DocumentId, AuditInfo> lastTranslatedMap = new HashMap<DocumentId, AuditInfo>();

      for (DocumentId documentId : action.getDocIds())
      {
         TranslationStats stats = statisticsServiceImpl.getDocStatistics(documentId.getId(), action.getWorkspaceId().getLocaleId());
         statsMap.put(documentId, stats);

         Long id = null;
         
         if (USE_CACHE)
         {
            id = translationStateCacheImpl.getDocLastTranslatedTextFlowTarget(documentId.getId(), action.getWorkspaceId().getLocaleId());
         }
         else
         {
            id = documentDAO.getLastTranslatedTargetId(documentId.getId(), action.getWorkspaceId().getLocaleId());
         }
         
         Date lastTranslatedDate = null;
         String lastTranslatedBy = "";

         if (id != null)
         {
            HTextFlowTarget target = textFlowTargetDAO.findById(id, false);

            if (target != null)
            {
               lastTranslatedDate = target.getLastChanged();

               if (target.getLastModifiedBy() != null)
               {
                  lastTranslatedBy = target.getLastModifiedBy().getAccount().getUsername();
               }
            }
         }
         lastTranslatedMap.put(documentId, new AuditInfo(lastTranslatedDate, lastTranslatedBy));
      }
      return new GetDocumentStatsResult(statsMap, lastTranslatedMap);
   }

   @Override
   public void rollback(GetDocumentStats action, GetDocumentStatsResult result, ExecutionContext context) throws ActionException
   {

   }
}