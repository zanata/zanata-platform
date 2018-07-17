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
import java.util.Locale;

import org.dbunit.operation.DatabaseOperation;
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
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.seam.security.CurrentUserImpl;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LockManagerService;
import org.zanata.service.TranslationStateCache;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.service.TranslationService.TranslationResult;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({
        LocaleServiceImpl.class,
        CurrentUserImpl.class
})
public class TranslationServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    TranslationServiceImpl transService;

    @Produces @Mock ZanataIdentity identity;
    @Produces @Mock TranslationStateCache translationStateCache;
    @Produces @Mock @FullText FullTextEntityManager fullTextEntityManager;
    @Produces @Mock LockManagerService lockManagerService;
    @Produces Messages messages = new Messages(Locale.ENGLISH);
    @Produces @Mock ValidationServiceImpl validationService;

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
    public void saveTranslationWithSuccessfulValidation() {
        when(validationService.validateWithServerRules(any(), any(), any(), any())).thenReturn(emptyList());

        TransUnitId transUnitId = new TransUnitId(1L);
        List<String> newContents = asList("translated 1", "translated 2");
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, newContents,
                    ContentState.Approved, 1,
                    TranslationSourceType.UNKNOWN.getAbbr());

        List<TranslationResult> result =
                transService.translate(LocaleId.DE,
                        asList(translateReq));

        assertThat(result.get(0).isTranslationSuccessful())
                .as("translation successful").isTrue();
        assertThat(result.get(0).getBaseVersionNum()).isEqualTo(1);
        assertThat(result.get(0).getBaseContentState())
                .isEqualTo(ContentState.Translated);
        assertThat(result.get(0).getNewContentState())
                .isEqualTo(ContentState.Approved);
        assertThat(result.get(0).getTranslatedTextFlowTarget().getVersionNum())
                .isEqualTo(2); // moved up a version
        assertThat(result.get(0).getTranslatedTextFlowTarget().getSourceType())
                .isEqualTo(TranslationSourceType.UNKNOWN);
    }

    @Test
    @InRequestScope
    public void doNotSaveInvalidTranslationOverExistingTranslation() {
        when(validationService.validateWithServerRules(any(), any(), any(), any())).thenReturn(singletonList("validation error"));

        TransUnitId transUnitId = new TransUnitId(1L);
        List<String> newContents = asList("translated 1", "translated 2");
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, newContents,
                        ContentState.Approved, 1,
                        TranslationSourceType.UNKNOWN.getAbbr());

        List<TranslationResult> result =
                transService.translate(LocaleId.DE,
                        asList(translateReq));

        assertThat(result.get(0).isTranslationSuccessful())
                .as("translation successful").isFalse();
    }

    @Test
    @InRequestScope
    public void saveNewTranslationAsFuzzyIfInvalid() {
        when(validationService.validateWithServerRules(any(), any(), any(), any())).thenReturn(singletonList("validation error"));

        // This is the translation of TF 1 for DE
        HTextFlowTarget tft = em.find(HTextFlowTarget.class, 2L);
        em.remove(tft);

        TransUnitId transUnitId = new TransUnitId(1L);
        List<String> newContents = asList("translated 1", "translated 2");
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, newContents,
                        ContentState.Approved, 0,
                        TranslationSourceType.UNKNOWN.getAbbr());

        List<TranslationResult> result =
                transService.translate(LocaleId.DE,
                        asList(translateReq));

        assertThat(result.get(0).isTranslationSuccessful())
                .as("translation successful").isTrue();
        assertThat(result.get(0).getBaseVersionNum()).isEqualTo(0);
        assertThat(result.get(0).getBaseContentState())
                .isEqualTo(ContentState.New);
        assertThat(result.get(0).getNewContentState())
                .isEqualTo(ContentState.NeedReview);
        assertThat(result.get(0).getTranslatedTextFlowTarget().getVersionNum())
                .isEqualTo(1); // moved up a version
        assertThat(result.get(0).getTranslatedTextFlowTarget().getSourceType())
                .isEqualTo(TranslationSourceType.UNKNOWN);
    }

    @Test
    @InRequestScope
    public void replaceFuzzyTranslationWithInvalid() {
        when(validationService.validateWithServerRules(any(), any(), any(), any())).thenReturn(singletonList("validation error"));

        // This is the translation of TF 1 for DE
        HTextFlowTarget tft = em.find(HTextFlowTarget.class, 2L);
        tft.setState(ContentState.NeedReview);
        // this will increment the versionNum to 2
        em.merge(tft);

        TransUnitId transUnitId = new TransUnitId(1L);
        List<String> newContents = asList("translated 1", "translated 2");
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, newContents,
                        ContentState.Approved, 2,
                        TranslationSourceType.UNKNOWN.getAbbr());

        List<TranslationResult> result =
                transService.translate(LocaleId.DE,
                        asList(translateReq));

        assertThat(result.get(0).isTranslationSuccessful())
                .as("translation successful").isTrue();
        assertThat(result.get(0).getBaseVersionNum()).isEqualTo(2);
        assertThat(result.get(0).getBaseContentState())
                .isEqualTo(ContentState.NeedReview);
        assertThat(result.get(0).getNewContentState())
                // NOT Approved (since validation failed)
                .isEqualTo(ContentState.NeedReview);
        assertThat(result.get(0).getTranslatedTextFlowTarget().getVersionNum())
                .isEqualTo(3); // moved up a version
        assertThat(result.get(0).getTranslatedTextFlowTarget().getSourceType())
                .isEqualTo(TranslationSourceType.UNKNOWN);
    }

    @Test
    @InRequestScope
    public void translateMultiple() {
        List<TransUnitUpdateRequest> translationReqs =
                new ArrayList<TransUnitUpdateRequest>();

        // Request 1
        TransUnitId transUnitId = new TransUnitId(1L);
        List<String> newContents1 = asList("translated 1", "translated 2");
        translationReqs.add(new TransUnitUpdateRequest(transUnitId,
                newContents1, ContentState.Approved, 1,
                TranslationSourceType.COPY_VERSION.getAbbr()));

        // Request 2 (different documents)
        transUnitId = new TransUnitId(2L);
        List<String> newContents2 = asList("translated 1", "translated 2");
        translationReqs.add(new TransUnitUpdateRequest(transUnitId,
                newContents2, ContentState.NeedReview, 0,
                TranslationSourceType.COPY_TRANS.getAbbr()));

        List<TranslationResult> results =
                transService.translate(LocaleId.DE, translationReqs);

        // First result
        TranslationResult result = results.get(0);
        assertThat(result.isTranslationSuccessful()).isTrue();
        assertThat(result.getBaseVersionNum()).isEqualTo(1);
        assertThat(result.getBaseContentState()).isEqualTo(ContentState.Translated);

        //there was a previous translation, moved up to a version
        assertThat(result.getTranslatedTextFlowTarget().getVersionNum()).isEqualTo(2);
        assertThat(result.getTranslatedTextFlowTarget().getSourceType())
                .isEqualTo(TranslationSourceType.COPY_VERSION);

        // Second result
        result = results.get(1);
        assertThat(result.isTranslationSuccessful()).isTrue();
        assertThat(result.getBaseVersionNum()).isEqualTo(0);
        assertThat(result.getBaseContentState()).isEqualTo(ContentState.New);

        //no previous translation, first version
        assertThat(result.getTranslatedTextFlowTarget().getVersionNum()).isEqualTo(1);
        assertThat(result.getTranslatedTextFlowTarget().getSourceType())
                .isEqualTo(TranslationSourceType.COPY_TRANS);
    }

    @Test
    @InRequestScope
    public void incorrectBaseVersion() {
        TransUnitId transUnitId = new TransUnitId(2L);
        List<String> newContents = asList("translated 1", "translated 2");
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, newContents,
                        ContentState.Approved, 1,
                        TranslationSourceType.MERGE_VERSION.getAbbr());

        // Should not pass as the base version (1) does not match
        List<TransUnitUpdateRequest> translationRequests =
                asList(translateReq);
        List<TranslationResult> result =
                transService.translate(LocaleId.DE, translationRequests);

        assertThat(result.get(0).isTranslationSuccessful()).isFalse();
    }

    @Test
    @InRequestScope
    public void willCheckPermissionForReviewState() {
        // untranslated
        TransUnitId transUnitId = new TransUnitId(3L);
        TransUnitUpdateRequest translateReq =
                new TransUnitUpdateRequest(transUnitId, asList("a",
                        "b"), ContentState.Approved, 0,
                        TranslationSourceType.MERGE_VERSION.getAbbr());

        List<TranslationResult> result =
                transService.translate(LocaleId.DE,
                        asList(translateReq));

        verify(identity).checkPermission(eq("translation-review"),
                isA(HProject.class), isA(HLocale.class));
        assertThat(result.get(0).isTranslationSuccessful()).isTrue();
        assertThat(result.get(0).getBaseVersionNum()).isEqualTo(0);
        assertThat(result.get(0).getTranslatedTextFlowTarget().getVersionNum())
                .isEqualTo(1); // moved up only one version
        assertThat(result.get(0).getTranslatedTextFlowTarget().getState())
                .isEqualTo(ContentState.Approved);
        assertThat(result.get(0).getTranslatedTextFlowTarget().getSourceType())
                .isEqualTo(TranslationSourceType.MERGE_VERSION);
    }
}
