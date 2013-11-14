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

package org.zanata.webtrans.server.rpc;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.webtrans.shared.model.TransMemoryResultItem.MatchType;
import static org.zanata.webtrans.shared.rpc.HasSearchType.SearchType.FUZZY_PLURAL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.customware.gwt.dispatch.shared.ActionException;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TransMemoryUnitDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.TestFixture;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.SecurityService;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.MergeOptions;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransMemoryMergeHandlerTest {
    TransMemoryMergeHandler handler;
    @Mock
    private GetTransMemoryHandler getTransMemoryHandler;
    @Mock
    private GetTransMemoryDetailsHandler getTransMemoryDetailsHandler;
    @Mock
    private UpdateTransUnitHandler updateTransUnitHandler;
    @Mock
    private SecurityService securityService;
    @Mock
    private TextFlowDAO textFlowDAO;
    @Mock
    private TransMemoryUnitDAO transMemoryUnitDAO;
    @Mock
    private SecurityService.SecurityCheckResult securityResult;
    private HLocale hLocale = new HLocale(new LocaleId("en-US"));
    private HLocale sourceLocale = new HLocale(new LocaleId("en-US"));
    @Mock
    private TranslationWorkspace workspace;
    @Captor
    ArgumentCaptor<List<TransUnitUpdateRequest>> updateRequestCaptor;

    private static List<Long> idsWithTranslations = newArrayList(11L, 12L, 13L,
            14L);
    private static ArrayList<String> tmSource = newArrayList("tm source");
    private static ArrayList<String> tmTarget = newArrayList("tm target");

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
      handler = SeamAutowire.instance()
            .reset()
            .use("webtrans.gwt.GetTransMemoryHandler", getTransMemoryHandler)
            .use("webtrans.gwt.GetTransMemoryDetailsHandler", getTransMemoryDetailsHandler)
            .use("webtrans.gwt.UpdateTransUnitHandler", updateTransUnitHandler)
            .use("securityServiceImpl", securityService)
            .use("textFlowDAO", textFlowDAO)
            .use("transMemoryUnitDAO", transMemoryUnitDAO)
            .autowire(TransMemoryMergeHandler.class);
      // @formatter:on

    }

    private TransMemoryMerge prepareActionAndMockSecurityService(int threshold,
            long... tranUnitIds) throws NoSuchWorkspaceException {
        return prepareActionAndMockSecurityService(threshold, true, tranUnitIds);
    }

    private TransMemoryMerge prepareActionAndMockSecurityService(int threshold,
            boolean acceptImportedTMResults, long... tranUnitIds)
            throws NoSuchWorkspaceException {
        List<TransUnitUpdateRequest> requests = newArrayList();
        for (long tranUnitId : tranUnitIds) {
            requests.add(new TransUnitUpdateRequest(
                    new TransUnitId(tranUnitId), null, null, 0));
        }
        // we have TransMemoryMergeStatusResolverTest to cover various different
        // merge options so here we don't test that
        MergeRule importedTMOption =
                acceptImportedTMResults ? MergeRule.IGNORE_CHECK
                        : MergeRule.REJECT;
        MergeOptions opts = MergeOptions.allIgnore();
        opts.setImportedMatch(importedTMOption);
        TransMemoryMerge action =
                new TransMemoryMerge(threshold, requests, opts);
        mockSecurityService(action);
        return action;
    }

    private void mockSecurityService(TransMemoryMerge action)
            throws NoSuchWorkspaceException {
        when(
                securityService.checkPermission(action,
                        SecurityService.TranslationAction.MODIFY)).thenReturn(
                securityResult);
        when(securityResult.getLocale()).thenReturn(hLocale);
        when(securityResult.getWorkspace()).thenReturn(workspace);
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
                "pot/msg.pot", "resId", null, null, null, null);
    }

    @Test
    public void canAutoTranslateIfHasTMAboveThreshold() throws ActionException {
        // Given: an action with threshold 80% and trans unit id is 1
        final long transUnitId = 1L;
        TransMemoryMerge action =
                prepareActionAndMockSecurityService(80, transUnitId);

        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, hLocale,
                        ContentState.New, "pot/a.po");
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));
        // Given: there are three TM results returned for text flow id 1, and
        // the most matched one is text flow id 11
        HTextFlow tmResultSource =
                TestFixture.makeApprovedHTextFlow(11L, hLocale);
        TransMemoryResultItem mostSimilarTM =
                tmResult(tmResultSource.getId(), 100);
        TransMemoryQuery tmQuery =
                new TransMemoryQuery(hTextFlow.getContents(), FUZZY_PLURAL);
        when(
                getTransMemoryHandler.searchTransMemory(hLocale, tmQuery,
                        sourceLocale.getLocaleId())).thenReturn(
                newArrayList(mostSimilarTM, tmResult(12L, 90),
                        tmResult(13L, 80)));
        when(textFlowDAO.findById(tmResultSource.getId(), false)).thenReturn(
                tmResultSource);
        // Given: tm detail of text flow id 11
        when(
                getTransMemoryDetailsHandler.getTransMemoryDetail(hLocale,
                        tmResultSource)).thenReturn(tmDetail());

        // When: execute the action
        handler.execute(action, null);

        // Then: we should have text flow auto translated by using the most
        // similar TM
        verify(updateTransUnitHandler).doTranslation(
                same(hLocale.getLocaleId()), same(workspace),
                updateRequestCaptor.capture(),
                same(action.getEditorClientId()),
                eq(TransUnitUpdated.UpdateType.TMMerge));
        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest, Matchers.hasSize(1));
        TransUnitUpdateRequest transUnitUpdateRequest = updateRequest.get(0);
        assertThat(transUnitUpdateRequest.getNewContents(),
                Matchers.equalTo(mostSimilarTM.getTargetContents()));
        assertThat(
                transUnitUpdateRequest.getTargetComment(),
                Matchers.equalTo("auto translated by TM merge from project: project a, version: master, DocId: pot/msg.pot"));
    }

    @Test
    public void willTranslateIfTargetIsNull() throws ActionException {
        // Given: text flow id 1 is not untranslated
        final long transUnitId = 1L;
        TransMemoryMerge action =
                prepareActionAndMockSecurityService(80, transUnitId);

        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, hLocale,
                        ContentState.New, "pot/a.po");
        hTextFlow.getTargets().put(hLocale.getId(), null); // make sure target
                                                           // is null
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));
        // Given: there is TM results returned for text flow id 1
        HTextFlow tmResultSource =
                TestFixture.makeApprovedHTextFlow(11L, hLocale);
        TransMemoryQuery tmQuery =
                new TransMemoryQuery(hTextFlow.getContents(), FUZZY_PLURAL);
        TransMemoryResultItem tmResult = tmResult(tmResultSource.getId(), 100);
        when(
                getTransMemoryHandler.searchTransMemory(hLocale, tmQuery,
                        sourceLocale.getLocaleId())).thenReturn(
                newArrayList(tmResult));
        when(textFlowDAO.findById(tmResultSource.getId(), false)).thenReturn(
                tmResultSource);
        // Given: tm detail of text flow id 11
        when(
                getTransMemoryDetailsHandler.getTransMemoryDetail(hLocale,
                        tmResultSource)).thenReturn(tmDetail());

        // When: execute the action
        handler.execute(action, null);

        // Then: we should have translation auto filled
        verify(updateTransUnitHandler).doTranslation(
                same(hLocale.getLocaleId()), same(workspace),
                updateRequestCaptor.capture(),
                same(action.getEditorClientId()),
                eq(TransUnitUpdated.UpdateType.TMMerge));
        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest, Matchers.hasSize(1));
        assertThat(updateRequest.get(0).getNewContents(),
                Matchers.equalTo(tmResult.getTargetContents()));
    }

    @Test
    public void willNotTranslateIfNoTM() throws ActionException {
        // Given: an action with threshold 80% and trans unit id is 1
        final long transUnitId = 1L;
        TransMemoryMerge action =
                prepareActionAndMockSecurityService(80, transUnitId);

        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, hLocale,
                        ContentState.New, "pot/a.po");
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));
        // Given: there is no TM results returned for text flow id 1
        TransMemoryQuery tmQuery =
                new TransMemoryQuery(hTextFlow.getContents(), FUZZY_PLURAL);
        when(
                getTransMemoryHandler.searchTransMemory(hLocale, tmQuery,
                        sourceLocale.getLocaleId())).thenReturn(
                Lists.<TransMemoryResultItem> newArrayList());

        // When: execute the action
        UpdateTransUnitResult result = handler.execute(action, null);

        // Then: we should have EMPTY trans unit update request
        verifyZeroInteractions(updateTransUnitHandler);
        assertThat(result.getUpdateInfoList(),
                Matchers.<TransUnitUpdateInfo> empty());
    }

    @Test
    public void willNotTranslateIfTMBelowThreshold() throws ActionException {
        // Given: an action with threshold 80% and trans unit id is 1
        final long transUnitId = 1L;
        TransMemoryMerge action =
                prepareActionAndMockSecurityService(80, transUnitId);

        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, hLocale,
                        ContentState.New, "pot/a.po");
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));
        // Given: there are three TM results returned for text flow id 1, and
        // the most matched one has percentage 79
        TransMemoryQuery tmQuery =
                new TransMemoryQuery(hTextFlow.getContents(), FUZZY_PLURAL);
        when(
                getTransMemoryHandler.searchTransMemory(hLocale, tmQuery,
                        sourceLocale.getLocaleId())).thenReturn(
                newArrayList(tmResult(11L, 79), tmResult(12L, 60),
                        tmResult(13L, 50)));

        // When: execute the action
        UpdateTransUnitResult result = handler.execute(action, null);

        // Then: we should have EMPTY trans unit update request
        verifyZeroInteractions(updateTransUnitHandler);
        assertThat(result.getUpdateInfoList(),
                Matchers.<TransUnitUpdateInfo> empty());
    }

    @Test
    public void willIgnoreApprovedTextFlows() throws ActionException {
        // Given: text flow id 1 is not untranslated
        final long transUnitId = 1L;
        TransMemoryMerge action =
                prepareActionAndMockSecurityService(80, transUnitId);

        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, hLocale,
                        ContentState.Approved, "pot/a.po");
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));

        // When: execute the action
        UpdateTransUnitResult result = handler.execute(action, null);

        // Then: we should have EMPTY trans unit update request
        verifyZeroInteractions(getTransMemoryHandler,
                getTransMemoryDetailsHandler, updateTransUnitHandler);
        assertThat(result.getUpdateInfoList(),
                Matchers.<TransUnitUpdateInfo> empty());
    }

    @Test
    public void willNotTranslateIfDifferentMetaDataOptionSetToSkip()
            throws ActionException {
        // Given: an action with threshold 80% and trans unit id is 1, with
        // different doc id option set to skip
        final long transUnitId = 1L;
        ArrayList<TransUnitUpdateRequest> requests =
                Lists.newArrayList(new TransUnitUpdateRequest(new TransUnitId(
                        1L), null, null, 0));
        MergeOptions opts = MergeOptions.allIgnore();
        opts.setDifferentDocument(MergeRule.REJECT);
        TransMemoryMerge action = new TransMemoryMerge(80, requests, opts);
        mockSecurityService(action);

        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, hLocale,
                        ContentState.New, "pot/a.po");
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));
        // Given: there are three TM results returned for text flow id 1, and
        // the most matched one is text flow id 11
        HTextFlow tmResultSource =
                TestFixture.makeApprovedHTextFlow(11L, hLocale);
        TransMemoryResultItem mostSimilarTM =
                tmResult(tmResultSource.getId(), 100);
        TransMemoryQuery tmQuery =
                new TransMemoryQuery(hTextFlow.getContents(), FUZZY_PLURAL);
        when(
                getTransMemoryHandler.searchTransMemory(hLocale, tmQuery,
                        sourceLocale.getLocaleId())).thenReturn(
                newArrayList(mostSimilarTM, tmResult(12L, 90),
                        tmResult(13L, 80)));
        when(textFlowDAO.findById(tmResultSource.getId(), false)).thenReturn(
                tmResultSource);
        // Given: tm detail of text flow id 11 which has different doc id
        TransMemoryDetails tmDetails =
                new TransMemoryDetails("", "", "project a", "master",
                        "different/doc/id", "resId", "", ContentState.Approved,
                        "", new Date());
        when(
                getTransMemoryDetailsHandler.getTransMemoryDetail(hLocale,
                        tmResultSource)).thenReturn(tmDetails);

        // When: execute the action
        UpdateTransUnitResult result = handler.execute(action, null);

        // Then: we should have EMPTY trans unit update request
        verifyZeroInteractions(updateTransUnitHandler);
        assertThat(result.getUpdateInfoList(),
                Matchers.<TransUnitUpdateInfo> empty());
    }

    @Test
    public void canHandleMultipleTextFlows() throws ActionException {
        // Given: an action with threshold 90% and trans unit id is 1, 2, 3, 4
        final long idWith100MatchTM = 1L;
        final long idWithoutTM = 2L;
        final long idWith80MatchTM = 3L;
        final long idWith90MatchTM = 4L;
        TransMemoryMerge action =
                prepareActionAndMockSecurityService(90, idWith100MatchTM,
                        idWithoutTM, idWith80MatchTM, idWith90MatchTM);

        HTextFlow textFlow100TM =
                TestFixture.makeHTextFlow(idWith100MatchTM, hLocale,
                        ContentState.New, "pot/a.po");
        HTextFlow textFlowNoTM =
                TestFixture.makeHTextFlow(idWithoutTM, hLocale,
                        ContentState.New, "pot/a.po");
        HTextFlow textFlow80TM =
                TestFixture.makeHTextFlow(idWith80MatchTM, hLocale,
                        ContentState.New, "pot/a.po");
        HTextFlow textFLow90TM =
                TestFixture.makeHTextFlow(idWith90MatchTM, hLocale,
                        ContentState.New, "pot/a.po");

        when(
                textFlowDAO.findByIdList(newArrayList(idWith100MatchTM,
                        idWithoutTM, idWith80MatchTM, idWith90MatchTM)))
                .thenReturn(
                        newArrayList(textFlow100TM, textFlowNoTM, textFlow80TM,
                                textFLow90TM));
        // Given: TM results
        HTextFlow tmResultSource =
                TestFixture.makeApprovedHTextFlow(11L, hLocale);
        TransMemoryResultItem tm100 = tmResult(tmResultSource.getId(), 100);
        TransMemoryResultItem tm90 = tmResult(tmResultSource.getId(), 90);
        TransMemoryResultItem tm80 = tmResult(tmResultSource.getId(), 80);

        when(
                getTransMemoryHandler.searchTransMemory(hLocale,
                        new TransMemoryQuery(textFlow100TM.getContents(),
                                FUZZY_PLURAL), sourceLocale.getLocaleId()))
                .thenReturn(newArrayList(tm100));
        when(
                getTransMemoryHandler.searchTransMemory(hLocale,
                        new TransMemoryQuery(textFLow90TM.getContents(),
                                FUZZY_PLURAL), sourceLocale.getLocaleId()))
                .thenReturn(newArrayList(tm90));
        when(
                getTransMemoryHandler.searchTransMemory(hLocale,
                        new TransMemoryQuery(textFlow80TM.getContents(),
                                FUZZY_PLURAL), sourceLocale.getLocaleId()))
                .thenReturn(newArrayList(tm80));
        when(
                getTransMemoryHandler.searchTransMemory(hLocale,
                        new TransMemoryQuery(textFlowNoTM.getContents(),
                                FUZZY_PLURAL), sourceLocale.getLocaleId()))
                .thenReturn(Lists.<TransMemoryResultItem> newArrayList());

        when(textFlowDAO.findById(tmResultSource.getId(), false)).thenReturn(
                tmResultSource);
        // Given: tm detail of text flow id 11
        when(
                getTransMemoryDetailsHandler.getTransMemoryDetail(hLocale,
                        tmResultSource)).thenReturn(tmDetail());

        // When: execute the action
        handler.execute(action, null);

        // Then: we should have text flow auto translated by using the most
        // similar TM
        verify(updateTransUnitHandler).doTranslation(
                same(hLocale.getLocaleId()), same(workspace),
                updateRequestCaptor.capture(),
                same(action.getEditorClientId()),
                eq(TransUnitUpdated.UpdateType.TMMerge));
        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest, Matchers.hasSize(2));
        assertThat(updateRequest.get(0).getNewContents(),
                Matchers.equalTo(tm100.getTargetContents()));
        assertThat(updateRequest.get(1).getNewContents(),
                Matchers.equalTo(tm90.getTargetContents()));
    }

    @Test
    public void doubleToInt() {
        double oneHundred = 100.00000001D;

        assertThat(oneHundred <= 100, Matchers.is(false));
        assertThat((int) oneHundred <= 100, Matchers.is(true));

    }

    @Test
    public void canAutoTranslateImportedTMResults() throws Exception {
        // Given:
        // an action with threshold 80% and trans unit id
        final long transUnitId = 1L;
        TransMemoryMerge mergeAction =
                prepareActionAndMockSecurityService(80, transUnitId);

        // A Text Flow to be translated
        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, hLocale,
                        ContentState.New, "pot/a.po");

        // A matching imported Translation Unit
        TransMemoryUnit tuResultSource =
                TestFixture.makeTransMemoryUnit(10L, hLocale);

        // and an associated result item
        TransMemoryResultItem mostSimilarTM =
                importedTmResult(tuResultSource.getId(), 100);

        // A Translation memory query
        TransMemoryQuery tmQuery =
                new TransMemoryQuery(hTextFlow.getContents(), FUZZY_PLURAL);

        // Expectations:
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));
        when(
                getTransMemoryHandler.searchTransMemory(hLocale, tmQuery,
                        sourceLocale.getLocaleId())).thenReturn(
                newArrayList(mostSimilarTM, tmResult(12L, 90),
                        tmResult(13L, 80)));
        when(transMemoryUnitDAO.findById(tuResultSource.getId())).thenReturn(
                tuResultSource);

        // When: execute the action
        handler.execute(mergeAction, null);

        // Then: we should have text flow auto translated by using the most
        // similar TM
        verify(updateTransUnitHandler).doTranslation(
                same(hLocale.getLocaleId()), same(workspace),
                updateRequestCaptor.capture(),
                same(mergeAction.getEditorClientId()),
                eq(TransUnitUpdated.UpdateType.TMMerge));
        List<TransUnitUpdateRequest> updateRequest =
                updateRequestCaptor.getValue();
        assertThat(updateRequest, Matchers.hasSize(1));
        TransUnitUpdateRequest transUnitUpdateRequest = updateRequest.get(0);
        assertThat(transUnitUpdateRequest.getNewContents(),
                Matchers.equalTo(mostSimilarTM.getTargetContents()));
        assertThat(
                transUnitUpdateRequest.getTargetComment(),
                Matchers.equalTo("auto translated by TM merge from translation memory: test-tm, unique id: uid10"));
    }

    @Test
    public void willIgnoreImportedTMResults() throws Exception {
        // Given:
        // an action with threshold 80% and trans unit id
        final long transUnitId = 1L;
        prepareActionAndMockSecurityService(80, transUnitId);
        TransMemoryMerge mergeAction =
                prepareActionAndMockSecurityService(80, false, transUnitId);

        // A Text Flow to be translated
        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(transUnitId, hLocale,
                        ContentState.New, "pot/a.po");

        // A matching imported Translation Unit
        TransMemoryUnit tuResultSource =
                TestFixture.makeTransMemoryUnit(10L, hLocale);

        // and an associated result item
        TransMemoryResultItem mostSimilarTM =
                importedTmResult(tuResultSource.getId(), 100);

        // A Translation memory query
        TransMemoryQuery tmQuery =
                new TransMemoryQuery(hTextFlow.getContents(), FUZZY_PLURAL);

        // Expectations:
        when(textFlowDAO.findByIdList(newArrayList(transUnitId))).thenReturn(
                newArrayList(hTextFlow));
        when(
                getTransMemoryHandler.searchTransMemory(hLocale, tmQuery,
                        sourceLocale.getLocaleId())).thenReturn(
                newArrayList(mostSimilarTM, tmResult(12L, 90),
                        tmResult(13L, 80)));
        when(transMemoryUnitDAO.findById(tuResultSource.getId())).thenReturn(
                tuResultSource);

        // When: execute the action
        UpdateTransUnitResult result = handler.execute(mergeAction, null);

        // Then: We should not have anything auto-translated
        // Then: we should have EMPTY trans unit update request
        verifyZeroInteractions(updateTransUnitHandler);
        assertThat(result.getUpdateInfoList(),
                Matchers.<TransUnitUpdateInfo> empty());
    }
}
