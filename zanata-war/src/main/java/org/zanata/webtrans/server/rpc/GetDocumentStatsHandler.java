package org.zanata.webtrans.server.rpc;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.service.TranslationStateCache;
import org.zanata.rest.service.StatisticsServiceImpl;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.rpc.GetDocumentStats;
import org.zanata.webtrans.shared.rpc.GetDocumentStatsResult;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Name("webtrans.gwt.GetDocumentStatsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetDocumentStats.class)
public class GetDocumentStatsHandler extends
        AbstractActionHandler<GetDocumentStats, GetDocumentStatsResult> {
    @In
    private StatisticsServiceImpl statisticsServiceImpl;

    @In
    private TranslationStateCache translationStateCacheImpl;

    @Override
    public GetDocumentStatsResult execute(GetDocumentStats action,
            ExecutionContext context) throws ActionException {
        Map<DocumentId, ContainerTranslationStatistics> statsMap =
                new HashMap<DocumentId, ContainerTranslationStatistics>();
        Map<DocumentId, AuditInfo> lastTranslatedMap =
                new HashMap<DocumentId, AuditInfo>();

        for (DocumentId documentId : action.getDocIds()) {
            ContainerTranslationStatistics stats =
                    statisticsServiceImpl.getDocStatistics(documentId.getId(),
                            action.getWorkspaceId().getLocaleId());
            statsMap.put(documentId, stats);

            DocumentStatus docStat =
                    translationStateCacheImpl.getDocumentStatus(documentId
                            .getId(), action.getWorkspaceId().getLocaleId());

            lastTranslatedMap.put(
                    documentId,
                    new AuditInfo(docStat.getLastTranslatedDate(), docStat
                            .getLastTranslatedBy()));
        }
        return new GetDocumentStatsResult(statsMap, lastTranslatedMap);
    }

    @Override
    public void rollback(GetDocumentStats action,
            GetDocumentStatsResult result, ExecutionContext context)
            throws ActionException {

    }
}
