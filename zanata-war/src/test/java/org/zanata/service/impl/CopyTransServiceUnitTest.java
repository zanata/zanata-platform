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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.IGNORE;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.REJECT;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationFinder;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.ValidationService;
import org.zanata.service.VersionStateCache;


/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class CopyTransServiceUnitTest {

    @Mock
    LocaleService localeServiceImpl;
    @Mock
    TranslationFinder translationFinder;
    @Mock
    TextFlowTargetDAO textFlowTargetDAO;
    @Mock
    DocumentDAO documentDAO;
    @Mock
    ProjectDAO projectDAO;
    @Mock
    ValidationService validationServiceImpl;
    @Mock
    VersionStateCache versionStateCacheImpl;
    @Mock
    Callable copyTransWork;
    @Mock
    CopyTransWorkFactory copyTransWorkFactory;
    @Mock
    TranslationStateCache translationStateCacheImpl;
    @Mock
    TextFlowDAO textFlowDAO;

    @Before
    public void initializeSeam() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldUseSpecifiedOptions() throws Exception {
        shouldUseProjectOptions(false);
    }

    @Test
    public void shouldUseProjectOptions() throws Exception {
        shouldUseProjectOptions(true);
    }

    private void shouldUseProjectOptions(boolean useProjectOpts) throws Exception {
        CopyTransServiceImpl ctService =
                new CopyTransServiceImpl(
                        localeServiceImpl, projectDAO, documentDAO,
                        copyTransWorkFactory, textFlowTargetDAO,
                        translationStateCacheImpl, textFlowDAO);

        HCopyTransOptions projOptions = new HCopyTransOptions(IGNORE, IGNORE, IGNORE);

        String projSlug = "projslug";
        long projId = 123L;
        HProject proj = createProject(projSlug, projId, projOptions);
        when(projectDAO.findById(projId, false)).thenReturn(proj);


        String iterSlug = "iterslug";
        boolean requireReview = true;
        HProjectIteration iter = createIter(iterSlug, proj, requireReview);

        List<HTextFlow> textFlows = Arrays.asList(new HTextFlow());
        HDocument doc = createDoc(iter, textFlows);

        when(documentDAO.findById(doc.getId())).thenReturn(doc);

        HLocale de = new HLocale(LocaleId.DE);
        List<HLocale> localeList = Arrays.asList(de);
        when(localeServiceImpl.getSupportedLanguageByProjectIteration(projSlug, iterSlug)).thenReturn(localeList);

        when(copyTransWorkFactory
                .createCopyTransExecution(any(HLocale.class),
                        any(HCopyTransOptions.class), any(HDocument.class),
                        anyBoolean(), anyListOf(HTextFlow.class)))
                .thenReturn(
                        copyTransWork);

        HCopyTransOptions optionsIn, optionsOut;
        if (useProjectOpts) {
            optionsIn = null;
            optionsOut = projOptions;
        } else {
            optionsIn = new HCopyTransOptions(DOWNGRADE_TO_FUZZY, DOWNGRADE_TO_FUZZY, DOWNGRADE_TO_FUZZY);
            optionsOut = optionsIn;
        }

        ctService.copyTransForDocument(doc, optionsIn, null);

        verify(copyTransWorkFactory).createCopyTransExecution(de, optionsOut, doc,
                requireReview, textFlows);
        // TODO Need to mock the static method but then that defeats the purpose
        //verify(copyTransWork).call();
    }

    private HDocument createDoc(HProjectIteration iter,
            List<HTextFlow> textFlows) {
        HDocument doc = new HDocument();
        doc.setId(9999L);
        doc.setProjectIteration(iter);
        doc.setTextFlows(textFlows);
        return doc;
    }

    private HProjectIteration createIter(String iterSlug, HProject proj, boolean requireReview) {
        HProjectIteration iter = new HProjectIteration();
        iter.setSlug(iterSlug);
        iter.setProject(proj);
        iter.setRequireTranslationReview(requireReview);
        return iter;
    }

    private HProject createProject(String projSlug, long projId, HCopyTransOptions options) {
        HProject proj = new HProject();
        proj.setSlug(projSlug);
        proj.setId(projId);
        proj.setDefaultCopyTransOpts(options);
        return proj;
    }
}
