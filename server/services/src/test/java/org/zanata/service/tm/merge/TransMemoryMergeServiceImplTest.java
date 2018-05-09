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

package org.zanata.service.tm.merge;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.service.tm.merge.TransMemoryMergeServiceImpl.BATCH_SIZE;
import static org.zanata.test.EntityTestData.makeApprovedHTextFlow;
import static org.zanata.test.EntityTestData.makeHTextFlow;
import static org.zanata.test.EntityTestData.makeTransMemoryUnit;
import static org.zanata.webtrans.shared.model.TransMemoryResultItem.MatchType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.async.handle.TransMemoryMergeTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.config.TMBands;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TransMemoryUnitDAO;
import org.zanata.email.HtmlEmailBuilder;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LocaleService;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.service.TranslationService;
import org.zanata.service.VersionStateCache;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.test.CdiUnitRunner;
import org.zanata.transaction.TransactionUtil;
import org.zanata.transaction.TransactionUtilForUnitTest;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rest.dto.InternalTMSource;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeOptions;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.search.FilterConstraints;

import com.google.common.collect.Lists;

import kotlin.ranges.IntRange;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class TransMemoryMergeServiceImplTest {

    @Inject
    private TransMemoryMergeServiceImpl transMemoryMergeService;

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

    @Produces @Mock
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Produces @Authenticated @Mock
    private HAccount authenticated;
    @Captor
    private ArgumentCaptor<List<TransUnitUpdateRequest>> updateRequestCaptor;

    @Produces
    private TransactionUtil transactionUtil = new TransactionUtilForUnitTest(null);

    @Produces @Mock
    private VersionStateCache versionStateCacheImpl;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private ProjectIterationDAO projectIterationDAO;
    @Produces
    @ServerPath
    private String serverPath = "http://example.com/";
    @Produces @TMBands
    private Map<ContentState, List<IntRange>> bands = new TMBandDefsProducer().produce("80 90");
    @Produces @Mock
    private HtmlEmailBuilder emailBuilder;
    @Produces @Mock
    private Messages messages;

    private String projectSlug = "projectSlug";
    private String versionSlug = "versionSlug";
    private String docId = "pot/a.po";

    private HLocale targetLocale = new HLocale(new LocaleId("de"));
    private HLocale sourceLocale = new HLocale(new LocaleId("en-US"));

    private static ArrayList<String> tmSource = newArrayList("tm source");
    private static ArrayList<String> tmTarget = newArrayList("tm target");
    private TransMemoryMergeTaskHandle asyncTaskHandle;
    private ProjectIterationId projectIterationId = new ProjectIterationId(
            projectSlug, versionSlug, ProjectType.File);
    private final DocumentId documentId = new DocumentId(1L, docId);
    private final EditorClientId editorClientId =
            new EditorClientId("sessionId", 1);
    private FilterConstraints untranslatedFilter;
    private InternalTMSource fromVersions;

    private TransMemoryMergeRequest prepareAction(int threshold, MergeOptions opts) {
        LocaleId localeId = targetLocale.getLocaleId();
        TransMemoryMergeRequest action =
                new TransMemoryMergeRequest(
                        editorClientId, projectIterationId,
                        documentId, localeId, threshold,
                        opts.getDifferentProject(), opts.getDifferentDocument(),
                        opts.getDifferentResId(), opts.getImportedMatch());
        return action;
    }

    private TransMemoryMergeRequest prepareAction(int threshold)
            throws NoSuchWorkspaceException {
        return prepareAction(threshold, true);
    }

    private TransMemoryMergeRequest prepareAction(int threshold,
            boolean acceptImportedTMResults) {
        // we have TransMemoryMergeStatusResolverTest to cover various different
        // merge options so here we don't test that
        MergeRule importedTMOption =
                acceptImportedTMResults ? MergeRule.IGNORE_CHECK
                        : MergeRule.REJECT;
        MergeOptions opts = MergeOptions.allIgnore();
        opts.setImportedMatch(importedTMOption);

        return prepareAction(threshold, opts);
    }

    private static TransMemoryResultItem tmResult(Long sourceId, int percent) {
        TransMemoryResultItem resultItem =
                new TransMemoryResultItem(tmSource, tmTarget,
                        MatchType.ApprovedInternal, 1D, percent, 1L);
        resultItem.addSourceId(sourceId);
        return resultItem;
    }

    private static TransMemoryResultItem importedTmResult(Long sourceId,
            int percent) {
        TransMemoryResultItem resultItem =
                new TransMemoryResultItem(tmSource, tmTarget,
                        MatchType.Imported, 1D, percent, 1L);
        resultItem.addSourceId(sourceId);
        return resultItem;
    }

    private static TransMemoryDetails tmDetail() {
        return new TransMemoryDetails("", "", "project a", "master",
                "pot/msg.pot", "resId", null, null, null, null, null);
    }

    @Before
    public void setUp() throws NoSuchWorkspaceException {
        MockitoAnnotations.initMocks(this);
        fromVersions = InternalTMSource.SELECT_ALL;
        asyncTaskHandle = new TransMemoryMergeTaskHandle();
        WorkspaceId workspaceId =
                new WorkspaceId(projectIterationId, targetLocale.getLocaleId());
        when(translationWorkspaceManager.getOrRegisterWorkspace(
                workspaceId)).thenReturn(workspace);
        untranslatedFilter = FilterConstraints.builder().keepAll().excludeApproved()
                .excludeFuzzy().excludeTranslated()
                .excludeRejected().build();
    }

    @Test
    @InRequestScope
    public void willTranslateIfMatches() throws ActionException {
        // Given:
        // an action with threshold 80% and trans unit id is 1
        final long transUnitId = 1L;
        TransMemoryMergeRequest action = prepareAction(80);

        HTextFlow hTextFlow =
                makeHTextFlow(transUnitId, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        // Given: there are three TM results returned for text flow id 1, and
        // the most matched one is text flow id 11
        HTextFlow tmResultSource =
                makeApprovedHTextFlow(11L, targetLocale);
        TransMemoryResultItem mostSimilarTM =
                tmResult(tmResultSource.getId(), 100);

        when(localeService.getByLocaleId(action.localeId))
                .thenReturn(targetLocale);
        List<HTextFlow> untranslated = Lists.newArrayList(hTextFlow);
        when(textFlowDAO.getUntranslatedTextFlowCount(documentId, targetLocale))
                .thenReturn(Long.valueOf(untranslated.size()));
        when(textFlowDAO.getTextFlowByDocumentIdWithConstraints(documentId,
                targetLocale, untranslatedFilter, 0, BATCH_SIZE))
                        .thenReturn(untranslated);
        when(textFlowDAO.findById(tmResultSource.getId(), false)).thenReturn(
                tmResultSource);

        when(
                translationMemoryService.getTransMemoryDetail(targetLocale,
                        tmResultSource)).thenReturn(tmDetail());

        Optional<TransMemoryResultItem> matches = Optional.of(mostSimilarTM);

        when(
                translationMemoryService.searchBestMatchTransMemory(hTextFlow,
                        targetLocale.getLocaleId(), sourceLocale.getLocaleId(),
                        false, false, false, action.getThresholdPercent(),
                        fromVersions))
                .thenReturn(matches);

        // When: execute the action
        transMemoryMergeService.executeMerge(action, asyncTaskHandle);

        // Then:
        // we should have text flow auto translated by using the most
        // similar TM
        verify(translationService).translate(same(targetLocale.getLocaleId()),
                updateRequestCaptor.capture());

        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest).hasSize(1);
        TransUnitUpdateRequest transUnitUpdateRequest = updateRequest.get(0);
        assertThat(transUnitUpdateRequest.getNewContents())
                .isEqualTo(mostSimilarTM.getTargetContents());
        assertThat(transUnitUpdateRequest.getSourceType())
            .isEqualTo(TranslationSourceType.TM_MERGE.getAbbr());
        assertThat(
                transUnitUpdateRequest.getTargetComment())
                .isEqualTo(
                        "auto translated by TM merge from project: project a, version: master, DocId: pot/msg.pot");
    }

    @Test
    @InRequestScope
    public void willNotTranslateIfNoMatches() throws ActionException {
        final long transUnitId = 1L;
        TransMemoryMergeRequest action = prepareAction(80);

        HTextFlow hTextFlow =
                makeHTextFlow(transUnitId, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));

        Optional<TransMemoryResultItem> matches =
                Optional.empty();

        when(
                translationMemoryService.searchBestMatchTransMemory(hTextFlow,
                        targetLocale.getLocaleId(), sourceLocale.getLocaleId(),
                        false, false, false, action.getThresholdPercent(),
                        fromVersions))
                .thenReturn(matches);

        when(localeService.getByLocaleId(action.localeId))
                .thenReturn(targetLocale);

        // When: execute the action
        transMemoryMergeService.executeMerge(action, asyncTaskHandle);

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
        TransMemoryMergeRequest action =
                prepareAction(90
                );

        HTextFlow textFlow100TM =
                makeHTextFlow(idWith100MatchTM, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        HTextFlow textFlowNoTM =
                makeHTextFlow(idWithoutTM, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);
        HTextFlow textFlow80TM =
                makeHTextFlow(idWith80MatchTM, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);
        HTextFlow textFLow90TM =
                makeHTextFlow(idWith90MatchTM, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        when(localeService.getByLocaleId(action.localeId))
                .thenReturn(targetLocale);

        List<HTextFlow> untranslated = Lists.newArrayList(textFlow80TM, textFLow90TM, textFlow100TM, textFlowNoTM);
        when(textFlowDAO.getUntranslatedTextFlowCount(documentId, targetLocale))
                .thenReturn(Long.valueOf(untranslated.size()));
        when(textFlowDAO.getTextFlowByDocumentIdWithConstraints(documentId,
                targetLocale, untranslatedFilter, 0, BATCH_SIZE))
                .thenReturn(untranslated);

        // Given: TM results
        HTextFlow tmResultSource =
                makeApprovedHTextFlow(11L, targetLocale);

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
                        sourceLocale.getLocaleId(), false, false, false, 90,
                        fromVersions))
                .thenReturn(tm100);
        when(
                translationMemoryService.searchBestMatchTransMemory(
                        textFLow90TM, targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), false, false, false, 90,
                        fromVersions))
                .thenReturn(tm90);
        when(
                translationMemoryService.searchBestMatchTransMemory(
                        textFlow80TM, targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), false, false, false, 90,
                        fromVersions))
                .thenReturn(tm80);
        when(
                translationMemoryService.searchBestMatchTransMemory(
                        textFlowNoTM, targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), false, false, false, 90,
                        fromVersions))
                .thenReturn(noMatch);

        when(textFlowDAO.findById(tmResultSource.getId(), false)).thenReturn(
                tmResultSource);
        // Given: tm detail of text flow id 11
        when(
                translationMemoryService.getTransMemoryDetail(targetLocale,
                        tmResultSource)).thenReturn(tmDetail());

        // When: execute the action
        transMemoryMergeService.executeMerge(action, asyncTaskHandle);

        // Then: we should have text flow auto translated by using the most
        // similar TM
        verify(translationService).translate(
                same(action.localeId),
                updateRequestCaptor.capture());

        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest).hasSize(3);
        assertThat(updateRequest.get(0).getNewContents())
                .isEqualTo(tm100.get().getTargetContents());
        assertThat(updateRequest.get(1).getNewContents())
                .isEqualTo(tm90.get().getTargetContents());
        assertThat(updateRequest.get(2).getNewContents())
                .isEqualTo(tm80.get().getTargetContents());

        assertThat(updateRequest.get(0).getSourceType())
                .isEqualTo(TranslationSourceType.TM_MERGE.getAbbr());
        assertThat(updateRequest.get(1).getSourceType())
                .isEqualTo(TranslationSourceType.TM_MERGE.getAbbr());
        assertThat(updateRequest.get(2).getSourceType())
                .isEqualTo(TranslationSourceType.TM_MERGE.getAbbr());
    }

    @Test
    @InRequestScope
    public void canAutoTranslateImportedTMResults() throws Exception {
        // Given:
        // an action with threshold 80% and trans unit id
        final long transUnitId = 1L;
        TransMemoryMergeRequest action = prepareAction(80);

        // A Text Flow to be translated
        HTextFlow hTextFlow =
                makeHTextFlow(transUnitId, sourceLocale,
                        targetLocale, ContentState.New, docId, versionSlug,
                        projectSlug);

        // A matching imported Translation Unit
        TransMemoryUnit tuResultSource =
                makeTransMemoryUnit(10L, targetLocale);

        // and an associated result item
        TransMemoryResultItem mostSimilarTM =
                importedTmResult(tuResultSource.getId(), 100);

        Optional<TransMemoryResultItem> match = Optional.of(mostSimilarTM);

        // Expectations:
        when(localeService.getByLocaleId(action.localeId))
                .thenReturn(targetLocale);

        List<HTextFlow> untranslated = Lists.newArrayList(hTextFlow);
        when(textFlowDAO.getUntranslatedTextFlowCount(documentId, targetLocale))
                .thenReturn(Long.valueOf(untranslated.size()));
        when(textFlowDAO.getTextFlowByDocumentIdWithConstraints(documentId,
                targetLocale, untranslatedFilter, 0, BATCH_SIZE))
                .thenReturn(untranslated);

        when(
                translationMemoryService.searchBestMatchTransMemory(hTextFlow,
                        targetLocale.getLocaleId(), sourceLocale.getLocaleId(),
                        false, false, false, action.getThresholdPercent(),
                        fromVersions))
                .thenReturn(match);
        when(transMemoryUnitDAO.findById(tuResultSource.getId())).thenReturn(
                tuResultSource);

        // When: execute the action
        transMemoryMergeService.executeMerge(action, asyncTaskHandle);

        // Then: we should have text flow auto translated by using the most
        // similar TM
        verify(translationService).translate(
                same(action.localeId),
                updateRequestCaptor.capture());

        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest).hasSize(1);
        TransUnitUpdateRequest transUnitUpdateRequest = updateRequest.get(0);
        assertThat(transUnitUpdateRequest.getNewContents())
                .isEqualTo(mostSimilarTM.getTargetContents());
        assertThat(
                transUnitUpdateRequest.getTargetComment())
                .isEqualTo("auto translated by TM merge from translation memory: test-tm, unique id: uid10");
        assertThat(transUnitUpdateRequest.getSourceType())
                .isEqualTo(TranslationSourceType.TM_MERGE.getAbbr());
    }
}
