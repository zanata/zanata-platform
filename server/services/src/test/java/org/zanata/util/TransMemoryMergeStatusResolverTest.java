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

package org.zanata.util;

import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.MergeOptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.webtrans.shared.model.TransMemoryResultItem.MatchType;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergeStatusResolverTest {
    TransMemoryMergeStatusResolver resolver;
    private TransMemoryMergeRequest action;
    private HTextFlow textFlow;
    private TransMemoryDetails tmDetail;
    private String docId = "/po/message.po";
    private String projectName = "project name";
    private String resId = "resId";
    private String msgContext;

    @Before
    public void beforeMethod() {
        resolver = TransMemoryMergeStatusResolver.newInstance();

        HDocument document =
                new HDocument(docId, "message.po", "/po", ContentType.PO,
                        new HLocale(new LocaleId("en")));
        HProjectIteration projectIteration = new HProjectIteration();
        HProject project = new HProject();
        project.setName(projectName);
        projectIteration.setProject(project);
        document.setProjectIteration(projectIteration);
        textFlow = new HTextFlow(document, resId, "this is a string.");

        tmDetail = tmDetail(projectName, docId, resId, msgContext);

    }

    private static TransMemoryResultItem tmResultWithSimilarity(double percent) {
        return new TransMemoryResultItem(null, null,
                MatchType.TranslatedInternal, 0, percent, 1L);
    }

    private static TransMemoryResultItem
            tmResultWithSimilarityAndExternallyImported(double percent) {
        return new TransMemoryResultItem(null, null, MatchType.Imported, 0,
                percent, null);
    }

    private static TransMemoryDetails tmDetail(String projectName,
            String docId, String resId, String msgContext) {
        return new TransMemoryDetails(null, null, projectName, null, docId,
                resId, msgContext, null, null, null, null);
    }

    private static TransMemoryMergeRequest mergeTMAction(MergeOptions mergeOptions) {
        TransMemoryMergeRequest mergeRequest =
                new TransMemoryMergeRequest();
        mergeRequest.differentProjectRule = mergeOptions.getDifferentProject();
        mergeRequest.differentContextRule = mergeOptions.getDifferentResId();
        mergeRequest.differentDocumentRule = mergeOptions.getDifferentDocument();
        mergeRequest.thresholdPercent = 80;
        return mergeRequest;
    }

    private static TransMemoryMergeRequest mergeTMActionWhenResIdIsDifferent(
            MergeRule resIdOption) {
        MergeOptions opts = MergeOptions.allFuzzy();
        opts.setDifferentResId(resIdOption);
        return mergeTMAction(opts);
    }

    private static TransMemoryMergeRequest mergeTMActionWhenDocIdIsDifferent(
            MergeRule documentOption) {
        MergeOptions opts = MergeOptions.allFuzzy();
        opts.setDifferentDocument(documentOption);
        return mergeTMAction(opts);
    }

    private TransMemoryMergeRequest mergeTMActionWhenProjectNameIsDifferent(
            MergeRule projectOption) {
        MergeOptions opts = MergeOptions.allFuzzy();
        opts.setDifferentProject(projectOption);
        return mergeTMAction(opts);
    }

    @Test
    public void notOneHundredMatchWillBeSetAsFuzzy() {
        action = mergeTMAction(MergeOptions.allFuzzy());
        ContentState result =
                resolver.decideStatus(action, textFlow, tmDetail,
                        tmResultWithSimilarity(90), null);

        assertThat(result).isEqualTo(ContentState.NeedReview);
    }

    @Test
    public void differentResIdAndOptionIsFuzzy() {
        TransMemoryDetails tmDetail =
                tmDetail(projectName, docId, "different res id", msgContext);
        action = mergeTMActionWhenResIdIsDifferent(MergeRule.FUZZY);
        assertThat(resolver.decideStatus(action, textFlow, tmDetail,
                tmResultWithSimilarity(100), null))
                .isEqualTo(ContentState.NeedReview);
    }


    @Test
    public void differentDocIdAndOptionIsFuzzy() {
        TransMemoryDetails tmDetail =
                tmDetail(projectName, "different doc id", resId, msgContext);
        action = mergeTMActionWhenDocIdIsDifferent(MergeRule.FUZZY);
        assertThat(resolver.decideStatus(action, textFlow, tmDetail,
                tmResultWithSimilarity(100), null))
                .isEqualTo(ContentState.NeedReview);
    }


    @Test
    public void differentProjectNameAndOptionIsFuzzy() {
        TransMemoryDetails tmDetail =
                tmDetail("different project name", docId, resId, msgContext);
        action = mergeTMActionWhenProjectNameIsDifferent(MergeRule.FUZZY);
        assertThat(resolver.decideStatus(action, textFlow, tmDetail,
                tmResultWithSimilarity(100), null))
                .isEqualTo(ContentState.NeedReview);
    }


    @Test
    public
            void
            willRejectIfThereIsOldTranslationButCanNotFindTranslationToSetAsApproved() {
        TransMemoryDetails tmDetail =
                tmDetail("different project name", docId, resId, msgContext);
        action = mergeTMActionWhenProjectNameIsDifferent(MergeRule.FUZZY);

        HTextFlowTarget oldTarget =
                new HTextFlowTarget(textFlow, new HLocale(LocaleId.DE));
        oldTarget.setState(ContentState.NeedReview);

        assertThat(resolver.decideStatus(action, textFlow, tmDetail,
                tmResultWithSimilarity(100), oldTarget)).isNull();
    }

    @Test
    public void fromImportedTmAndOptionIsFuzzy() {
        TransMemoryMergeRequest transMemoryMerge =
                mergeTMAction(importedMatch(MergeRule.FUZZY));
        assertThat(resolver.decideStatus(transMemoryMerge,
                tmResultWithSimilarityAndExternallyImported(100), null))
                .isEqualTo(ContentState.NeedReview);
    }


    private static MergeOptions importedMatch(MergeRule option) {
        MergeOptions opts = MergeOptions.allFuzzy();
        opts.setImportedMatch(option);
        return opts;
    }

}
