package org.zanata.webtrans.server.rpc;

import java.util.List;
import java.util.Map;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetReviewCommentsDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.ReviewCommentId;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("webtrans.gwt.GetTranslationHistoryHandler")
@RequestScoped
@ActionHandlerFor(GetTranslationHistoryAction.class)
public class GetTranslationHistoryHandler extends
        AbstractActionHandler<GetTranslationHistoryAction, GetTranslationHistoryResult> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(GetTranslationHistoryHandler.class);

    @Inject
    private ZanataIdentity identity;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private TextFlowTargetReviewCommentsDAO textFlowTargetReviewCommentsDAO;
    @Inject
    private ResourceUtils resourceUtils;

    @Override
    public GetTranslationHistoryResult execute(
            GetTranslationHistoryAction action, ExecutionContext context)
            throws ActionException {
        identity.checkLoggedIn();
        log.debug("get translation history for text flow id {}",
                action.getTransUnitId());
        HLocale hLocale;
        try {
            hLocale = localeServiceImpl.validateLocaleByProjectIteration(
                    action.getWorkspaceId().getLocaleId(),
                    action.getWorkspaceId().getProjectIterationId()
                            .getProjectSlug(),
                    action.getWorkspaceId().getProjectIterationId()
                            .getIterationSlug());
        } catch (ZanataServiceException e) {
            throw new ActionException(e);
        }
        HTextFlow hTextFlow =
                textFlowDAO.findById(action.getTransUnitId().getId(), false);
        HTextFlowTarget hTextFlowTarget =
                hTextFlow.getTargets().get(hLocale.getId());
        Map<Integer, HTextFlowTargetHistory> history = Maps.newHashMap();
        TransHistoryItem latest = null;
        if (hTextFlowTarget != null) {
            String lastModifiedBy =
                    usernameOrEmptyString(hTextFlowTarget.getLastModifiedBy());
            int nPlurals = resourceUtils.getNumPlurals(hTextFlow.getDocument(),
                    hLocale);
            latest = new TransHistoryItem(
                    hTextFlowTarget.getVersionNum().toString(),
                    GwtRpcUtil.getTargetContentsWithPadding(hTextFlow,
                            hTextFlowTarget, nPlurals),
                    hTextFlowTarget.getState(), lastModifiedBy,
                    hTextFlowTarget.getLastChanged(),
                    hTextFlowTarget.getRevisionComment());
            // history translation
            history = hTextFlowTarget.getHistory();
        }
        Iterable<TransHistoryItem> historyItems =
                Iterables.transform(history.values(),
                        new TargetHistoryToTransHistoryItemFunction());
        log.debug("found {} history for text flow id {}",
                Iterables.size(historyItems), action.getTransUnitId());
        List<ReviewComment> reviewComments = getReviewComments(action);
        log.debug("found {} review comments for text flow id {}",
                reviewComments.size(), action.getTransUnitId());
        // we re-wrap the list because gwt rpc doesn't like other list
        // implementation
        return new GetTranslationHistoryResult(Lists.newArrayList(historyItems),
                latest, Lists.newArrayList(reviewComments));
    }

    private static String usernameOrEmptyString(HPerson lastModifiedBy) {
        return lastModifiedBy != null && lastModifiedBy.hasAccount()
                ? lastModifiedBy.getAccount().getUsername() : "";
    }

    protected List<ReviewComment>
            getReviewComments(GetTranslationHistoryAction action) {
        List<HTextFlowTargetReviewComment> hComments =
                textFlowTargetReviewCommentsDAO.getReviewComments(
                        action.getTransUnitId(),
                        action.getWorkspaceId().getLocaleId());
        return Lists.transform(hComments,
                new Function<HTextFlowTargetReviewComment, ReviewComment>() {

                    @Override
                    public ReviewComment
                            apply(HTextFlowTargetReviewComment input) {
                        return new ReviewComment(
                                new ReviewCommentId(input.getId()),
                                input.getComment(), input.getCommenterName(),
                                input.getCreationDate(),
                                input.getTargetVersion());
                    }
                });
    }

    @Override
    public void rollback(GetTranslationHistoryAction action,
            GetTranslationHistoryResult result, ExecutionContext context)
            throws ActionException {
    }

    private static class TargetHistoryToTransHistoryItemFunction
            implements Function<HTextFlowTargetHistory, TransHistoryItem> {

        @Override
        public TransHistoryItem apply(HTextFlowTargetHistory targetHistory) {
            return new TransHistoryItem(
                    targetHistory.getVersionNum().toString(),
                    targetHistory.getContents(), targetHistory.getState(),
                    usernameOrEmptyString(targetHistory.getLastModifiedBy()),
                    targetHistory.getLastChanged(),
                    targetHistory.getRevisionComment());
        }
    }
}
