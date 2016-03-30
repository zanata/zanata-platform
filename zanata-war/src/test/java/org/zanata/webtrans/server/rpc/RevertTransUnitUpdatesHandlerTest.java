package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hamcrest.Matchers;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.google.common.collect.Lists;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
@AdditionalClasses({
        // required by ServiceLocator
        TransUnitTransformer.class
})
public class RevertTransUnitUpdatesHandlerTest extends ZanataTest {
    @Inject @Any
    private RevertTransUnitUpdatesHandler handler;
    @Produces @Mock
    private ResourceUtils resourceUtils;
    @Produces @Mock
    private TranslationService translationServiceImpl;
    @Produces @Mock
    private SecurityService securityServiceImpl;
    @Produces @Mock
    private TranslationWorkspace translationWorkspace;
    @Produces
    private IServiceLocator serviceLocator = ServiceLocator.instance();

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        List<TransUnitUpdateInfo> updatesToRevert =
                Lists.newArrayList(new TransUnitUpdateInfo(true, true,
                        new DocumentId(new Long(1), ""), TestFixture
                                .makeTransUnit(1), 0, 0, ContentState.Approved));
        RevertTransUnitUpdates action =
                new RevertTransUnitUpdates(updatesToRevert);
        action.setWorkspaceId(new WorkspaceId(new ProjectIterationId("", "",
                ProjectType.File), LocaleId.EN_US));

        TranslationService.TranslationResult translationResult =
                mockTranslationResult(ContentState.NeedReview, 0);
        when(
                translationServiceImpl.revertTranslations(LocaleId.EN_US,
                        action.getUpdatesToRevert())).thenReturn(
                Lists.newArrayList(translationResult));

        UpdateTransUnitResult result = handler.execute(action, null);

        assertThat(result.getUpdateInfoList(), Matchers.hasSize(1));
        assertThat(result.getUpdateInfoList().get(0).getPreviousState(),
                Matchers.equalTo(ContentState.NeedReview));
    }

    private static TranslationService.TranslationResult mockTranslationResult(
            ContentState baseContentState, int baseVersionNum) {
        TranslationService.TranslationResult translationResult =
                mock(TranslationService.TranslationResult.class);
        when(translationResult.isTargetChanged()).thenReturn(true);
        when(translationResult.isTranslationSuccessful()).thenReturn(true);
        when(translationResult.getBaseContentState()).thenReturn(
                baseContentState);
        when(translationResult.getBaseVersionNum()).thenReturn(baseVersionNum);
        HTextFlow hTextFlow =
                TestFixture.makeHTextFlow(1, new HLocale(LocaleId.EN_US),
                        ContentState.Approved);
        HDocument spy = spy(new HDocument());
        when(spy.getId()).thenReturn(1L);
        hTextFlow.setDocument(spy);
        when(translationResult.getTranslatedTextFlowTarget()).thenReturn(
                new HTextFlowTarget(hTextFlow, new HLocale(LocaleId.DE)));

        return translationResult;
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
