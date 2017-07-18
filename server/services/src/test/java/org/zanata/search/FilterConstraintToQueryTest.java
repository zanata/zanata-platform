package org.zanata.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.search.FilterConstraintToQuery.Parameters.*;
import java.util.List;
import org.hamcrest.Matchers;
import org.hibernate.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.model.HLocale;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.search.FilterConstraints;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FilterConstraintToQueryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(FilterConstraintToQueryTest.class);

    public static final ContentCriterion contentCriterion =
            new ContentCriterion(1);
    public static final String SOURCE_CONTENT_CASE_INSENSITIVE =
            contentCriterion.withEntityAlias("tf").contentsCriterionAsString();
    public static final String TARGET_CONTENT_CASE_INSENSITIVE =
            contentCriterion.contentsCriterionAsString();
    public static final String QUERY_BEFORE_WHERE =
            "SELECT distinct tf FROM HTextFlow tf LEFT JOIN tf.targets tfts WITH tfts.index=:locale ";
    @Mock
    private Query query;
    @Mock
    private HLocale hLocale;
    private DocumentId documentId = new DocumentId(1L, "");

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        when(hLocale.getId()).thenReturn(2L);
    }

    @Test
    public void testBuildSearchConditionWithNothingToSearch() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepAll().build(),
                        documentId);
        String result = constraintToQuery.buildSearchCondition();
        assertThat(result, Matchers.nullValue());
    }

    @Test
    public void testBuildSearchConditionInSource() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().filterBy("FiLe").checkInTarget(false)
                        .checkInSource(true).build(), documentId);
        constraintToQuery.setContentCriterion(contentCriterion);
        String result = constraintToQuery.buildSearchCondition();
        assertThat(result, Matchers.equalToIgnoringCase(
                "(" + SOURCE_CONTENT_CASE_INSENSITIVE + ")"));
    }

    @Test
    public void testBuildSearchConditionInTarget() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().filterBy("FiLe").checkInSource(false)
                        .checkInTarget(true).build(), documentId);
        constraintToQuery.setContentCriterion(contentCriterion);
        String result = constraintToQuery.buildSearchCondition();
        assertThat(result,
                Matchers.equalToIgnoringCase(
                        "( EXISTS ( FROM HTextFlowTarget WHERE ("
                                + TARGET_CONTENT_CASE_INSENSITIVE
                                + " and textFlow=tf and locale=:locale)))"));
    }

    @Test
    public void testBuildSearchConditionInBoth() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().filterBy("FiLe").build(),
                        documentId);
        constraintToQuery.setContentCriterion(contentCriterion);
        String result = constraintToQuery.buildSearchCondition();
        assertThat(result,
                Matchers.equalToIgnoringCase(
                        "(" + SOURCE_CONTENT_CASE_INSENSITIVE
                                + " OR  EXISTS ( FROM HTextFlowTarget WHERE ("
                                + TARGET_CONTENT_CASE_INSENSITIVE
                                + " AND textFlow=tf and locale=:locale)))"));
    }

    @Test
    public void testBuildStateConditionWithAllState() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepAll().build(),
                        documentId);
        constraintToQuery.setContentCriterion(contentCriterion);
        String result = constraintToQuery.buildSearchCondition();
        assertThat(result, Matchers.nullValue());
    }

    @Test
    public void testBuildStateConditionWithoutUntranslatedState() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(FilterConstraints
                        .builder().keepAll().excludeNew().build(), documentId);
        String result = constraintToQuery.buildStateCondition();
        assertThat(result, Matchers.equalToIgnoringCase(
                " EXISTS ( FROM HTextFlowTarget WHERE ((textFlow=tf AND locale=:locale) AND state in (:ContentStateList)))"));
    }

    @Test
    public void testBuildStateConditionWithUntranslatedStateButNoSearch() {
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(FilterConstraints.builder().keepAll()
                        .excludeTranslated().build(), documentId);
        String result = constraintToQuery.buildStateCondition();
        assertThat(result, Matchers.equalToIgnoringCase(
                "( EXISTS ( FROM HTextFlowTarget WHERE ((textFlow=tf AND locale=:locale) AND state in (:ContentStateList))) OR :locale not in indices(tf.targets))"));
    }

    @Test
    public void testBuildStateConditionWithUntranslatedStateAndSearch() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery
                        .filterInSingleDocument(
                                FilterConstraints.builder().filterBy("blah")
                                        .excludeTranslated().build(),
                                documentId);
        constraintToQuery.setContentCriterion(contentCriterion);
        String result = constraintToQuery.buildStateCondition();
        assertThat(result,
                Matchers.equalToIgnoringCase(
                        "( EXISTS ( FROM HTextFlowTarget WHERE ((textFlow=tf and locale=:locale) AND state in (:ContentStateList))) OR (:locale not in indices(tf.targets) AND "
                                + SOURCE_CONTENT_CASE_INSENSITIVE + "))"));
    }

    @Test
    public void testToHQLWithNoCondition() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        FilterConstraints.builder().keepAll().build(),
                        documentId);
        String result = constraintToQuery.toEntityQuery();
        assertThat(result, Matchers.equalToIgnoringCase(QUERY_BEFORE_WHERE
                + "WHERE (tf.obsolete=0 AND tf.document.id=:documentId) ORDER BY tf.pos"));
    }

    @Test
    public void testToHQLWithNoConditionForMultipleDocuments() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInMultipleDocuments(
                        FilterConstraints.builder().keepAll().build(),
                        Lists.newArrayList(1L));
        String result = constraintToQuery.toEntityQuery();
        assertThat(result, Matchers.equalToIgnoringCase(QUERY_BEFORE_WHERE
                + "WHERE (tf.obsolete=0 AND tf.document.id in (:documentIdList)) ORDER BY tf.pos"));
    }

    @Test
    public void testToHQLWithSearchAndStateCondition() {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery
                        .filterInSingleDocument(
                                FilterConstraints.builder().filterBy("FiLe")
                                        .excludeTranslated().build(),
                                documentId);
        constraintToQuery.setContentCriterion(contentCriterion);
        String result = constraintToQuery.toEntityQuery();
        log.info("hql: {}", result);

        /*
         * formatted by SQLinform SELECT DISTINCT tf FROM HTextFlow tf LEFT JOIN
         * tf.targets tfts WITH tfts.index=:locale WHERE ( tf.obsolete =0 AND
         * tf.document.id=:documentId AND ( ( lower(tf.content0) LIKE
         * :searchString ) OR EXISTS ( FROM HTextFlowTarget WHERE ( (
         * lower(content0) LIKE :searchString ) AND textFlow=tf AND locale
         * =:locale ) ) ) AND ( EXISTS ( FROM HTextFlowTarget WHERE ( (
         * textFlow=tf AND locale =:locale ) AND state IN (:ContentStateList) )
         * ) OR ( :locale NOT IN indices(tf.targets) AND ( lower(tf.content0)
         * LIKE :searchString ) ) ) ) ORDER BY tf.pos
         */
        assertThat(result, Matchers.equalToIgnoringCase(
                "SELECT distinct tf FROM HTextFlow tf LEFT JOIN tf.targets tfts WITH tfts.index=:locale WHERE (tf.obsolete=0 AND tf.document.id=:documentId AND ((lower(tf.content0) like :searchString) OR  EXISTS ( FROM HTextFlowTarget WHERE ((lower(content0) like :searchString) AND textFlow=tf AND locale=:locale))) AND ( EXISTS ( FROM HTextFlowTarget WHERE ((textFlow=tf AND locale=:locale) AND state in (:ContentStateList))) OR (:locale not in indices(tf.targets) AND (lower(tf.content0) like :searchString)))) ORDER BY tf.pos"));
    }

    @Test
    public void testSetParametersForQuery() {
        FilterConstraints constraints = FilterConstraints.builder()
                .filterBy("file").excludeTranslated().build();
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(constraints, documentId);
        constraintToQuery.setQueryParameters(query, hLocale);
        verify(query).setParameter(
                FilterConstraintToQuery.Parameters.DocumentId.namedParam(),
                documentId.getId());
        verify(query).setParameter(Locale.namedParam(), hLocale.getId());
        verify(query).setParameter(SearchString.namedParam(), "%file%");
        verify(query).setParameterList(ContentStateList.namedParam(),
                constraints.getIncludedStates().asList());
        verifyNoMoreInteractions(query);
    }

    @Test
    public void testSetParametersForQueryWithMultipleDocuments() {
        FilterConstraints constraints = FilterConstraints.builder()
                .filterBy("file").excludeTranslated().build();
        List<Long> docIdList = Lists.newArrayList(1L, 2L);
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInMultipleDocuments(constraints, docIdList);
        constraintToQuery.setQueryParameters(query, hLocale);
        verify(query).setParameterList(DocumentIdList.namedParam(), docIdList);
        verify(query).setParameter(Locale.namedParam(), hLocale.getId());
        verify(query).setParameter(SearchString.namedParam(), "%file%");
        verify(query).setParameterList(ContentStateList.namedParam(),
                constraints.getIncludedStates().asList());
        verifyNoMoreInteractions(query);
    }

    @Test
    public void testSetParametersForQueryWithNoSearch() {
        FilterConstraints constraints = FilterConstraints.builder().keepAll()
                .excludeTranslated().build();
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(constraints, documentId);
        constraintToQuery.setQueryParameters(query, hLocale);
        verify(query).setParameter(
                FilterConstraintToQuery.Parameters.DocumentId.namedParam(),
                documentId.getId());
        verify(query).setParameter(Locale.namedParam(), hLocale.getId());
        verify(query).setParameterList(ContentStateList.namedParam(),
                constraints.getIncludedStates().asList());
        verifyNoMoreInteractions(query);
    }

    @Test
    public void testSetParametersForQueryWithNoStateFilter() {
        FilterConstraints constraints =
                FilterConstraints.builder().filterBy("FiLe").build();
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(constraints, documentId);
        constraintToQuery.setQueryParameters(query, hLocale);
        verify(query).setParameter(
                FilterConstraintToQuery.Parameters.DocumentId.namedParam(),
                documentId.getId());
        verify(query).setParameter(Locale.namedParam(), hLocale.getId());
        verify(query).setParameter(SearchString.namedParam(), "%file%");
        verifyNoMoreInteractions(query);
    }

    @Test
    public void testSetParametersForQueryWithSearchCaseSensitive() {
        FilterConstraints constraints = FilterConstraints.builder()
                .filterBy("FiLe").caseSensitive(true).build();
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(constraints, documentId);
        constraintToQuery.setQueryParameters(query, hLocale);
        verify(query).setParameter(
                FilterConstraintToQuery.Parameters.DocumentId.namedParam(),
                documentId.getId());
        verify(query).setParameter(Locale.namedParam(), hLocale.getId());
        verify(query).setParameter(SearchString.namedParam(), "%FiLe%");
        verifyNoMoreInteractions(query);
    }

    @Test
    public void testSetParametersForQueryWithSearchTermAsPercent() {
        FilterConstraints constraints = FilterConstraints.builder()
                .filterBy("% blah blah %").caseSensitive(true).build();
        FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery
                .filterInSingleDocument(constraints, documentId);
        constraintToQuery.setQueryParameters(query, hLocale);
        verify(query).setParameter(
                FilterConstraintToQuery.Parameters.DocumentId.namedParam(),
                documentId.getId());
        verify(query).setParameter(Locale.namedParam(), hLocale.getId());
        verify(query).setParameter(SearchString.namedParam(),
                "%\\% blah blah \\%%");
        verifyNoMoreInteractions(query);
    }
}
