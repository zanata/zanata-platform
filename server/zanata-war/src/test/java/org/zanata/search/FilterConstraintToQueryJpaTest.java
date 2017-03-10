package org.zanata.search;

import static org.hamcrest.MatcherAssert.assertThat;
import java.util.List;
import org.hamcrest.Matchers;
import org.hibernate.transform.ResultTransformer;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataJpaTest;
import org.zanata.common.ContentState;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowBuilder;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.WebHook;
import org.zanata.model.type.WebhookType;
import org.zanata.webtrans.server.rpc.GetTransUnitsNavigationService;
import org.zanata.webtrans.shared.model.ContentStateGroup;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.search.FilterConstraints;
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FilterConstraintToQueryJpaTest extends ZanataJpaTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(FilterConstraintToQueryJpaTest.class);

    private ContentStateGroup untranslatedOnly =
            ContentStateGroup.builder().removeAll().includeNew(true).build();
    private ContentStateGroup allContentStates =
            ContentStateGroup.builder().addAll().build();
    private HLocale hLocale;
    private DocumentId documentId;
    private DateTime today = new DateTime();
    private DateTime yesterday = new DateTime().minusDays(1);
    private ResultTransformer transformer;
    private HPerson admin;
    private HPerson translator;

    @Before
    public void setUpData() {
        deleteAllTables();
        hLocale = EntityMakerBuilder.builder().build().makeAndPersist(getEm(),
                HLocale.class);
        transformer =
                new GetTransUnitsNavigationService.TextFlowResultTransformer(
                        hLocale);
        admin = makePerson("admin123");
        translator = makePerson("translator123");
        HDocument hDocument = EntityMakerBuilder.builder()
                .addFieldOrPropertyMaker(HProject.class, "sourceViewURL",
                        FixedValueMaker.EMPTY_STRING_MAKER)
                .reuseEntity(hLocale).build()
                .makeAndPersist(getEm(), HDocument.class);
        HProject hProject = hDocument.getProjectIteration().getProject();
        hProject.getWebHooks().add(new WebHook(hProject,
                "http://www.test.example.com", "test",
                Sets.newHashSet(WebhookType.DocumentMilestoneEvent), "key"));
        documentId = new DocumentId(hDocument.getId(), hDocument.getDocId());
        HTextFlowBuilder baseBuilder = new HTextFlowBuilder()
                .withDocument(hDocument).withTargetLocale(hLocale);
        HTextFlowBuilder adminModifiedToday = baseBuilder
                .withLastModifiedDate(today).withLastModifiedBy(admin);
        // make 10 text flows and some text flow targets
        // 1. null target
        baseBuilder.withResId("res1").withSourceContent("source 1").build();
        // target translated by admin on today
        // 2. translated target
        adminModifiedToday.withResId("res2").withSourceContent("source 2")
                .withTargetContent("target 2")
                .withTargetState(ContentState.Translated).build();
        // 3. fuzzy target
        adminModifiedToday.withResId("res3").withSourceContent("source 3")
                .withTargetContent("target 3")
                .withTargetState(ContentState.NeedReview).build();
        // 4. untranslated target but is not null
        adminModifiedToday.withResId("res4").withSourceContent("source 4")
                .withTargetContent("").withTargetState(ContentState.New)
                .build();
        // 5. target with comment
        adminModifiedToday.withResId("res5").withSourceContent("source 5")
                .withTargetContent("target 5")
                .withTargetState(ContentState.Translated)
                .withTargetComment("target comment").build();
        // 6. source with msgContext
        adminModifiedToday.withResId("res6").withSourceContent("source 6")
                .withMsgContext(",gettext ,fuzzy").build();
        // 7. source with comment
        adminModifiedToday.withResId("res7").withSourceContent("source 7")
                .withSourceComment("source comment").build();
        // 8. target translated by admin on yesterday
        baseBuilder.withLastModifiedDate(yesterday).withLastModifiedBy(admin)
                .withResId("res8").withSourceContent("source 8")
                .withTargetContent("target 8")
                .withTargetState(ContentState.Translated).build();
        // 9. target translated by translator on today
        baseBuilder.withLastModifiedBy(translator).withResId("res9")
                .withSourceContent("source 9").withTargetContent("target 9")
                .withTargetState(ContentState.Translated).build();
        // 10. target translated by translator on yesterday
        baseBuilder.withLastModifiedBy(translator)
                .withLastModifiedDate(yesterday).withResId("res10")
                .withSourceContent("source 10").withTargetContent("target_10")
                .withTargetState(ContentState.Translated).build();
        getEm().flush();
    }

    private HPerson makePerson(String username) {
        return EntityMakerBuilder.builder()
                .addFieldOrPropertyMaker(HAccount.class, "username",
                        FixedValueMaker.fix(username))
                .includeOptionalOneToOne().build()
                .makeAndPersist(getEm(), HPerson.class);
    }

    @Test
    public void getAll() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> textFlows = getResultList(hql, constraintToQuery);
        assertThat(textFlows, Matchers.hasSize(10));
        String navigationQuery = constraintToQuery.toModalNavigationQuery();
        List<HTextFlow> navigationResult =
                getNavigationResult(navigationQuery, constraintToQuery);
        assertThat(navigationResult, Matchers.hasSize(10));
    }

    @SuppressWarnings("unchecked")
    private List<HTextFlow> getNavigationResult(String navigationQuery,
            FilterConstraintToQuery constraintToQuery) {
        org.hibernate.Query query = getSession().createQuery(navigationQuery)
                .setResultTransformer(transformer);
        return constraintToQuery.setQueryParameters(query, hLocale).list();
    }

    @Test
    public void filterBySourceContent() {
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(FilterConstraints.builder().keepNone()
                        .checkInSource(true).includeStates(allContentStates)
                        .filterBy("source 3").build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res3"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    private void verifyModalNavigationQuery(
            FilterConstraintToQuery constraintToQuery, List<HTextFlow> result) {
        String navigationQuery = constraintToQuery.toModalNavigationQuery();
        List<HTextFlow> navigationResult =
                getNavigationResult(navigationQuery, constraintToQuery);
        verifyIdAndContentStateMatches(result, navigationResult);
    }

    private void verifyIdAndContentStateMatches(List<HTextFlow> one,
            List<HTextFlow> two) {
        assertThat(one.size(), Matchers.equalTo(two.size()));
        for (int i = 0; i < one.size(); i++) {
            HTextFlow textFlow1 = one.get(i);
            HTextFlow textFlow2 = two.get(i);
            assertThat(textFlow1.getId(), Matchers.equalTo(textFlow2.getId()));
            assertThat(getContentState(textFlow1, hLocale),
                    Matchers.equalTo(getContentState(textFlow2, hLocale)));
        }
    }

    private static ContentState getContentState(HTextFlow hTextFlow,
            HLocale hLocale) {
        HTextFlowTarget target = hTextFlow.getTargets().get(hLocale.getId());
        return target == null ? ContentState.New : target.getState();
    }

    @Test
    public void filterByContentInSourceAndTarget() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .checkInSource(true).checkInTarget(true)
                                .includeStates(allContentStates).filterBy("2")
                                .build(),
                        documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res2"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByUntranslated() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(untranslatedOnly).build(),
                        documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res1", "res4", "res6", "res7"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByUntranslatedAndSourceContent() {
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(FilterConstraints.builder().keepNone()
                        .checkInSource(true).includeStates(untranslatedOnly)
                        .filterBy("source 4").build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res4"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByResId() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepNone().includeStates(allContentStates)
                        .resourceIdIs("res2").build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res2"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByMessageContext() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepNone().includeStates(allContentStates)
                        .msgContext(",FuzZy").build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res6"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterBySourceComment() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepNone().includeStates(allContentStates)
                        .sourceCommentContains("Comment").build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res7"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByTargetComment() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepNone().includeStates(allContentStates)
                        .targetCommentContains("COMMENT").build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res5"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByTargetModifiedUser() {
        String username = translator.getAccount().getUsername();
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepNone().includeStates(allContentStates)
                        .lastModifiedBy(username).build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res9", "res10"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByTargetChangedDateAfter() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepNone().includeStates(allContentStates)
                        .targetChangedAfter(yesterday).build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids,
                Matchers.contains("res2", "res3", "res4", "res5", "res9"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByTargetChangedDateBefore() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepNone().includeStates(allContentStates)
                        .targetChangedBefore(today).build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res8", "res10"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByUntranslatedAndModifiedPerson() {
        String username = admin.getAccount().getUsername();
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepNone().includeStates(untranslatedOnly)
                        .lastModifiedBy(username).build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res4"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void filterByContentAndModifiedPersonAndState() {
        String username = admin.getAccount().getUsername();
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(FilterConstraints.builder().keepNone()
                        .checkInSource(true).checkInTarget(true)
                        .filterBy("source").includeStates(untranslatedOnly)
                        .lastModifiedBy(username).build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res4"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @Test
    public void testEscapeCharacter() {
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(FilterConstraints.builder().keepNone()
                        .checkInSource(true).checkInTarget(true).filterBy("_")
                        .includeStates(allContentStates).build(), documentId);
        String hql = constraintToQuery.toEntityQuery();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res10"));
        verifyModalNavigationQuery(constraintToQuery, result);
    }

    @SuppressWarnings("unchecked")
    private List<HTextFlow> getResultList(String hql,
            FilterConstraintToQuery constraintToQuery) {
        org.hibernate.Query query = getSession().createQuery(hql);
        return constraintToQuery.setQueryParameters(query, hLocale).list();
    }

    private static List<String> transformToResIds(List<HTextFlow> textFlows) {
        return Lists.transform(textFlows, new Function<HTextFlow, String>() {

            @Override
            public String apply(HTextFlow input) {
                return input.getResId();
            }
        });
    }
}
