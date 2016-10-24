package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;

public class GetDocumentStatsResult implements DispatchResult {

    private static final long serialVersionUID = 1L;

    private Map<DocumentId, ContainerTranslationStatistics> statsMap;
    private Map<DocumentId, AuditInfo> lastTranslatedMap;

    @SuppressWarnings("unused")
    private GetDocumentStatsResult() {
    }

    public GetDocumentStatsResult(
            Map<DocumentId, ContainerTranslationStatistics> statsMap,
            Map<DocumentId, AuditInfo> lastTranslatedMap) {
        this.statsMap = statsMap;
        this.lastTranslatedMap = lastTranslatedMap;
    }

    public Map<DocumentId, ContainerTranslationStatistics> getStatsMap() {
        return statsMap;
    }

    public Map<DocumentId, AuditInfo> getLastTranslatedMap() {
        return lastTranslatedMap;
    }
}
