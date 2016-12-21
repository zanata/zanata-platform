/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.service.impl;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.service.SecurityService.TranslationAction.MODIFY;
import static org.zanata.webtrans.shared.model.TransMemoryResultItem.MatchType;
import static org.zanata.webtrans.shared.rpc.HasSearchType.SearchType.FUZZY_PLURAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.customware.gwt.dispatch.shared.ActionException;

import org.hamcrest.Matchers;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TransMemoryUnitDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.TestFixture;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.service.LocaleService;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.service.TranslationService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasSearchType;
import org.zanata.webtrans.shared.rpc.MergeOptions;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class TransMemoryMergeServiceImplTest {

    @Inject
    TransMemoryMergeServiceImpl transMemoryMergeService;

    @Produces @Mock
    private SecurityService securityService;
    @Produces @Mock
    private LocaleService localeService;
    @Produces @Mock
    private TextFlowDAO textFlowDAO;
    @Produces @Mock
    private TransMemoryUnitDAO transMemoryUnitDAO;
    @Produces @Mock
    private TranslationMemoryService translationMemoryService;
    @Produces @Mock
    private TranslationService translationService;
    @Produces @Mock
    private TranslationWorkspace workspace;
    @Captor
    ArgumentCaptor<List<TransUnitUpdateRequest>> updateRequestCaptor;

    private String projectSlug = "projectSlug";
    private String versionSlug = "versionSlug";
    private String docId = "pot/a.po";

    private HLocale targetLocale = new HLocale(new LocaleId("de"));
    private HLocale sourceLocale = new HLocale(new LocaleId("en-US"));

    private static ArrayList<String> tmSource = newArrayList("tm source");
    private static ArrayList<String> tmTarget = newArrayList("tm target");

    private TransMemoryMerge prepareAction(int threshold,
            List<TransUnitUpdateRequest> requests, MergeOptions opts) {
        TransMemoryMerge action =
                new TransMemoryMerge(threshold, requests, opts);
        action.setWorkspaceId(new WorkspaceId(new ProjectIterationId(
                projectSlug, versionSlug, ProjectType.File), targetLocale
                .getLocaleId()));
        return action;
    }

    private TransMemoryMerge prepareAction(int threshold, long... tranUnitIds)
            throws NoSuchWorkspaceException {
        return prepareAction(threshold, true, tranUnitIds);
    }

    private TransMemoryMerge prepareAction(int threshold,
            boolean acceptImportedTMResults, long... tranUnitIds)
            throws NoSuchWorkspaceException {
        List<TransUnitUpdateRequest> requests = newArrayList();
        for (long tranUnitId : tranUnitIds) {
            requests.add(new TransUnitUpdateRequest(
                    new TransUnitId(tranUnitId), null, null, 0,
                TranslationSourceType.TM_MERGE.getAbbr()));
        }
        // we have TransMemoryMergeStatusResolverTest to cover various different
        // merge options so here we don't test that
        MergeRule importedTMOption =
                acceptImportedTMResults ? MergeRule.IGNORE_CHECK
                        : MergeRule.REJECT;
        MergeOptions opts = MergeOptions.allIgnore();
        opts.setImportedMatch(importedTMOption);

        return prepareAction(threshold, requests, opts);
    }

    private static TransMemoryResultItem tmResult(Long sourceId, int percent) {
        TransMemoryResultItem resultItem =
                new TransMemoryResultItem(tmSource, tmTarget,
                        MatchType.ApprovedInternal, 1D, percent);
        resultItem.addSourceId(sourceId);
        return resultItem;
    }

    private static TransMemoryResultItem importedTmResult(Long sourceId,
            int percent) {
        TransMemoryResultItem resultItem =
                new TransMemoryResultItem(tmSource, tmTarget,
                        MatchType.Imported, 1D, percent);
        resultItem.addSourceId(sourceId);
        return resultItem;
    }

    private static TransMemoryDetails tmDetail() {
        return new TransMemoryDetails("", "", "project a", "master",
                "pot/msg.pot", "resId", null, null, null, null, null);
    }

    private TransMemoryQuery
            prepareTMQuery(List<String> contents,
                    HasSearchType.SearchType searchType, MergeOptions opts,
                    String resId) {

        if (opts == null) {
            return new TransMemoryQuery(contents, searchType,
                    new TransMemoryQuery.Condition(false, projectSlug),
                    new TransMemoryQuery.Condition(false, docId),
                    new TransMemoryQuery.Condition(false, resId));
        } else {
            TransMemoryQuery.Condition projectCondition =
                    new TransMemoryQuery.Condition(
                            opts.getDifferentProject() == MergeRule.REJECT,
                            projectSlug);

            TransMemoryQuery.Condition documentCondition =
                    new TransMemoryQuery.Condition(
                            opts.getDifferentDocument() == MergeRule.REJECT,
                            docId);

            TransMemoryQuery.Condition resCondition =
                    new TransMemoryQuery.Condition(
                            opts.getDifferentResId() == MergeRule.REJECT, resId);

            return new TransMemoryQuery(contents, searchType, projectCondition,
                    documentCondition, resCondition);
        }
    }

    @Test
    @InRequestScope
    public void willTranslateIfMatches() throws ActionException {
        // Given:
        // an action with threshold 80% and trans unit id is 1
        final long transUnitId = 1L;
        TransMemoryMerge action = prepareAction(80, transUnitId);

        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));
        // Given: there are three TM results returned for text flow id 1, and
        // the most matched one is text flow id 11
        HTextFlow tmResultSource =
                TestFixture.makeApprovedHTextFlow(11L, targetLocale);
        TransMemoryResultItem mostSimilarTM =
                tmResult(tmResultSource.getId(), 100);

        when(textFlowDAO.findById(tmResultSource.getId(), false)).thenReturn(
                tmResultSource);

        when(localeService.getByLocaleId(action.getWorkspaceId().getLocaleId()))
                .thenReturn(targetLocale);

        when(
                translationMemoryService.getTransMemoryDetail(targetLocale,
                        tmResultSource)).thenReturn(tmDetail());

        Optional<TransMemoryResultItem> matches = Optional.of(mostSimilarTM);

        when(
                translationMemoryService.searchBestMatchTransMemory(hTextFlow,
                        targetLocale.getLocaleId(), sourceLocale.getLocaleId(),
                        false, false, false, action.getThresholdPercent()))
                .thenReturn(matches);

        // When: execute the action
        transMemoryMergeService.executeMerge(action);

        // Then:
        verify(securityService).checkWorkspaceAction(action.getWorkspaceId(),
                MODIFY);

        // we should have text flow auto translated by using the most
        // similar TM
        verify(translationService).translate(same(targetLocale.getLocaleId()),
                updateRequestCaptor.capture());

        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest, Matchers.hasSize(1));
        TransUnitUpdateRequest transUnitUpdateRequest = updateRequest.get(0);
        assertThat(transUnitUpdateRequest.getNewContents(),
                Matchers.equalTo(mostSimilarTM.getTargetContents()));
        assertThat(transUnitUpdateRequest.getSourceType(),
            Matchers.equalTo(TranslationSourceType.TM_MERGE.getAbbr()));
        assertThat(
                transUnitUpdateRequest.getTargetComment(),
                Matchers.equalTo("auto translated by TM merge from project: project a, version: master, DocId: pot/msg.pot"));
    }

    @Test
    @InRequestScope
    public void willNotTranslateIfNoMatches() throws ActionException {
        final long transUnitId = 1L;
        TransMemoryMerge action = prepareAction(80, transUnitId);

        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));

        Optional<TransMemoryResultItem> matches =
                Optional.empty();

        when(
                translationMemoryService.searchBestMatchTransMemory(hTextFlow,
                        targetLocale.getLocaleId(), sourceLocale.getLocaleId(),
                        false, false, false, action.getThresholdPercent()))
                .thenReturn(matches);

        when(localeService.getByLocaleId(action.getWorkspaceId().getLocaleId()))
                .thenReturn(targetLocale);

        // When: execute the action
        transMemoryMergeService.executeMerge(action);

        verify(securityService).checkWorkspaceAction(action.getWorkspaceId(),
                MODIFY);

        // Then: we should have EMPTY trans unit update request
        verifyZeroInteractions(translationService);
    }

    @Test
    @InRequestScope
    public void canHandleMultipleTextFlows() throws ActionException {
        // Given: an action with threshold 90% and trans unit id is 1, 2, 3, 4
        final long idWith100MatchTM = 1L;
        final long idWithoutTM = 2L;
        final long idWith80MatchTM = 3L;
        final long idWith90MatchTM = 4L;
        TransMemoryMerge action =
                prepareAction(90, idWith100MatchTM, idWithoutTM,
                        idWith80MatchTM, idWith90MatchTM);

        HTextFlow textFlow100TM =
                TestFixture.makeHTextFlow(idWith100MatchTM, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        HTextFlow textFlowNoTM =
                TestFixture.makeHTextFlow(idWithoutTM, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);
        HTextFlow textFlow80TM =
                TestFixture.makeHTextFlow(idWith80MatchTM, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);
        HTextFlow textFLow90TM =
                TestFixture.makeHTextFlow(idWith90MatchTM, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        when(localeService.getByLocaleId(action.getWorkspaceId().getLocaleId()))
                .thenReturn(targetLocale);

        when(
                textFlowDAO.findByIdList(newArrayList(idWith100MatchTM,
                        idWithoutTM, idWith80MatchTM, idWith90MatchTM)))
                .thenReturn(
                        newArrayList(textFlow100TM, textFlowNoTM, textFlow80TM,
                                textFLow90TM));
        // Given: TM results
        HTextFlow tmResultSource =
                TestFixture.makeApprovedHTextFlow(11L, targetLocale);

        Optional<TransMemoryResultItem> tm100 =
                Optional.of(tmResult(tmResultSource.getId(), 100));
        Optional<TransMemoryResultItem> tm90 =
                Optional.of(tmResult(tmResultSource.getId(), 90));
        Optional<TransMemoryResultItem> tm80 =
                Optional.of(tmResult(tmResultSource.getId(), 80));
        Optional<TransMemoryResultItem> noMatch = Optional.empty();

        when(
                translationMemoryService.searchBestMatchTransMemory(
                        textFlow100TM, targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), false, false, false, 90))
                .thenReturn(tm100);
        when(
                translationMemoryService.searchBestMatchTransMemory(
                        textFLow90TM, targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), false, false, false, 90))
                .thenReturn(tm90);
        when(
                translationMemoryService.searchBestMatchTransMemory(
                        textFlow80TM, targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), false, false, false, 90))
                .thenReturn(tm80);
        when(
                translationMemoryService.searchBestMatchTransMemory(
                        textFlowNoTM, targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), false, false, false, 90))
                .thenReturn(noMatch);

        when(textFlowDAO.findById(tmResultSource.getId(), false)).thenReturn(
                tmResultSource);
        // Given: tm detail of text flow id 11
        when(
                translationMemoryService.getTransMemoryDetail(targetLocale,
                        tmResultSource)).thenReturn(tmDetail());

        // When: execute the action
        transMemoryMergeService.executeMerge(action);

        verify(securityService).checkWorkspaceAction(action.getWorkspaceId(),
                MODIFY);

        // Then: we should have text flow auto translated by using the most
        // similar TM
        verify(translationService).translate(
                same(action.getWorkspaceId().getLocaleId()),
                updateRequestCaptor.capture());

        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest, Matchers.hasSize(3));
        assertThat(updateRequest.get(0).getNewContents(),
                Matchers.equalTo(tm100.get().getTargetContents()));
        assertThat(updateRequest.get(1).getNewContents(),
                Matchers.equalTo(tm90.get().getTargetContents()));
        assertThat(updateRequest.get(2).getNewContents(),
                Matchers.equalTo(tm80.get().getTargetContents()));

        assertThat(updateRequest.get(0).getSourceType(),
            Matchers.equalTo(TranslationSourceType.TM_MERGE.getAbbr()));
        assertThat(updateRequest.get(1).getSourceType(),
            Matchers.equalTo(TranslationSourceType.TM_MERGE.getAbbr()));
        assertThat(updateRequest.get(2).getSourceType(),
            Matchers.equalTo(TranslationSourceType.TM_MERGE.getAbbr()));
    }

    @Test
    @InRequestScope
    public void canAutoTranslateImportedTMResults() throws Exception {
        // Given:
        // an action with threshold 80% and trans unit id
        final long transUnitId = 1L;
        TransMemoryMerge action = prepareAction(80, transUnitId);

        // A Text Flow to be translated
        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        // A matching imported Translation Unit
        TransMemoryUnit tuResultSource =
                TestFixture.makeTransMemoryUnit(10L, targetLocale);

        // and an associated result item
        TransMemoryResultItem mostSimilarTM =
                importedTmResult(tuResultSource.getId(), 100);

        Optional<TransMemoryResultItem> match = Optional.of(mostSimilarTM);

        // A Translation memory query
        TransMemoryQuery tmQuery =
                prepareTMQuery(hTextFlow.getContents(), FUZZY_PLURAL, null,
                        hTextFlow.getResId());

        // Expectations:
        when(localeService.getByLocaleId(action.getWorkspaceId().getLocaleId()))
                .thenReturn(targetLocale);

        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));

        when(
                translationMemoryService.searchBestMatchTransMemory(hTextFlow,
                        targetLocale.getLocaleId(), sourceLocale.getLocaleId(),
                        false, false, false, action.getThresholdPercent()))
                .thenReturn(match);
        when(transMemoryUnitDAO.findById(tuResultSource.getId())).thenReturn(
                tuResultSource);

        // When: execute the action
        transMemoryMergeService.executeMerge(action);

        // Then: we should have text flow auto translated by using the most
        // similar TM
        verify(translationService).translate(
                same(action.getWorkspaceId().getLocaleId()),
                updateRequestCaptor.capture());

        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest, Matchers.hasSize(1));
        TransUnitUpdateRequest transUnitUpdateRequest = updateRequest.get(0);
        assertThat(transUnitUpdateRequest.getNewContents(),
                Matchers.equalTo(mostSimilarTM.getTargetContents()));
        assertThat(
                transUnitUpdateRequest.getTargetComment(),
                Matchers.equalTo("auto translated by TM merge from translation memory: test-tm, unique id: uid10"));
        assertThat(transUnitUpdateRequest.getSourceType(),
            Matchers.equalTo(TranslationSourceType.TM_MERGE.getAbbr()));
    }
}
