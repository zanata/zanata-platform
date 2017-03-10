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
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
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
import org.zanata.service.SecurityService;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.service.TranslationService;
import javax.enterprise.event.Event;
import org.zanata.util.TranslationUtil;
import org.zanata.webtrans.server.rpc.TransMemoryMergeStatusResolver;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.customware.gwt.dispatch.shared.ActionException;
import static org.zanata.service.SecurityService.TranslationAction.MODIFY;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("transMemoryMergeServiceImpl")
@RequestScoped
@Transactional
public class TransMemoryMergeServiceImpl implements TransMemoryMergeService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TransMemoryMergeServiceImpl.class);

    @Inject
    private SecurityService securityServiceImpl;
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
    private static final String commentPrefix =
            "auto translated by TM merge from";

    @Override
    public List<TranslationService.TranslationResult>
            executeMerge(TransMemoryMerge action) throws ActionException {
        HLocale targetLocale = localeServiceImpl
                .getByLocaleId(action.getWorkspaceId().getLocaleId());
        securityServiceImpl.checkWorkspaceAction(action.getWorkspaceId(),
                MODIFY);
        Map<Long, TransUnitUpdateRequest> requestMap =
                transformToMap(action.getUpdateRequests());
        List<HTextFlow> hTextFlows = textFlowDAO
                .findByIdList(Lists.newArrayList(requestMap.keySet()));
        List<TransUnitUpdateRequest> updateRequests = Lists.newArrayList();
        for (HTextFlow hTextFlow : hTextFlows) {
            HTextFlowTarget hTextFlowTarget =
                    hTextFlow.getTargets().get(targetLocale.getId());
            // TODO rhbz953734 - TM getUpdateRequests won't override Translated
            // to Approved
            // yet. May or may not want this feature.
            if (hTextFlowTarget != null
                    && hTextFlowTarget.getState().isTranslated()) {
                log.warn("Text flow id {} is translated. Ignored.",
                        hTextFlow.getId());
                continue;
            }
            boolean checkContext =
                    action.getDifferentContextRule() == MergeRule.REJECT;
            boolean checkDocument =
                    action.getDifferentDocumentRule() == MergeRule.REJECT;
            boolean checkProject =
                    action.getDifferentProjectRule() == MergeRule.REJECT;
            Optional<TransMemoryResultItem> tmResult =
                    translationMemoryServiceImpl.searchBestMatchTransMemory(
                            hTextFlow, targetLocale.getLocaleId(),
                            hTextFlow.getDocument().getLocale().getLocaleId(),
                            checkContext, checkDocument, checkProject,
                            action.getThresholdPercent());
            if (tmResult.isPresent()) {
                TransUnitUpdateRequest request =
                        createRequest(action, targetLocale, requestMap,
                                hTextFlow, tmResult.get(), hTextFlowTarget);
                updateRequests.add(request);
            }
        }
        if (updateRequests.isEmpty()) {
            return Collections.emptyList();
        }
        for (TransUnitUpdateRequest updateRequest : updateRequests) {
            textFlowTargetUpdateContextEvent
                    .fire(new TextFlowTargetUpdateContextEvent(
                            updateRequest.getTransUnitId(),
                            action.getWorkspaceId().getLocaleId(),
                            action.getEditorClientId(),
                            TransUnitUpdated.UpdateType.NonEditorSave));
        }
        return translationServiceImpl.translate(
                action.getWorkspaceId().getLocaleId(), updateRequests);
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

    private TransUnitUpdateRequest createRequest(TransMemoryMerge action,
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
