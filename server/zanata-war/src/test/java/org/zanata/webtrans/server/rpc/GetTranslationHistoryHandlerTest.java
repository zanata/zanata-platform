package org.zanata.webtrans.server.rpc;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.hamcrest.Matchers;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetReviewCommentsDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class GetTranslationHistoryHandlerTest extends ZanataTest {
    @Inject @Any
    private GetTranslationHistoryHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private LocaleService localeService;
    @Produces @Mock
    private TextFlowDAO textFlowDAO;
    @Produces @Mock
    private ExecutionContext executionContext;
    @Produces @Mock
    private TextFlowTargetReviewCommentsDAO reviewCommentsDAO;
    @Produces @Mock
    private ResourceUtils resourceUtils;

    private GetTranslationHistoryAction action;
    private TransUnitId transUnitId = new TransUnitId(1L);
    @Mock
    private HLocale hLocale;
    private LocaleId localeId = new LocaleId("en-US");

    @Before
    public void beforeMethod() {
        action = new GetTranslationHistoryAction(transUnitId);
    }

    @Test(expected = ActionException.class)
    @InRequestScope
    public void invalidLocaleWillThrowException() throws ActionException {
        // Given:
        String projectSlug = "rhel";
        String iterationSlug = "7.0";
        action.setWorkspaceId(new WorkspaceId(new ProjectIterationId(
                projectSlug, iterationSlug, ProjectType.Podir), localeId));
        when(
                localeService.validateLocaleByProjectIteration(localeId,
                        projectSlug, iterationSlug)).thenThrow(
                new ZanataServiceException("BANG!"));

        // When:
        handler.execute(action, executionContext);

        // Then:
        verify(identity).checkLoggedIn();
    }

    @Test
    @InRequestScope
    public void canGetEmptyHistoryForTextFlowWithNoTranslation()
            throws ActionException {
        // Given: text flow has empty targets
        action.setWorkspaceId(new WorkspaceId(new ProjectIterationId("rhel",
                "7.0", ProjectType.Podir), localeId));
        when(
                localeService.validateLocaleByProjectIteration(localeId,
                        "rhel", "7.0")).thenReturn(hLocale);
        HTextFlow hTextFlow = createHTextFlow();
        when(textFlowDAO.findById(transUnitId.getId(), false)).thenReturn(
                hTextFlow);
        assertThat(hTextFlow.getTargets().values(),
                Matchers.<HTextFlowTarget> emptyIterable());

        // When:
        GetTranslationHistoryResult result =
                handler.execute(action, executionContext);

        // Then:
        assertThat(result.getHistoryItems(),
                Matchers.<TransHistoryItem> empty());
        assertThat(result.getLatest(), Matchers.nullValue());
    }

    @Test
    @InRequestScope
    public void canGetHistoryAndCurrentTranslation() throws ActionException {
        // Given: text flow has 2 history translation
        action.setWorkspaceId(new WorkspaceId(new ProjectIterationId("rhel",
                "7.0", ProjectType.Podir), localeId));
        when(
                localeService.validateLocaleByProjectIteration(localeId,
                        "rhel", "7.0")).thenReturn(hLocale);
        when(hLocale.getId()).thenReturn(2L);
        HTextFlow hTextFlow = createHTextFlow();
        // two history items
        HashMap<Integer, HTextFlowTargetHistory> history = Maps.newHashMap();
        history.put(
                0,
                createHistory(createTarget(new Date(), "smith", 0,
                        Maps.<Integer, HTextFlowTargetHistory> newHashMap())));
        history.put(
                1,
                createHistory(createTarget(new Date(), "john", 1,
                        Maps.<Integer, HTextFlowTargetHistory> newHashMap())));
        HTextFlowTarget currentTranslation =
                createTarget(new Date(), "admin", 2, history);
        hTextFlow.getTargets().put(hLocale.getId(), currentTranslation);

        when(resourceUtils.getNumPlurals(hTextFlow.getDocument(), hLocale))
                .thenReturn(2);
        when(textFlowDAO.findById(transUnitId.getId(), false)).thenReturn(
                hTextFlow);

        // When:
        GetTranslationHistoryResult result =
                handler.execute(action, executionContext);

        // Then:
        assertThat(result.getHistoryItems(), Matchers.hasSize(2));
        assertThat(result.getLatest().getVersionNum(),
                Matchers.equalTo(currentTranslation.getVersionNum().toString()));
        assertThat(result.getLatest().getContents(),
                Matchers.equalTo(currentTranslation.getContents()));
        assertThat(result.getLatest().getModifiedBy(),
                Matchers.equalTo("admin"));
    }

    @Test
    @InRequestScope
    public void canGetCurrentTranslationWithoutLastModifiedBy()
            throws ActionException {
        // Given: text flow has no history translation and only current
        // translation which has no last modified by person
        action.setWorkspaceId(new WorkspaceId(new ProjectIterationId("rhel",
                "7.0", ProjectType.Podir), localeId));
        when(
                localeService.validateLocaleByProjectIteration(localeId,
                        "rhel", "7.0")).thenReturn(hLocale);
        when(hLocale.getId()).thenReturn(2L);
        HTextFlow hTextFlow = createHTextFlow();
        HTextFlowTarget currentTranslation =
                createTarget(new Date(), null, 0,
                        new HashMap<Integer, HTextFlowTargetHistory>());
        currentTranslation.setLastModifiedBy(null);
        hTextFlow.getTargets().put(hLocale.getId(), currentTranslation);

        when(resourceUtils.getNumPlurals(hTextFlow.getDocument(), hLocale))
                .thenReturn(2);
        when(textFlowDAO.findById(transUnitId.getId(), false)).thenReturn(
                hTextFlow);

        // When:
        GetTranslationHistoryResult result =
                handler.execute(action, executionContext);

        // Then:
        assertThat(result.getHistoryItems(),
                Matchers.<TransHistoryItem> emptyIterable());
        assertThat(result.getLatest().getVersionNum(),
                Matchers.equalTo(currentTranslation.getVersionNum().toString()));
        assertThat(result.getLatest().getContents(),
                Matchers.equalTo(currentTranslation.getContents()));
        assertThat(result.getLatest().getModifiedBy(), Matchers.equalTo(""));
    }

    @Test
    @InRequestScope
    public void canStripObsoleteTargetContentBasedOnCurrentNPlural()
            throws ActionException {
        // Given: text flow has no history translation
        action.setWorkspaceId(new WorkspaceId(new ProjectIterationId("rhel",
                "7.0", ProjectType.Podir), localeId));
        when(
                localeService.validateLocaleByProjectIteration(localeId,
                        "rhel", "7.0")).thenReturn(hLocale);
        when(hLocale.getId()).thenReturn(2L);
        HTextFlow hTextFlow = createHTextFlow();
        HTextFlowTarget currentTranslation =
                createTarget(new Date(), null, 0,
                        new HashMap<Integer, HTextFlowTargetHistory>());
        currentTranslation.setLastModifiedBy(null);
        hTextFlow.getTargets().put(hLocale.getId(), currentTranslation);

        when(textFlowDAO.findById(transUnitId.getId(), false)).thenReturn(
                hTextFlow);

        // When: number of plurals has changed to 1
        when(resourceUtils.getNumPlurals(hTextFlow.getDocument(), hLocale))
                .thenReturn(1);
        GetTranslationHistoryResult result =
                handler.execute(action, executionContext);

        // Then: the contents we get back is consistent against number of plural
        assertThat(result.getHistoryItems(),
                Matchers.<TransHistoryItem> emptyIterable());
        assertThat(result.getLatest().getVersionNum(),
                Matchers.equalTo(currentTranslation.getVersionNum().toString()));
        assertThat(result.getLatest().getContents(),
                Matchers.contains(currentTranslation.getContents().get(0)));
        assertThat(result.getLatest().getModifiedBy(), Matchers.equalTo(""));
    }

    private static HTextFlow createHTextFlow() {
        HTextFlow hTextFlow = new HTextFlow();
        HashMap<Long, HTextFlowTarget> targetMap = Maps.newHashMap();
        hTextFlow.setTargets(targetMap);
        hTextFlow.setPlural(true);
        return hTextFlow;
    }

    private static HTextFlowTarget createTarget(Date lastChanged,
            String lastModifiedPerson, Integer versionNum,
            @NotNull Map<Integer, HTextFlowTargetHistory> historyMap) {
        HTextFlowTarget target = new HTextFlowTarget();
        target.setLastChanged(lastChanged);
        HPerson person = new HPerson();
        HAccount acc = new HAccount();
        acc.setUsername(lastModifiedPerson);
        person.setAccount(acc);
        person.setName(lastModifiedPerson);
        target.setLastModifiedBy(person);
        target.setVersionNum(versionNum);
        target.setHistory(historyMap);
        target.setContents("a", "b");
        return target;
    }

    private static HTextFlowTargetHistory createHistory(HTextFlowTarget target) {
        HTextFlowTargetHistory targetHistory =
                new HTextFlowTargetHistory(target);
        targetHistory.setContents(target.getContents());
        return targetHistory;
    }

    @Test
    @InRequestScope
    public void canGetReviewComments() {
        GetTranslationHistoryAction action =
                new GetTranslationHistoryAction(new TransUnitId(1L));
        action.setWorkspaceId(TestFixture.workspaceId());
        LocaleId localeId = action.getWorkspaceId().getLocaleId();
        when(
                reviewCommentsDAO.getReviewComments(action.getTransUnitId(),
                        localeId)).thenReturn(
                Lists.newArrayList(makeCommentEntity(localeId, "a comment"),
                        makeCommentEntity(localeId, "another comment")));

        List<ReviewComment> result = handler.getReviewComments(action);

        assertThat(result, Matchers.hasSize(2));
        assertThat(result.get(0).getComment(), Matchers.equalTo("a comment"));
        assertThat(result.get(1).getComment(),
                Matchers.equalTo("another comment"));
    }

    private static HTextFlowTargetReviewComment makeCommentEntity(
            LocaleId localeId, String comment) {
        HLocale hLocale = new HLocale(localeId);
        TestFixture.setId(2L, hLocale);

        HTextFlow textFlow =
                TestFixture.makeHTextFlow(1L, hLocale, ContentState.Rejected);

        HPerson commenter = new HPerson();
        TestFixture.setId(3L, commenter);

        return new HTextFlowTargetReviewComment(textFlow.getTargets().get(
                hLocale.getId()), comment, commenter);
    }
}
