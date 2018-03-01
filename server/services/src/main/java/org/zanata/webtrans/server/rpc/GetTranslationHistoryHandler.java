package org.zanata.webtrans.server.rpc;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.zanata.common.LocaleId;
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
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

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
        // Extract Strings and primitives for getTranslationHistory
        String localeId = action.getWorkspaceId().getLocaleId().toString();
        Long transUnitId = action.getTransUnitId().getId();
        String projectSlug = action.getWorkspaceId().getProjectIterationId()
                .getProjectSlug();
        String versionSlug = action.getWorkspaceId().getProjectIterationId()
                .getIterationSlug();
        GetTranslationHistoryResult result;
        try {
            result = getTranslationHistory(
                    localeId, transUnitId, projectSlug, versionSlug);
        } catch (ZanataServiceException e) {
            throw new ActionException(e);
        }
        return result;
    }

    /*
     * Gets the Translation History Result, shared by both GWT and React Editor
     */
    public GetTranslationHistoryResult getTranslationHistory(String localeId,
            Long transUnitId, String projectSlug, String versionSlug)
            throws ZanataServiceException {
        LocaleId localeID = new LocaleId(localeId);
        log.debug("get translation history for text flow id %d", transUnitId);
        HLocale hLocale = localeServiceImpl.validateLocaleByProjectIteration(
                localeID, projectSlug, versionSlug);
        TransUnitId tUnitId = new TransUnitId(transUnitId);
        HTextFlow hTextFlow = textFlowDAO.findById(transUnitId, false);
        HTextFlowTarget hTextFlowTarget =
                hTextFlow.getTargets().get(hLocale.getId());
        Map<Integer, HTextFlowTargetHistory> history = Maps.newHashMap();
        TransHistoryItem latest =
                getLatest(hTextFlowTarget, hTextFlow, hLocale);
        if (hTextFlowTarget != null) {
            history = hTextFlowTarget.getHistory();
        }
        Iterable<TransHistoryItem> historyItems =
                Iterables.transform(history.values(),
                        new TargetHistoryToTransHistoryItemFunction());
        List<ReviewComment> reviewComments =
                getReviewComments(tUnitId, hLocale);
        // we re-wrap the list because gwt rpc doesn't like other list
        // implementation
        return new GetTranslationHistoryResult(Lists.newArrayList(historyItems),
                latest, Lists.newArrayList(reviewComments));
    }

    private TransHistoryItem getLatest(HTextFlowTarget hTextFlowTarget,
                    HTextFlow hTextFlow, HLocale hLocale) {
        TransHistoryItem latest = null;
        if (hTextFlowTarget != null) {
            String lastModifiedBy = GetTranslationHistoryHandler
                    .usernameOrEmptyString(hTextFlowTarget.getLastModifiedBy());
            int nPlurals = resourceUtils.getNumPlurals(hTextFlow.getDocument(),
                    hLocale);
            latest = new TransHistoryItem(
                    hTextFlowTarget.getVersionNum().toString(),
                    GwtRpcUtil.getTargetContentsWithPadding(hTextFlow,
                            hTextFlowTarget, nPlurals),
                    hTextFlowTarget.getState(), lastModifiedBy,
                    hTextFlowTarget.getLastChanged(),
                    hTextFlowTarget.getRevisionComment());
        }
        return latest;
    }

    protected List<ReviewComment>
    getReviewComments(TransUnitId transUnitId, HLocale hLocale) {
        List<HTextFlowTargetReviewComment> hComments =
                textFlowTargetReviewCommentsDAO.getReviewComments(
                        transUnitId,
                        hLocale.getLocaleId());
        return Lists.transform(hComments, input -> new ReviewComment(
                new ReviewCommentId(input.getId()),
                input.getCommentText(), input.getCommenterName(),
                input.getCreationDate()
        ));
    }

    private static String usernameOrEmptyString(HPerson lastModifiedBy) {
        return lastModifiedBy != null && lastModifiedBy.hasAccount()
                ? lastModifiedBy.getAccount().getUsername() : "";
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
