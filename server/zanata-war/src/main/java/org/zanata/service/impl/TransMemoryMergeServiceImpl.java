/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.handle.TransMemoryMergeTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TransMemoryUnitDAO;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.type.EntityType;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.service.LocaleService;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.service.TranslationService;
import org.zanata.transaction.TransactionUtil;
import org.zanata.util.TranslationUtil;
import org.zanata.webtrans.server.rpc.TransMemoryMergeStatusResolver;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.search.FilterConstraints;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("transMemoryMergeServiceImpl")
@RequestScoped
@Transactional
public class TransMemoryMergeServiceImpl implements TransMemoryMergeService {
    private static final Logger log = LoggerFactory
            .getLogger(TransMemoryMergeServiceImpl.class);
    private static final String commentPrefix =
            "auto translated by TM merge from";
    private static final int BATCH_SIZE = 50;

    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private TransMemoryUnitDAO transMemoryUnitDAO;
    @Inject
    private TranslationMemoryService translationMemoryServiceImpl;
    @Inject
    private TranslationService translationServiceImpl;
    @Inject
    private Event<TextFlowTargetUpdateContextEvent> textFlowTargetUpdateContextEvent;
    @Inject
    private TransactionUtil transactionUtil;


    @Override
    public List<TranslationService.TranslationResult> executeMerge(
            TransMemoryMergeRequest request,
            TransMemoryMergeTaskHandle asyncTaskHandle) {
        HLocale targetLocale =
                localeServiceImpl.getByLocaleId(request.localeId);

        // get all untranslated text flows
        List<HTextFlow> textFlows = textFlowDAO
                .getAllTextFlowByDocumentIdWithConstraints(
                        request.documentId, targetLocale,
                        FilterConstraints.builder().keepAll().excludeApproved()
                                .excludeFuzzy().excludeTranslated()
                                .excludeRejected().build());
        asyncTaskHandle.setTotalTextFlows(textFlows.size());

        // here we set baseTranslationVersion to 0 because we only target untranslated textflows
        int baseTranslationVersion = 0;
        List<TransUnitUpdateRequest> updateRequests =
                textFlows.stream().map(
                        from -> new TransUnitUpdateRequest(
                                new TransUnitId(from.getId()),
                                null, null, baseTranslationVersion,
                                TranslationSourceType.TM_MERGE.getAbbr()))
                        .collect(Collectors.toList());
        Map<Long, TransUnitUpdateRequest> requestMap =
                transformToMap(updateRequests);
        List<HTextFlow> hTextFlows = textFlowDAO
                .findByIdList(Lists.newArrayList(requestMap.keySet()));
        return fillTextFlowsFromTMResult(request, targetLocale, requestMap,
                hTextFlows
        );
    }

    @Async
    @Override
    public Future<List<TranslationService.TranslationResult>> executeMergeAsync(TransMemoryMergeRequest request,
            TransMemoryMergeTaskHandle asyncTaskHandle) {
        List<TranslationService.TranslationResult> translationResults =
                executeMerge(request, asyncTaskHandle);
        return AsyncTaskResult.taskResult(translationResults);
    }


    private List<TranslationService.TranslationResult> fillTextFlowsFromTMResult(
            TransMemoryMergeRequest action, HLocale targetLocale,
            Map<Long, TransUnitUpdateRequest> requestMap,
            List<HTextFlow> hTextFlows) {
        List<HTextFlow> textFlowBatch = Lists.newLinkedList();
        List<TranslationService.TranslationResult> finalResult = Lists.newLinkedList();

        int index = 0;
        while (index < hTextFlows.size()) {
            HTextFlow hTextFlow = hTextFlows.get(index);
            HTextFlowTarget hTextFlowTarget =
                    hTextFlow.getTargets().get(targetLocale.getId());
            index++;
            // TODO rhbz953734 - TM getUpdateRequests won't override Translated
            // to Approved
            // yet. May or may not want this feature.
            if (hTextFlowTarget != null
                    && hTextFlowTarget.getState().isTranslated()) {
                log.warn("Text flow id {} is translated. Ignored.",
                        hTextFlow.getId());
                continue;
            }

            textFlowBatch.add(hTextFlow);

            if (index % BATCH_SIZE == 0) {
                List<TranslationService.TranslationResult> batchResult =
                        translateInBatch(action, textFlowBatch, targetLocale,
                                requestMap);
                finalResult.addAll(batchResult);
                textFlowBatch.clear();
            }

        }
        List<TranslationService.TranslationResult> batchResult =
                translateInBatch(action, textFlowBatch, targetLocale,
                        requestMap);
        finalResult.addAll(batchResult);

        return finalResult;
    }

