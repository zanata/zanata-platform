package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class RevertTransUnitUpdatesHandlerTest
{
   private RevertTransUnitUpdatesHandler handler;
   @Mock
   private TranslationService translationServiceImpl;
   private TransUnitTransformer transUnitTransformer;
   @Mock
   private SecurityService securityServiceImpl;
   @Mock
   private SecurityService.SecurityCheckResult checkResult;
   @Mock
   private TranslationWorkspace translationWorkspace;
   @Captor
   private ArgumentCaptor<TransUnitUpdated> updatedCaptor;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      transUnitTransformer = SeamAutowire.instance().use("resourceUtils", new ResourceUtils()).autowire(TransUnitTransformer.class);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("translationServiceImpl", translationServiceImpl)
            .use("transUnitTransformer", transUnitTransformer)
            .use("securityServiceImpl", securityServiceImpl)
            .ignoreNonResolvable()
            .autowire(RevertTransUnitUpdatesHandler.class);
      // @formatter:on
   }

   @Test
   public void testExecute() throws Exception
   {
      List<TransUnitUpdateInfo> updatesToRevert = Lists.newArrayList(new TransUnitUpdateInfo(true, true, new DocumentId(1), TestFixture.makeTransUnit(1), 0, 0, ContentState.Approved));
      RevertTransUnitUpdates action = new RevertTransUnitUpdates(updatesToRevert);
      when(securityServiceImpl.checkPermission(action, SecurityService.TranslationAction.MODIFY)).thenReturn(checkResult);
      when(checkResult.getLocale()).thenReturn(new HLocale(LocaleId.EN_US));
      when(checkResult.getWorkspace()).thenReturn(translationWorkspace);
      TranslationService.TranslationResult translationResult = mockTranslationResult(ContentState.NeedReview, 0);
      when(translationServiceImpl.revertTranslations(LocaleId.EN_US, action.getUpdatesToRevert())).thenReturn(Lists.newArrayList(translationResult));

      UpdateTransUnitResult result = handler.execute(action, null);

      assertThat(result.getUpdateInfoList(), Matchers.hasSize(1));
      assertThat(result.getUpdateInfoList().get(0).getPreviousState(), Matchers.equalTo(ContentState.NeedReview));
      verify(translationWorkspace).publish(updatedCaptor.capture());
      TransUnitUpdated transUnitUpdated = updatedCaptor.getValue();
      TransUnitUpdateInfo updateInfo = transUnitUpdated.getUpdateInfo();
      assertThat(updateInfo.getPreviousState(), Matchers.equalTo(ContentState.NeedReview));
      assertThat(updateInfo.getPreviousVersionNum(), Matchers.equalTo(0));
      assertThat(transUnitUpdated.getUpdateType(), Matchers.equalTo(TransUnitUpdated.UpdateType.Revert));
   }

   private static TranslationService.TranslationResult mockTranslationResult(ContentState baseContentState, int baseVersionNum)
   {
      TranslationService.TranslationResult translationResult = mock(TranslationService.TranslationResult.class);
      when(translationResult.isTargetChanged()).thenReturn(true);
      when(translationResult.isTranslationSuccessful()).thenReturn(true);
      when(translationResult.getBaseContentState()).thenReturn(baseContentState);
      when(translationResult.getBaseVersionNum()).thenReturn(baseVersionNum);
      HTextFlow hTextFlow = TestFixture.makeHTextFlow(1, new HLocale(LocaleId.EN_US), ContentState.Approved);
      HDocument spy = spy(new HDocument());
      when(spy.getId()).thenReturn(1L);
      hTextFlow.setDocument(spy);
      when(translationResult.getTranslatedTextFlowTarget()).thenReturn(new HTextFlowTarget(hTextFlow, new HLocale(LocaleId.DE)));

      return translationResult;
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
