package org.zanata.search;

import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.joda.time.DateTime;
import org.zanata.common.HasContents;
import org.zanata.model.HLocale;
import org.zanata.util.HqlCriterion;
import org.zanata.util.QueryBuilder;
import org.zanata.webtrans.shared.model.DocumentId;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Setter;

import static org.zanata.search.FilterConstraintToQuery.Parameters.*;
import static org.zanata.util.HqlCriterion.eq;
import static org.zanata.util.HqlCriterion.escapeWildcard;
import static org.zanata.util.HqlCriterion.ilike;
import static org.zanata.util.HqlCriterion.match;
import static org.zanata.util.QueryBuilder.and;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FilterConstraintToQuery {
    private final FilterConstraints constraints;
    private final boolean hasSearch;
    private String searchString;
    private DocumentId documentId;
    private Collection<Long> documentIds;

    @Setter(AccessLevel.PACKAGE)
    private ContentCriterion contentCriterion = new ContentCriterion(
            HasContents.MAX_PLURALS);

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
            term = escapeWildcard(term);
            searchString = match(term, MatchMode.ANYWHERE);
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

    /**
     * This builds a query for constructing TransUnit in editor. It returns a
     * list of HTextFlow objects
     *
     * @return the HQL query
     */
    public String toEntityQuery() {
        String docIdCondition;
        if (documentId != null) {
            docIdCondition =
                    eq("tf.document.id", Parameters.documentId.placeHolder());
        } else {
            docIdCondition =
                    "tf.document.id in (" + documentIdList.placeHolder() + ")";
        }
        return buildQuery("distinct tf", docIdCondition);
    }

    private String buildQuery(String selectStatement, String docIdCondition) {

        String obsoleteCondition = eq("tf.obsolete", "0");
        String searchCondition = buildSearchCondition();
        String stateCondition = buildStateCondition();

        QueryBuilder query =
                QueryBuilder
                        .select(selectStatement)
                        .from("HTextFlow tf")
                        .leftJoin("tf.targets tfts")
                        .with(eq("tfts.index", locale.placeHolder()))
                        .where(and(obsoleteCondition, docIdCondition,
                                searchCondition, stateCondition))
                        .orderBy("tf.pos");
        return query.toQueryString();
    }

    /**
     * This builds a query for editor modal navigation. It only select text flow
     * id and text flow target content state.
     *
     * @return the HQL query
     */
    public String toModalNavigationQuery() {
        String docIdCondition =
                eq("tf.document.id", Parameters.documentId.placeHolder());
        return buildQuery(
                "distinct tf.id as id, (case when tfts is null then 0 else tfts.state end) as state, tf.pos as pos",
                docIdCondition);
    }

    protected String buildSearchCondition() {
        String searchInSourceCondition = null;
        List<String> sourceConjunction = Lists.newArrayList();
        if (hasSearch && constraints.isSearchInSource()) {
            String contentsCriterion =
                    contentCriterion.contentsCriterionAsString("tf",
                            constraints.isCaseSensitive(),
                            Parameters.searchString.placeHolder());
            addToJunctionIfNotNull(sourceConjunction, contentsCriterion);
        }
        addToJunctionIfNotNull(sourceConjunction,
                buildSourceCommentCondition(constraints.getSourceComment()));
        addToJunctionIfNotNull(sourceConjunction, buildMsgContextCondition());
        addToJunctionIfNotNull(sourceConjunction, buildResourceIdCondition());

        if (!sourceConjunction.isEmpty()) {
            searchInSourceCondition = QueryBuilder.and(sourceConjunction);
        }

        String searchInTargetCondition = null;
        List<String> targetConjunction = Lists.newArrayList();

        if (hasSearch && constraints.isSearchInTarget()) {
            targetConjunction.add(contentCriterion.contentsCriterionAsString(
                    null, constraints.isCaseSensitive(),
                    Parameters.searchString.placeHolder()));
        }
        addToJunctionIfNotNull(targetConjunction,
                buildLastModifiedByCondition());
        addToJunctionIfNotNull(targetConjunction,
                buildTargetCommentCondition(constraints.getTransComment()));
        addToJunctionIfNotNull(targetConjunction,
                buildLastModifiedDateCondition());
        if (!targetConjunction.isEmpty()) {
            targetConjunction.add(eq("textFlow", "tf"));
            targetConjunction.add(eq("locale", locale.placeHolder()));

            searchInTargetCondition =
                    QueryBuilder.exists().from("HTextFlowTarget")
                            .where(QueryBuilder.and(targetConjunction))
                            .toQueryString();
        }
        if (searchInSourceCondition == null && searchInTargetCondition == null) {
            return null;
        }
        return QueryBuilder
                .or(searchInSourceCondition, searchInTargetCondition);
    }

    private static List<String> addToJunctionIfNotNull(List<String> junction,
            String criterion) {
        if (criterion != null) {
            junction.add(criterion);
        }
        return junction;
    }

    private String buildResourceIdCondition() {
        if (Strings.isNullOrEmpty(constraints.getResId())) {
            return null;
        }
        return ilike("resId", resId.placeHolder());
    }

    private String buildMsgContextCondition() {
        if (Strings.isNullOrEmpty(constraints.getMsgContext())) {
            return null;
        }
        return ilike("tf.potEntryData.context", msgContext.placeHolder());
    }

    private String buildSourceCommentCondition(String commentToSearch) {
        if (Strings.isNullOrEmpty(commentToSearch)) {
            return null;
        }
        return ilike("tf.comment.comment", sourceComment.placeHolder());
    }

    private String buildTargetCommentCondition(String transComment) {
        if (Strings.isNullOrEmpty(transComment)) {
            return null;
        }
        return ilike("comment.comment", targetComment.placeHolder());
    }

    private String buildLastModifiedDateCondition() {
        DateTime changedBeforeTime = constraints.getChangedBefore();
        DateTime changedAfterTime = constraints.getChangedAfter();
        if (changedBeforeTime == null && changedAfterTime == null) {
            return null;
        }
        String changedAfter = null;
        String changedBefore = null;
        if (changedAfterTime != null) {
            changedAfter =
                    HqlCriterion.gt("lastChanged",
                            lastChangedAfter.placeHolder());
        }
        if (changedBeforeTime != null) {
            changedBefore =
                    HqlCriterion.lt("lastChanged",
                            lastChangedBefore.placeHolder());
        }
        return QueryBuilder.and(changedAfter, changedBefore);
    }

    protected String buildStateCondition() {
        if (constraints.getIncludedStates().hasAllStates()) {
            return null;
        }
        List<String> conjunction = Lists.newArrayList();
        conjunction.add(eq("textFlow", "tf"));
        conjunction.add(eq("locale", locale.placeHolder()));
        String textFlowAndLocaleRestriction =
                and(eq("textFlow", "tf"), eq("locale", locale.placeHolder()));

        String stateInListWhereClause =
                and(textFlowAndLocaleRestriction,
                        String.format("state in (%s)",
                                contentStateList.placeHolder()));
        String stateInListCondition =
                QueryBuilder.exists().from("HTextFlowTarget")
                        .where(stateInListWhereClause).toQueryString();
        if (constraints.getIncludedStates().hasNew()) {
            String nullTargetCondition =
                    String.format("%s not in indices(tf.targets)",
                            locale.placeHolder());
            if (hasSearch && constraints.isSearchInSource()) {

                nullTargetCondition =
                        and(nullTargetCondition,
                                contentCriterion.contentsCriterionAsString(
                                        "tf", constraints.isCaseSensitive(),
                                        Parameters.searchString.placeHolder()));
            }
            return QueryBuilder.or(stateInListCondition, nullTargetCondition);
        }
        return stateInListCondition;
    }

    protected String buildLastModifiedByCondition() {
        if (Strings.isNullOrEmpty(constraints.getLastModifiedByUser())) {
            return null;
        }
        return eq("lastModifiedBy.account.username",
                lastModifiedBy.placeHolder());
    }

    public Query setQueryParameters(Query textFlowQuery, HLocale hLocale) {
        if (documentId != null) {
            textFlowQuery.setParameter(Parameters.documentId.namedParam(),
                    documentId.getId());
        } else {
            textFlowQuery.setParameterList(documentIdList.namedParam(),
                    documentIds);
        }
        textFlowQuery.setParameter(locale.namedParam(), hLocale.getId());
        if (hasSearch) {
            textFlowQuery.setParameter(Parameters.searchString.namedParam(),
                    searchString);
        }
        if (!constraints.getIncludedStates().hasAllStates()) {
            textFlowQuery.setParameterList(contentStateList.namedParam(),
                    constraints.getIncludedStates().asList());
        }
        addWildcardSearchParamIfPresent(textFlowQuery, constraints.getResId(),
                resId);
        addWildcardSearchParamIfPresent(textFlowQuery,
                constraints.getMsgContext(), msgContext);
        addWildcardSearchParamIfPresent(textFlowQuery,
                constraints.getSourceComment(), sourceComment);
        addWildcardSearchParamIfPresent(textFlowQuery,
                constraints.getTransComment(), targetComment);
        if (!Strings.isNullOrEmpty(constraints.getLastModifiedByUser())) {
            textFlowQuery.setParameter(lastModifiedBy.namedParam(),
                    constraints.getLastModifiedByUser());
        }
        if (constraints.getChangedAfter() != null) {
            textFlowQuery.setParameter(lastChangedAfter.namedParam(),
                    constraints.getChangedAfter().toDate());
        }
        if (constraints.getChangedBefore() != null) {
            textFlowQuery.setParameter(lastChangedBefore.namedParam(),
                    constraints.getChangedBefore().toDate());
        }
        return textFlowQuery;
    }

    private static Query addWildcardSearchParamIfPresent(Query textFlowQuery,
            String filterProperty, Parameters filterParam) {
        if (!Strings.isNullOrEmpty(filterProperty)) {
            String escapedAndLowered =
                    HqlCriterion.escapeWildcard(filterProperty.toLowerCase());
            textFlowQuery.setParameter(filterParam.namedParam(),
                    HqlCriterion.match(escapedAndLowered, MatchMode.ANYWHERE));
        }
        return textFlowQuery;
    }

    enum Parameters {
        searchString, contentStateList, locale, documentId, documentIdList,
        resId, sourceComment, msgContext, targetComment, lastModifiedBy,
        lastChangedAfter, lastChangedBefore;

        public String placeHolder() {
            return ":" + name();
        }

        public String namedParam() {
            return name();
        }

    }
}
