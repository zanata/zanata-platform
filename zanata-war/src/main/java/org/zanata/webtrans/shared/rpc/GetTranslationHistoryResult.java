package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GetTranslationHistoryResult implements DispatchResult {
    private List<TransHistoryItem> historyItems = Lists.newArrayList();
    private List<ReviewComment> reviewComments = Lists.newArrayList();
    private TransHistoryItem latest;

    @SuppressWarnings("unused")
    private GetTranslationHistoryResult() {
    }

    public GetTranslationHistoryResult(List<TransHistoryItem> historyItems,
            TransHistoryItem latest, List<ReviewComment> reviewComments) {
        this.latest = latest;
        this.historyItems = historyItems;
        this.reviewComments = reviewComments;
    }

    public List<TransHistoryItem> getHistoryItems() {
        return historyItems;
    }

    public TransHistoryItem getLatest() {
        return latest;
    }

    public List<ReviewComment> getReviewComments() {
        return reviewComments;
    }
}
