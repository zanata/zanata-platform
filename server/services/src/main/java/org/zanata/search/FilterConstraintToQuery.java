package org.zanata.search;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.joda.time.DateTime;
import org.zanata.model.HLocale;
import org.zanata.util.HqlCriterion;
import org.zanata.util.QueryBuilder;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.search.FilterConstraints;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import static org.zanata.search.FilterConstraintToQuery.Parameters.*;
import static org.zanata.util.HqlCriterion.eq;
import static org.zanata.util.HqlCriterion.ne;
import static org.zanata.util.HqlCriterion.isNull;
import static org.zanata.util.HqlCriterion.escapeWildcard;
import static org.zanata.util.HqlCriterion.ilike;
import static org.zanata.util.HqlCriterion.match;
import static org.zanata.util.QueryBuilder.and;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FilterConstraintToQuery {
    private final FilterConstraints constraints;
    private final boolean hasSearch;
    private String searchString;
    private DocumentId documentId;
    private Collection<Long> documentIds;
    private ContentCriterion contentCriterion = new ContentCriterion();

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
            String term = constraints.isCaseSensitive()
                    ? constraints.getSearchString()
                    : constraints.getSearchString().toLowerCase();
            term = escapeWildcard(term);
            searchString = match(term, MatchMode.ANYWHERE);
        }
        contentCriterion = new ContentCriterion()
                .withCaseSensitive(constraints.isCaseSensitive());
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
     * This builds a query for constructing TransUnit in editor. Executing the
     * query will returns a list of HTextFlow objects.
     *
     * @return the HQL query
     */
    public String toEntityQuery() {
        String docIdCondition;
        if (documentId != null) {
            docIdCondition =
                    eq("tf.document.id", Parameters.DocumentId.placeHolder());
        } else {
            docIdCondition =
                    "tf.document.id in (" + DocumentIdList.placeHolder() + ")";
        }
        return buildQuery("distinct tf", docIdCondition);
    }

    private String buildQuery(String selectStatement, String docIdCondition) {
        String obsoleteCondition = eq("tf.obsolete", "0");
        String searchCondition = buildSearchCondition();
        String stateCondition = buildStateCondition();
        String otherSourceCondition = buildSourceConditionsOtherThanSearch();
        String otherTargetCondition = buildTargetConditionsOtherThanSearch();
        QueryBuilder query = QueryBuilder.select(selectStatement)
                .from("HTextFlow tf").leftJoin("tf.targets tfts")
                .with(eq("tfts.index", Locale.placeHolder()))
                .where(and(obsoleteCondition, docIdCondition, searchCondition,
                        stateCondition, otherSourceCondition,
                        otherTargetCondition))
                .orderBy("tf.pos");
        return query.toQueryString();
    }

    /**
     * This builds a query for editor modal navigation. It only select text flow
     * id and text flow target content state (text flow pos is also in select
     * but just for ordering).
     *
     * @return the HQL query
     */
    public String toModalNavigationQuery() {
        String docIdCondition =
                eq("tf.document.id", Parameters.DocumentId.placeHolder());
        return buildQuery(
                "distinct tf.id as id, (case when tfts is null then 0 else tfts.state end) as state, tf.pos as pos, tf.resId as resId",
                docIdCondition);
    }

    protected String buildSearchCondition() {
        if (!hasSearch) {
            return null;
        }
        String searchInSourceCondition = null;
        if (constraints.isSearchInSource()) {
            searchInSourceCondition = contentCriterion.withEntityAlias("tf")
                    .contentsCriterionAsString();
        }
        String searchInTargetCondition = null;
        if (constraints.isSearchInTarget()) {
            List<String> targetConjunction = Lists.newArrayList();
            targetConjunction.add(contentCriterion.contentsCriterionAsString());
            targetConjunction.add(eq("textFlow", "tf"));
            targetConjunction.add(eq("locale", Locale.placeHolder()));
            searchInTargetCondition = QueryBuilder.exists()
                    .from("HTextFlowTarget")
                    .where(QueryBuilder.and(targetConjunction)).toQueryString();
        }
        return QueryBuilder.or(searchInSourceCondition,
                searchInTargetCondition);
    }

    protected boolean needToQueryNullTarget() {
        return isExcludeSearchTerm(constraints.getLastModifiedByUser());
    }

    protected String buildSourceConditionsOtherThanSearch() {
        List<String> sourceConjunction = Lists.newArrayList();
        addToJunctionIfNotNull(sourceConjunction,
                buildSourceCommentCondition(constraints.getSourceComment()));
        addToJunctionIfNotNull(sourceConjunction, buildMsgContextCondition());
        addToJunctionIfNotNull(sourceConjunction, buildResourceIdCondition());
        if (sourceConjunction.isEmpty()) {
            return null;
        }
        return QueryBuilder.and(sourceConjunction);
    }

    protected String buildTargetConditionsOtherThanSearch() {
        List<String> targetConjunction = Lists.newArrayList();
        addToJunctionIfNotNull(targetConjunction,
                buildLastModifiedByCondition());
        addToJunctionIfNotNull(targetConjunction,
                buildTargetCommentCondition(constraints.getTransComment()));
        addToJunctionIfNotNull(targetConjunction,
                buildLastModifiedDateCondition());
        if (targetConjunction.isEmpty()) {
            return null;
        }
        String textFlowTargetJoin = eq("tft.textFlow", "tf");
        String localeJoin = eq("tft.locale", Locale.placeHolder());
        targetConjunction.add(textFlowTargetJoin);
        targetConjunction.add(localeJoin);
        String existQuery = QueryBuilder.exists().from("HTextFlowTarget tft")
                .leftJoin("tft.lastModifiedBy.account acc")
                .where(and(targetConjunction)).toQueryString();
        if (!needToQueryNullTarget()) {
            return existQuery;
        }
        String notExistQuery = QueryBuilder.notExists()
                .from("HTextFlowTarget tft")
                .leftJoin("tft.lastModifiedBy.account acc")
                .where(and(textFlowTargetJoin, localeJoin)).toQueryString();
        return QueryBuilder.or(existQuery, notExistQuery);
    }

    private static boolean addToJunctionIfNotNull(List<String> junction,
            String criterion) {
        if (criterion != null) {
            junction.add(criterion);
            return true;
        }
        return false;
    }

    private String buildResourceIdCondition() {
        if (Strings.isNullOrEmpty(constraints.getResId())) {
            return null;
        }
        return eq("resId", ResId.placeHolder());
    }

    private String buildMsgContextCondition() {
        if (Strings.isNullOrEmpty(constraints.getMsgContext())) {
            return null;
        }
        return ilike("tf.potEntryData.context", MsgContext.placeHolder());
    }

    private String buildSourceCommentCondition(String commentToSearch) {
        if (Strings.isNullOrEmpty(commentToSearch)) {
            return null;
        }
        return ilike("tf.comment.comment", SourceComment.placeHolder());
    }

    private String buildTargetCommentCondition(String transComment) {
        if (Strings.isNullOrEmpty(transComment)) {
            return null;
        }
        return ilike("tft.comment.comment", TargetComment.placeHolder());
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
            changedAfter = HqlCriterion.gt("tft.lastChanged",
                    LastChangedAfter.placeHolder());
        }
        if (changedBeforeTime != null) {
            changedBefore = HqlCriterion.lt("tft.lastChanged",
                    LastChangedBefore.placeHolder());
        }
        return QueryBuilder.and(changedAfter, changedBefore);
    }

    protected String buildStateCondition() {
        if (constraints.getIncludedStates().hasAllStates()) {
            return null;
        }
        List<String> conjunction = Lists.newArrayList();
        conjunction.add(eq("textFlow", "tf"));
        conjunction.add(eq("locale", Locale.placeHolder()));
        String textFlowAndLocaleRestriction =
                and(eq("textFlow", "tf"), eq("locale", Locale.placeHolder()));
        String stateInListWhereClause = and(textFlowAndLocaleRestriction,
                String.format("state in (%s)", ContentStateList.placeHolder()));
        String stateInListCondition =
                QueryBuilder.exists().from("HTextFlowTarget")
                        .where(stateInListWhereClause).toQueryString();
        if (constraints.getIncludedStates().hasNew()) {
            String nullTargetCondition = String.format(
                    "%s not in indices(tf.targets)", Locale.placeHolder());
            if (hasSearch && constraints.isSearchInSource()) {
                nullTargetCondition = and(nullTargetCondition, contentCriterion
                        .withEntityAlias("tf").contentsCriterionAsString());
            }
            return QueryBuilder.or(stateInListCondition, nullTargetCondition);
        }
        return stateInListCondition;
    }

    protected String buildLastModifiedByCondition() {
        if (Strings.isNullOrEmpty(constraints.getLastModifiedByUser())) {
            return null;
        }
        if (!isExcludeSearchTerm(constraints.getLastModifiedByUser())) {
            return eq("acc.username", LastModifiedBy.placeHolder());
        }
        String nullLastModifiedByCondition = isNull("tft.lastModifiedBy");
        String excludeUsernameCondition =
                ne("acc.username", LastModifiedBy.placeHolder());
        return QueryBuilder.or(nullLastModifiedByCondition,
                excludeUsernameCondition);
    }
    // check if the term have exclude sign '-'

    private boolean isExcludeSearchTerm(@Nonnull String term) {
        return StringUtils.isEmpty(term) ? false
                : (term.startsWith("-") && term.length() > 1);
    }
    // remove exclude sign '-' from term

    private String getExcludeSearchTerm(@Nonnull String term) {
        if (isExcludeSearchTerm(term)) {
            return term.substring(1); // remove '-' sign in front
        }
        return term;
    }

    public Query setQueryParameters(Query textFlowQuery, HLocale hLocale) {
        if (documentId != null) {
            textFlowQuery.setParameter(Parameters.DocumentId.namedParam(),
                    documentId.getId());
        } else {
            textFlowQuery.setParameterList(DocumentIdList.namedParam(),
                    documentIds);
        }
        textFlowQuery.setParameter(Locale.namedParam(), hLocale.getId());
        if (hasSearch) {
            textFlowQuery.setParameter(Parameters.SearchString.namedParam(),
                    searchString);
        }
        if (!constraints.getIncludedStates().hasAllStates()) {
            textFlowQuery.setParameterList(ContentStateList.namedParam(),
                    constraints.getIncludedStates().asList());
        }
        addExactMatchParamIfPresent(textFlowQuery, constraints.getResId(),
                ResId);
        addWildcardSearchParamIfPresent(textFlowQuery,
                constraints.getMsgContext(), MsgContext);
        addWildcardSearchParamIfPresent(textFlowQuery,
                constraints.getSourceComment(), SourceComment);
        addWildcardSearchParamIfPresent(textFlowQuery,
                constraints.getTransComment(), TargetComment);
        String lastModifiedByUser =
                getExcludeSearchTerm(constraints.getLastModifiedByUser());
        addExactMatchParamIfPresent(textFlowQuery, lastModifiedByUser,
                LastModifiedBy);
        if (constraints.getChangedAfter() != null) {
            textFlowQuery.setParameter(LastChangedAfter.namedParam(),
                    constraints.getChangedAfter().toDate());
        }
        if (constraints.getChangedBefore() != null) {
            textFlowQuery.setParameter(LastChangedBefore.namedParam(),
                    constraints.getChangedBefore().toDate());
        }
        return textFlowQuery;
    }

    private static void addExactMatchParamIfPresent(Query textFlowQuery,
            String filterProperty, Parameters filterParam) {
        if (!Strings.isNullOrEmpty(filterProperty)) {
            textFlowQuery.setParameter(filterParam.namedParam(),
                    filterProperty);
        }
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
        SearchString,
        ContentStateList,
        Locale,
        DocumentId,
        DocumentIdList,
        ResId,
        SourceComment,
        MsgContext,
        TargetComment,
        LastModifiedBy,
        LastChangedAfter,
        LastChangedBefore;

        public String placeHolder() {
            return ":" + name();
        }

        public String namedParam() {
            return name();
        }
    }

    void setContentCriterion(final ContentCriterion contentCriterion) {
        this.contentCriterion = contentCriterion;
    }
}
