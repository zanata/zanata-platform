package org.zanata.search;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.zanata.model.HLocale;
import org.zanata.webtrans.shared.model.ContentStateGroup;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class FilterConstraintToModalNavigationQuery {
    private final Long documentId;
    private final HLocale hLocale;
    private final ResultTransformer resultTransformer;
    private final FilterConstraints constraints;
    private boolean hasSearchString;

    public FilterConstraintToModalNavigationQuery(Long documentId,
            HLocale hLocale, ResultTransformer resultTransformer,
            FilterConstraints constraints) {
        this.documentId = documentId;
        this.hLocale = hLocale;
        this.resultTransformer = resultTransformer;
        this.constraints = constraints;
        hasSearchString = !Strings.isNullOrEmpty(constraints.getSearchString());
    }

    public String toSql() {
        StringBuilder queryBuilder = new StringBuilder();
        // I can't write a HQL or criteria to achieve the same result. I gave
        // up...
        queryBuilder
                .append("SELECT tf.id, tft.state FROM HTextFlow tf ")
                .append(" LEFT JOIN HTextFlowTarget tft on tf.id = tft.tf_id AND locale = :locale")
                .append(" WHERE tf.document_id = :docId AND tf.obsolete = 0");
        queryBuilder.append(" AND ").append(
                buildContentStateCondition(constraints.getIncludedStates(),
                        "tft"));
        if (hasSearchString) {
            queryBuilder
                    .append(" AND (")
                    // search in source
                    .append(buildSearchCondition(constraints.getSearchString(),
                            "tf"))
                    .append(" OR ")
                    // search in target
                    .append(buildSearchCondition(constraints.getSearchString(),
                            "tft")).append(")");
        }
        queryBuilder.append(" ORDER BY tf.pos");

        log.debug("get navigation SQL query: {}", queryBuilder);
        return queryBuilder.toString();
    }

    public Query setParameters(SQLQuery query) {
        query.addScalar("id", StandardBasicTypes.LONG).addScalar("state")
                .setParameter("docId", documentId)
                .setParameter("locale", hLocale.getId());
        if (hasSearchString) {
            query.setParameter("searchstringlowercase", "%"
                    + constraints.getSearchString().toLowerCase() + "%");
        }
        query.setResultTransformer(resultTransformer);
        query.setComment("TextFlowDAO.getNavigationByDocumentId");
        return query;
    }

    /**
     * Build a SQL query condition that is true only for text flows with one of
     * the given states.
     *
     * @param includedStates
     *            states of targets that should return true
     * @param hTextFlowTargetTableAlias
     *            alias being used for the target table in the current query
     * @return a valid SQL query condition that is wrapped in parentheses if
     *         necessary
     */
    protected static String buildContentStateCondition(
            ContentStateGroup includedStates, String hTextFlowTargetTableAlias) {
        if (includedStates.hasAllStates()) {
            return "1";
        }
        if (includedStates.hasNoStates()) {
            return "0";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("(");
        List<String> conditions = Lists.newArrayList();
        final String stateColumn = hTextFlowTargetTableAlias + ".state";
        if (includedStates.hasNew()) {
            conditions.add(stateColumn + "=0 or " + stateColumn + " is null");
        }
        if (includedStates.hasFuzzy()) {
            conditions.add(stateColumn + "=1");
        }
        if (includedStates.hasTranslated()) {
            conditions.add(stateColumn + "=2");
        }
        if (includedStates.hasApproved()) {
            conditions.add(stateColumn + "=3");
        }
        if (includedStates.hasRejected()) {
            conditions.add(stateColumn + "=4");
        }
        Joiner joiner = Joiner.on(" or ");
        joiner.appendTo(builder, conditions);
        builder.append(")");
        return builder.toString();
    }

    /**
     * This will build a SQL query condition in where clause. It can be used to
     * search string in content0, content1 ... content5 in HTextFlow or
     * HTextFlowTarget. If search term is empty it will return '1'
     *
     * @param searchString
     *            search term
     * @param alias
     *            table name alias
     * @return '1' if searchString is empty or a SQL condition clause with
     *         lower(contentX) like '%searchString%' in parentheses '()' joined
     *         by 'or'
     */
    protected static String buildSearchCondition(String searchString,
            String alias) {
        if (Strings.isNullOrEmpty(searchString)) {
            return "1";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        List<String> conditions = Lists.newArrayList();
        for (int i = 0; i < 6; i++) {
            conditions.add("lower(" + alias + ".content" + i
                    + ") LIKE :searchstringlowercase");
        }
        Joiner joiner = Joiner.on(" or ");
        joiner.appendTo(builder, conditions);
        builder.append(")");
        return builder.toString();
    }
}
