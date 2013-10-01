package org.zanata.search;

import java.util.Collection;

import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.zanata.model.HLocale;
import org.zanata.util.QueryBuilder;
import org.zanata.webtrans.shared.model.DocumentId;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.like;
import static org.zanata.util.QueryBuilder.and;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FilterConstraintToQuery {
    protected static final String SEARCH_NAMED_PARAM = "searchString";
    protected static final String STATE_LIST_NAMED_PARAM = "contentStateList";
    protected static final String LOCALE_NAMED_PARAM = "locale";
    protected static final String DOC_ID_NAMED_PARAM = "docId";
    protected static final String DOC_IDS_LIST_NAMED_PARAM = "documentIds";
    private static final String SEARCH_PLACEHOLDER = ":" + SEARCH_NAMED_PARAM;
    private static final String STATE_LIST_PLACEHOLDER = ":"
            + STATE_LIST_NAMED_PARAM;
    private static final String LOCALE_PLACEHOLDER = ":" + LOCALE_NAMED_PARAM;
    private static final String DOC_ID_PLACEHOLDER = ":" + DOC_ID_NAMED_PARAM;
    private static final String DOC_IDS_LIST_PLACEHOLDER = ":"
            + DOC_IDS_LIST_NAMED_PARAM;

    private final FilterConstraints constraints;
    private final boolean hasSearch;
    private String searchString;
    private DocumentId documentId;
    private Collection<Long> documentIds;

    private FilterConstraintToQuery(FilterConstraints constraints,
            DocumentId documentId) {
        this(constraints);
        this.documentId = documentId;
    }

    public FilterConstraintToQuery(FilterConstraints constraints,
            Collection<Long> documentIds) {
        this(constraints);
        this.documentIds = documentIds;
    }

    private FilterConstraintToQuery(FilterConstraints constraints) {
        this.constraints = constraints;
        hasSearch = !Strings.isNullOrEmpty(constraints.getSearchString());
        if (hasSearch) {
            String term =
                    constraints.isCaseSensitive() ? constraints
                            .getSearchString() : constraints.getSearchString()
                            .toLowerCase();
            term = term.replaceAll("%", "\\\\%"); // we need to escape % in
                                                  // database query
            searchString = "%" + term + "%";
        }
    }

    public static FilterConstraintToQuery filterInSingleDocument(
            FilterConstraints constraints, DocumentId documentId) {
        Preconditions.checkNotNull(documentId);
        return new FilterConstraintToQuery(constraints, documentId);
    }

    public static FilterConstraintToQuery filterInMultipleDocuments(
            FilterConstraints constraints, Collection<Long> documentIds) {
        Preconditions.checkNotNull(documentIds);
        Preconditions.checkState(!documentIds.isEmpty());
        return new FilterConstraintToQuery(constraints, documentIds);
    }

    public String toHQL() {
        String docIdCondition;
        if (documentId != null) {
            docIdCondition =
                    eq("tf.document.id", DOC_ID_PLACEHOLDER).toString();
        } else {
            docIdCondition =
                    "tf.document.id in (" + DOC_IDS_LIST_PLACEHOLDER + ")";
        }
        String obsoleteCondition = eq("tf.obsolete", "0").toString();
        String searchCondition = buildSearchCondition();
        String stateCondition = buildStateCondition();

        QueryBuilder query =
                QueryBuilder
                        .select("distinct tf")
                        .from("HTextFlow tf")
                        .leftJoin("tf.targets tfts")
                        .with(eq("tfts.index", LOCALE_PLACEHOLDER).toString())
                        .where(and(obsoleteCondition, docIdCondition,
                            searchCondition, stateCondition))
                        .orderBy("tf.pos");
        return query.toQueryString();
    }

    protected String buildSearchCondition() {
        if (!hasSearch) {
            return null;
        }
        String searchInSourceCondition = null;
        if (constraints.isSearchInSource()) {
            searchInSourceCondition = contentsCriterion("tf").toString();
        }
        String searchInTargetCondition = null;
        if (constraints.isSearchInTarget()) {
            Criterion targetWhereClause =
                    Restrictions.conjunction().add(eq("textFlow", "tf"))
                            .add(eq("locale", LOCALE_PLACEHOLDER))
                            .add(contentsCriterion(null));
            searchInTargetCondition =
                    QueryBuilder.exists().from("HTextFlowTarget")
                            .where(targetWhereClause.toString())
                            .toQueryString();
        }
        return QueryBuilder
                .or(searchInSourceCondition, searchInTargetCondition);
    }

  private Criterion contentsCriterion(String alias) {
        String propertyAlias =
                Strings.isNullOrEmpty(alias) ? "content" : alias + ".content";
        String caseFunction = constraints.isCaseSensitive() ? "" : "lower";
        Disjunction disjunction = Restrictions.disjunction();
        for (int i = 0; i < 6; i++) {
            String contentFieldName =
                    String.format("%s(%s%s)", caseFunction, propertyAlias, i);
            disjunction.add(like(contentFieldName, SEARCH_PLACEHOLDER));
        }
        return disjunction;
    }

    protected String buildStateCondition() {
        if (constraints.getIncludedStates().hasAllStates()) {
            return null;
        }
        Criterion textFlowAndLocaleRestriction =
                Restrictions.and(eq("textFlow", "tf"),
                        eq("locale", LOCALE_PLACEHOLDER));

        String stateInListWhereClause =
                and(textFlowAndLocaleRestriction.toString(),
                        String.format("state in (%s)", STATE_LIST_PLACEHOLDER));
        String stateInListCondition =
                QueryBuilder.exists().from("HTextFlowTarget")
                        .where(stateInListWhereClause).toQueryString();
        if (constraints.getIncludedStates().hasNew()) {
            String nullTargetCondition =
                    String.format("%s not in indices(tf.targets)",
                            LOCALE_PLACEHOLDER);
            if (hasSearch && constraints.isSearchInSource()) {
                nullTargetCondition =
                        and(nullTargetCondition, contentsCriterion("tf")
                                .toString());
            }
            return QueryBuilder.or(stateInListCondition, nullTargetCondition);
        }
        return stateInListCondition;
    }

    public Query setQueryParameters(Query textFlowQuery, HLocale hLocale) {
        if (documentId != null) {
            textFlowQuery.setParameter(DOC_ID_NAMED_PARAM, documentId.getId());
        } else {
            textFlowQuery.setParameterList(DOC_IDS_LIST_NAMED_PARAM,
                    documentIds);
        }
        textFlowQuery.setParameter(LOCALE_NAMED_PARAM, hLocale.getId());
        if (hasSearch) {
            textFlowQuery.setParameter(SEARCH_NAMED_PARAM, searchString);
        }
        if (!constraints.getIncludedStates().hasAllStates()) {
            textFlowQuery.setParameterList(STATE_LIST_NAMED_PARAM, constraints
                    .getIncludedStates().asList());
        }
        return textFlowQuery;
    }
}
