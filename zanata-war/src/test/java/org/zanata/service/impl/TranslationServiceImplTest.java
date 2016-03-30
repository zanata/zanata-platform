/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.ArrayList;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.zanata.i18n.Messages;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LockManagerService;
import org.zanata.service.TranslationStateCache;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import com.google.common.collect.Lists;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.zanata.service.TranslationService.TranslationResult;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({
        LocaleServiceImpl.class,
        ValidationServiceImpl.class
})
public class TranslationServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    TranslationServiceImpl transService;

    @Produces @Mock ZanataIdentity identity;
    @Produces @Mock TranslationStateCache translationStateCache;
    @Produces @Mock @FullText FullTextEntityManager fullTextEntityManager;
    @Produces @Mock LockManagerService lockManagerService;
    @Produces @Mock Messages messages;

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Produces
    @Authenticated
    HAccount getAuthenticatedAccount(AccountDAO accountDAO) {
        return accountDAO.getByUsername("demo");
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @InRequestScope
    public void translate() throws Exception {
        TransUnitId transUnitId = new TransUnitId(1L);
        List<String> newContents = new ArrayList<String>(2);
        newContents.add("translated 1");
        newContents.add("translated 2");
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, newContents,
                    ContentState.Approved, 1,
                    TranslationSourceType.UNKNOWN.getAbbr());

        List<TranslationResult> result =
                transService.translate(new LocaleId("de"),
                        Lists.newArrayList(translateReq));

        assertThat(result.get(0).isTranslationSuccessful(), is(true));
        assertThat(result.get(0).getBaseVersionNum(), is(1));
        assertThat(result.get(0).getBaseContentState(),
                is(ContentState.Translated));
        assertThat(result.get(0).getTranslatedTextFlowTarget().getVersionNum(),
                is(2)); // moved up a version
        assertThat(result.get(0).getTranslatedTextFlowTarget().getSourceType(),
            is(TranslationSourceType.UNKNOWN));
    }

    @Test
    @InRequestScope
    public void translateMultiple() throws Exception {
        List<TransUnitUpdateRequest> translationReqs =
                new ArrayList<TransUnitUpdateRequest>();

        // Request 1
        TransUnitId transUnitId = new TransUnitId(1L);
        List<String> newContents = new ArrayList<String>(2);
        newContents.add("translated 1");
        newContents.add("translated 2");
        translationReqs.add(new TransUnitUpdateRequest(transUnitId,
                newContents, ContentState.Approved, 1,
                TranslationSourceType.COPY_VERSION.getAbbr()));

        // Request 2 (different documents)
        transUnitId = new TransUnitId(2L);
        newContents = new ArrayList<String>(2);
        newContents.add("translated 1");
        newContents.add("translated 2");
        translationReqs.add(new TransUnitUpdateRequest(transUnitId,
                newContents, ContentState.NeedReview, 0,
                TranslationSourceType.COPY_TRANS.getAbbr()));

        List<TranslationResult> results =
                transService.translate(new LocaleId("de"), translationReqs);

        // First result
        TranslationResult result = results.get(0);
        assertThat(result.isTranslationSuccessful(), is(true));
        assertThat(result.getBaseVersionNum(), is(1));
        assertThat(result.getBaseContentState(), is(ContentState.Translated));

        //there was a previous translation, moved up to a version
        assertThat(result.getTranslatedTextFlowTarget().getVersionNum(), is(2));
        assertThat(result.getTranslatedTextFlowTarget().getSourceType(),
            is(TranslationSourceType.COPY_VERSION));

        // Second result
        result = results.get(1);
        assertThat(result.isTranslationSuccessful(), is(true));
        assertThat(result.getBaseVersionNum(), is(0));
        assertThat(result.getBaseContentState(), is(ContentState.New));

        //no previous translation, first version
        assertThat(result.getTranslatedTextFlowTarget().getVersionNum(), is(1));
        assertThat(result.getTranslatedTextFlowTarget().getSourceType(),
            is(TranslationSourceType.COPY_TRANS));
    }

    @Test
    @InRequestScope
    public void incorrectBaseVersion() throws Exception {
        TransUnitId transUnitId = new TransUnitId(2L);
        List<String> newContents = new ArrayList<String>(2);
        newContents.add("translated 1");
        newContents.add("translated 2");
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, newContents,
                        ContentState.Approved, 1,
                        TranslationSourceType.MERGE_VERSION.getAbbr());

        // Should not pass as the base version (1) does not match
        List<TransUnitUpdateRequest> translationRequests =
                Lists.newArrayList(translateReq);
        List<TranslationResult> result =
                transService.translate(new LocaleId("de"), translationRequests);

        assertThat(result.get(0).isTranslationSuccessful(), Matchers.is(false));
    }

    @Test
    @InRequestScope
    public void willCheckPermissionForReviewState() {
        // untranslated
        TransUnitId transUnitId = new TransUnitId(3L);
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, Lists.newArrayList("a",
                        "b"), ContentState.Approved, 0,
                        TranslationSourceType.MERGE_VERSION.getAbbr());

        List<TranslationResult> result =
                transService.translate(new LocaleId("de"),
                        Lists.newArrayList(translateReq));

        verify(identity).checkPermission(eq("translation-review"),
                isA(HProject.class), isA(HLocale.class));
        assertThat(result.get(0).isTranslationSuccessful(), is(true));
        assertThat(result.get(0).getBaseVersionNum(), is(0));
        assertThat(result.get(0).getTranslatedTextFlowTarget().getVersionNum(),
                is(1)); // moved up only one version
        assertThat(result.get(0).getTranslatedTextFlowTarget().getState(),
                is(ContentState.Approved));
        assertThat(result.get(0).getTranslatedTextFlowTarget().getSourceType(),
            is(TranslationSourceType.MERGE_VERSION));
    }
}
