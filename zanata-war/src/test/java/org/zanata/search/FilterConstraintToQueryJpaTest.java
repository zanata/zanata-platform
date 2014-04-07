package org.zanata.search;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataJpaTest;
import org.zanata.common.ContentState;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowBuilder;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.webtrans.server.rpc.TransUnitTransformer;
import org.zanata.webtrans.shared.model.ContentStateGroup;
import org.zanata.webtrans.shared.model.DocumentId;
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "jpa-tests")
@Slf4j
public class FilterConstraintToQueryJpaTest extends ZanataJpaTest {

    private HLocale hLocale;
    @Mock
    private ResourceUtils resourceUtils;
    private HPerson admin;
    private HDocument hDocument;
    private HPerson translator;
    private ContentStateGroup allContentStates = ContentStateGroup.builder()
            .addAll().build();
    private DocumentId documentId;
    private DateTime today = new DateTime();
    private DateTime yesterday = new DateTime().minusDays(1);

    @BeforeMethod
    public void setUpData() {

        MockitoAnnotations.initMocks(this);
        LogManager.getLogger(
                FilterConstraintToQuery.class.getPackage().getName()).setLevel(
                Level.DEBUG);
        hLocale =
                EntityMakerBuilder.builder().build()
                        .makeAndPersist(getEm(), HLocale.class);

        admin = makePerson("admin");
        translator = makePerson("translator");

        hDocument =
                EntityMakerBuilder.builder().reuseEntity(hLocale).build()
                        .makeAndPersist(getEm(), HDocument.class);
        documentId = new DocumentId(hDocument.getId(), hDocument.getDocId());

        HTextFlowBuilder baseBuilder =
                new HTextFlowBuilder().withDocument(hDocument)
                        .withTargetLocale(hLocale);
        HTextFlowBuilder adminModifiedToday =
                baseBuilder.withLastModifiedDate(today).withLastModifiedBy(
                        admin);
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
                .withSourceContent("source 10").withTargetContent("target 10")
                .withTargetState(ContentState.Translated).build();

        getEm().flush();

        when(
                resourceUtils.getNumPlurals(any(HDocument.class),
                        any(HLocale.class))).thenReturn(1);
    }

    private HPerson makePerson(String username) {
        return EntityMakerBuilder
                .builder()
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

        String hql = constraintToQuery.toHQL();

        List<HTextFlow> textFlows = getResultList(hql, constraintToQuery);
        assertThat(textFlows, Matchers.hasSize(10));
    }

    @Test
    public void filterBySourceContent() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .checkInSource(true)
                                .includeStates(allContentStates)
                                .filterBy("source 3").build(), documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res3"));
    }

    @Test
    public void filterByContentInSourceAndTarget() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .checkInSource(true).checkInTarget(true)
                                .includeStates(allContentStates).filterBy("2")
                                .build(), documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res2"));
    }

    @Test
    public void filterByUntranslated() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints
                                .builder()
                                .keepNone()
                                .includeStates(
                                        ContentStateGroup.builder().removeAll()
                                                .includeNew(true).build())
                                .build(), documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res1", "res4", "res6", "res7"));
    }

    @Test
    public void filterByUntranslatedAndSourceContent() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints
                                .builder()
                                .keepNone()
                                .checkInSource(true)
                                .includeStates(
                                        ContentStateGroup.builder().removeAll()
                                                .includeNew(true).build())
                                .filterBy("source 4").build(), documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res4"));
    }

    @Test
    public void filterByResId() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(allContentStates)
                                .resourceIdContains("s2").build(), documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res2"));
    }

    @Test
    public void filterByMessageContext() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(allContentStates)
                                .msgContext(",FuzZy").build(), documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res6"));
    }

    @Test
    public void filterBySourceComment() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(allContentStates)
                                .sourceCommentContains("Comment").build(),
                        documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res7"));
    }

    @Test
    public void filterByTargetComment() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(allContentStates)
                                .targetCommentContains("COMMENT").build(),
                        documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res5"));
    }

    @Test
    public void filterByTargetModifiedUser() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(allContentStates)
                                .lastModifiedBy("translator").build(),
                        documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res9", "res10"));
    }

    @Test
    public void filterByTargetChangedDateAfter() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(allContentStates)
                                .targetChangedAfter(yesterday).build(),
                        documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids,
                Matchers.contains("res2", "res3", "res4", "res5", "res9"));
    }

    @Test
    public void filterByTargetChangedDateBefore() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(allContentStates)
                                .targetChangedBefore(today).build(),
                        documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res8", "res10"));
    }

    @Test
    public void filterByUntranslatedAndModifiedPerson() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepNone()
                                .includeStates(ContentStateGroup.builder().removeAll().includeNew(true).build())
                                .lastModifiedBy("admin").build(),
                        documentId);

        String hql = constraintToQuery.toHQL();
        List<HTextFlow> result = getResultList(hql, constraintToQuery);
        List<String> ids = transformToResIds(result);
        log.debug("result: {}", ids);
        assertThat(ids, Matchers.contains("res4"));
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