    private List<TranslationService.TranslationResult> translateInBatch(
            TransMemoryMergeRequest request, List<HTextFlow> textFlows,
            HLocale targetLocale,
            Map<Long, TransUnitUpdateRequest> requestMap) {

        if (textFlows.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return transactionUtil.call(() -> {
                boolean checkContext =
                        request.getDifferentContextRule() == MergeRule.REJECT;
                boolean checkDocument =
                        request.getDifferentDocumentRule() == MergeRule.REJECT;
                boolean checkProject =
                        request.getDifferentProjectRule() == MergeRule.REJECT;

                List<TransUnitUpdateRequest> updateRequests = Lists.newLinkedList();
                for (HTextFlow hTextFlow : textFlows) {
                    HTextFlowTarget hTextFlowTarget =
                            hTextFlow.getTargets().get(targetLocale.getId());
                    Optional<TransMemoryResultItem> tmResult =
                            translationMemoryServiceImpl.searchBestMatchTransMemory(
                                    hTextFlow, targetLocale.getLocaleId(),
                                    hTextFlow.getDocument().getLocale().getLocaleId(),
                                    checkContext, checkDocument, checkProject,
                                    request.getThresholdPercent());
                    if (tmResult.isPresent()) {
                        TransUnitUpdateRequest updateRequest =
                                createRequest(request, targetLocale, requestMap,
                                        hTextFlow, tmResult.get(), hTextFlowTarget);

                        if (updateRequest != null) {
                            updateRequests.add(updateRequest);
                            textFlowTargetUpdateContextEvent
                                    .fire(new TextFlowTargetUpdateContextEvent(
                                            updateRequest.getTransUnitId(),
                                            request.localeId,
                                            request.editorClientId,
                                            TransUnitUpdated.UpdateType.NonEditorSave));
                        }
                    }
                }
                return translationServiceImpl.translate(
                        request.localeId, updateRequests);
            });
        } catch (Exception e) {
            log.error("exception during TM merge", e);
            return Collections.emptyList();
        }
    }

    private Map<Long, TransUnitUpdateRequest>
            transformToMap(List<TransUnitUpdateRequest> updateRequests) {
        ImmutableMap.Builder<Long, TransUnitUpdateRequest> mapBuilder =
                ImmutableMap.builder();
        for (TransUnitUpdateRequest updateRequest : updateRequests) {
            mapBuilder.put(updateRequest.getTransUnitId().getId(),
                    updateRequest);
        }
        return mapBuilder.build();
    }

    private TransUnitUpdateRequest createRequest(TransMemoryMergeRequest action,
            HLocale hLocale, Map<Long, TransUnitUpdateRequest> requestMap,
            HTextFlow hTextFlowToBeFilled, TransMemoryResultItem tmResult,
            HTextFlowTarget oldTarget) {
        Long tmSourceId = tmResult.getSourceIdList().get(0);
        ContentState statusToSet;
        String comment;
        String revisionComment;
        String entityType;
        Long entityId;
        if (tmResult
                .getMatchType() == TransMemoryResultItem.MatchType.Imported) {
            TransMemoryUnit tu = transMemoryUnitDAO.findById(tmSourceId);
            statusToSet = TransMemoryMergeStatusResolver.newInstance()
                    .decideStatus(action, tmResult, oldTarget);
            comment = buildTargetComment(tu);
            revisionComment = TranslationUtil.getTMMergeMessage(tu);
            entityId = tu.getId();
            entityType = EntityType.TMX.getAbbr();
        } else {
            HTextFlow tmSource = textFlowDAO.findById(tmSourceId, false);
            TransMemoryDetails tmDetail = translationMemoryServiceImpl
                    .getTransMemoryDetail(hLocale, tmSource);
            statusToSet = TransMemoryMergeStatusResolver.newInstance()
                    .decideStatus(action, hTextFlowToBeFilled, tmDetail,
                            tmResult, oldTarget);
            comment = buildTargetComment(tmDetail);
            revisionComment = TranslationUtil.getTMMergeMessage(tmDetail);
            HTextFlowTarget target = tmSource.getTargets().get(hLocale.getId());
            entityId = TranslationUtil.getCopiedEntityId(target);
            entityType = TranslationUtil.getCopiedEntityType(target).getAbbr();
        }
        if (statusToSet != null) {
            TransUnitUpdateRequest unfilledRequest =
                    requestMap.get(hTextFlowToBeFilled.getId());
            TransUnitUpdateRequest request =
                    new TransUnitUpdateRequest(unfilledRequest.getTransUnitId(),
                            tmResult.getTargetContents(), statusToSet,
                            unfilledRequest.getBaseTranslationVersion(),
                            revisionComment, entityId, entityType,
                            TranslationSourceType.TM_MERGE.getAbbr());
            request.addTargetComment(comment);
            log.debug("auto translate from translation memory {}", request);
            return request;
        }
        return null;
    }

    private static String buildTargetComment(TransMemoryDetails tmDetail) {
        return new StringBuilder(commentPrefix).append(" project: ")
                .append(tmDetail.getProjectName()).append(", version: ")
                .append(tmDetail.getIterationName()).append(", DocId: ")
                .append(tmDetail.getDocId()).toString();
    }

    private static String buildTargetComment(TransMemoryUnit tu) {
        return new StringBuilder(commentPrefix).append(" translation memory: ")
                .append(tu.getTranslationMemory().getSlug())
                .append(", unique id: ").append(tu.getUniqueId()).toString();
    }
}
