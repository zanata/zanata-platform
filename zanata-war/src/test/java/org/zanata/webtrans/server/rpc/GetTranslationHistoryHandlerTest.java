package org.zanata.webtrans.server.rpc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import com.google.common.collect.Maps;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-test")
public class GetTranslationHistoryHandlerTest
{
   private GetTranslationHistoryHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private LocaleService localeService;
   @Mock
   private TextFlowDAO textFlowDAO;
   @Mock
   private ExecutionContext executionContext;

   private GetTranslationHistoryAction action;
   private TransUnitId transUnitId = new TransUnitId(1L);
   @Mock 
   private HLocale hLocale;
   private LocaleId localeId = new LocaleId("en-US");

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("localeServiceImpl", localeService)
            .use("textFlowDAO", textFlowDAO)
            .autowire(GetTranslationHistoryHandler.class);
      // @formatter:on
      action = new GetTranslationHistoryAction(transUnitId);
   }
   @Test(expectedExceptions = ActionException.class)
   public void invalidLocaleWillThrowException() throws ActionException
   {
      // Given:
      String projectSlug = "rhel";
      String iterationSlug = "7.0";
      action.setWorkspaceId(new WorkspaceId(new ProjectIterationId(projectSlug, iterationSlug), localeId));
      when(localeService.validateLocaleByProjectIteration(localeId, projectSlug, iterationSlug)).thenThrow(new ZanataServiceException("BANG!"));

      // When:
      handler.execute(action, executionContext);

      // Then:
      verify(identity).checkLoggedIn();
   }
   
   @Test
   public void canGetEmptyHistoryForTextFlowWithNoTranslation() throws ActionException
   {
      // Given: text flow has empty targets
      action.setWorkspaceId(new WorkspaceId(new ProjectIterationId("rhel", "7.0"), localeId));
      when(localeService.validateLocaleByProjectIteration(localeId, "rhel", "7.0")).thenReturn(hLocale);
      HTextFlow hTextFlow = createHTextFlow();
      when(textFlowDAO.findById(transUnitId.getId(), false)).thenReturn(hTextFlow);
      assertThat(hTextFlow.getTargets().values(), Matchers.<HTextFlowTarget>emptyIterable());

      // When:
      GetTranslationHistoryResult result = handler.execute(action, executionContext);

      // Then:
      assertThat(result.getHistoryItems(), Matchers.<TransHistoryItem>empty());
      assertThat(result.getLatest(), Matchers.nullValue());
   }

   @Test
   public void canGetHistoryAndCurrentTranslation() throws ActionException
   {
      // Given: text flow has 2 history translation
      action.setWorkspaceId(new WorkspaceId(new ProjectIterationId("rhel", "7.0"), localeId));
      when(localeService.validateLocaleByProjectIteration(localeId, "rhel", "7.0")).thenReturn(hLocale);
      when(hLocale.getId()).thenReturn(2L);
      HTextFlow hTextFlow = createHTextFlow();
      // two history items
      HashMap<Integer, HTextFlowTargetHistory> history = Maps.newHashMap();
      history.put(0, createHistory(createTarget(new Date(), "smith", 0, null)));
      history.put(1, createHistory(createTarget(new Date(), "john", 1, null)));
      HTextFlowTarget currentTranslation = createTarget(new Date(), "admin", 2, history);
      hTextFlow.getTargets().put(hLocale.getId(), currentTranslation);

      when(textFlowDAO.findById(transUnitId.getId(), false)).thenReturn(hTextFlow);

      // When:
      GetTranslationHistoryResult result = handler.execute(action, executionContext);

      // Then:
      assertThat(result.getHistoryItems(), Matchers.hasSize(2));
      assertThat(result.getLatest().getVersionNum(), Matchers.equalTo(currentTranslation.getVersionNum().toString()));
      assertThat(result.getLatest().getContents(), Matchers.equalTo(currentTranslation.getContents()));
      assertThat(result.getLatest().getModifiedBy(), Matchers.equalTo("admin"));
   }

   @Test
   public void canGetCurrentTranslationWithoutLastModifiedBy() throws ActionException
   {
      // Given: text flow has no history translation and only current translation which has no last modified by person
      action.setWorkspaceId(new WorkspaceId(new ProjectIterationId("rhel", "7.0"), localeId));
      when(localeService.validateLocaleByProjectIteration(localeId, "rhel", "7.0")).thenReturn(hLocale);
      when(hLocale.getId()).thenReturn(2L);
      HTextFlow hTextFlow = createHTextFlow();
      HTextFlowTarget currentTranslation = createTarget(new Date(), null, 0, null);
      currentTranslation.setLastModifiedBy(null);
      hTextFlow.getTargets().put(hLocale.getId(), currentTranslation);

      when(textFlowDAO.findById(transUnitId.getId(), false)).thenReturn(hTextFlow);

      // When:
      GetTranslationHistoryResult result = handler.execute(action, executionContext);

      // Then:
      assertThat(result.getHistoryItems(), Matchers.<TransHistoryItem>emptyIterable());
      assertThat(result.getLatest().getVersionNum(), Matchers.equalTo(currentTranslation.getVersionNum().toString()));
      assertThat(result.getLatest().getContents(), Matchers.equalTo(currentTranslation.getContents()));
      assertThat(result.getLatest().getModifiedBy(), Matchers.equalTo(""));
   }

   private static HTextFlow createHTextFlow()
   {
      HTextFlow hTextFlow = new HTextFlow();
      HashMap<Long, HTextFlowTarget> targetMap = Maps.newHashMap();
      hTextFlow.setTargets(targetMap);
      return hTextFlow;
   }

   private static HTextFlowTarget createTarget(Date lastChanged, String lastModifiedPerson, Integer versionNum, Map<Integer, HTextFlowTargetHistory> historyMap)
   {
      HTextFlowTarget target = new HTextFlowTarget();
      target.setLastChanged(lastChanged);
      HPerson person = new HPerson();
      person.setName(lastModifiedPerson);
      target.setLastModifiedBy(person);
      target.setVersionNum(versionNum);
      target.setHistory(historyMap);
      target.setContents("a", "b");
      return target;
   }

   private static HTextFlowTargetHistory createHistory(HTextFlowTarget target)
   {
      HTextFlowTargetHistory targetHistory = new HTextFlowTargetHistory(target);
      targetHistory.setContents(target.getContents());
      return targetHistory;
   }
}
