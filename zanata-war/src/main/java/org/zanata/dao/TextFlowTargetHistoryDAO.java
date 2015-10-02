/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.dao;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import javax.inject.Named;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.zanata.common.ContentState;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

@Named("textFlowTargetHistoryDAO")

@javax.enterprise.context.Dependent
public class TextFlowTargetHistoryDAO extends
        AbstractDAOImpl<HTextFlowTargetHistory, Long> {

    public TextFlowTargetHistoryDAO() {
        super(HTextFlowTargetHistory.class);
    }

    public TextFlowTargetHistoryDAO(Session session) {
        super(HTextFlowTargetHistory.class, session);
    }

    /**
     * Query to get total wordCount of a person(translated_by_id) from
     * HTextFlowTarget union HTextFlowTargetHistory tables
     * in a project-version, within given date range group by state and locale.
     *
     * HTextFlowTargetHistory:
     * gets latest of all records translated from user in given version,
     * locale and dateRange and its target is not translated by same person.
     *
     * HTextFlowTarget:
     * gets all records translated from user in given version, locale and
     * dateRange.
     *
     * @param versionId HProjectIteration identifier
     * @param personId HPerson identifier
     * @param from start of date range
     * @param to end of date range
     *
     * @return list of Object[wordCount][contentState][localeId]
     */
    @NativeQuery
    public List<Object[]> getUserContributionStatisticInVersion(
            Long versionId, Long personId, Date fromDate, Date toDate) {

        StringBuilder queryString = new StringBuilder();
        queryString
                .append("select sum(wordCount), state, localeId from ")
                    .append("(select wordCount, id, state, localeId from ")
                        .append(
                            "(select h.state, tft.id, h.translated_by_id, tf.wordCount, locale.localeId ")
                        .append("from HTextFlowTargetHistory h ")
                        .append("JOIN HTextFlowTarget tft ON tft.id = h.target_id ")
                        .append("JOIN HLocale locale ON locale.id = tft.locale ")
                        .append("JOIN HTextFlow tf ON tf.id = tft.tf_id ")
                        .append("JOIN HDocument doc ON doc.id = tf.document_Id ")
                        .append("where doc.project_iteration_id =:versionId ")
                        .append("and h.state <> :untranslated ")
                        .append("and h.translated_by_id =:personId ")
                        .append("and h.lastChanged between :fromDate and :toDate ")
                        .append("and h.automatedEntry =:automatedEntry ")
                        .append("and tft.translated_by_id <> h.translated_by_id ")
                        .append("and h.lastChanged = ")
                            .append(
                                "(select max(lastChanged) from HTextFlowTargetHistory where h.target_id = target_id) ")
                        .append("union all ")
                        .append("select tft.state, tft.id, tft.translated_by_id, tf.wordCount, locale.localeId ")
                        .append("from HTextFlowTarget tft ")
                        .append("JOIN HLocale locale ON locale.id = tft.locale ")
                        .append("JOIN HTextFlow tf ON tf.id = tft.tf_id ")
                        .append("JOIN HDocument doc ON doc.id = tf.document_Id ")
                        .append("where doc.project_iteration_id =:versionId ")
                        .append("and tft.state <> :untranslated ")
                        .append("and tft.automatedEntry =:automatedEntry ")
                        .append("and tft.translated_by_id =:personId ")
                        .append("and tft.lastChanged between :fromDate and :toDate ")
                        .append(") as target_history_union ")
                    .append("group by state, id, localeId, wordCount) as target_history_group ")
                .append("group by state, localeId");


        Query query = getSession().createSQLQuery(queryString.toString());
        query.setParameter("versionId", versionId);
        query.setParameter("personId", personId);
        query.setInteger("untranslated", ContentState.New.ordinal());
        query.setBoolean("automatedEntry", false);
        query.setTimestamp("fromDate", fromDate);
        query.setTimestamp("toDate", toDate);
        query.setComment("textFlowTargetHistoryDAO.getUserContributionStatisticInVersion");
        return query.list();
    }

    public boolean findContentInHistory(HTextFlowTarget target,
            List<String> contents) {
        // Ordinal parameters can't be used in NamedQueries due to the following
        // bug:
        // https://hibernate.onjira.com/browse/HHH-5653
        Query query;

        // use named queries for the smaller more common cases
        if (contents.size() <= 6) {
            query =
                    getSession().getNamedQuery(
                            HTextFlowTargetHistory
                                    .getQueryNameMatchingHistory(contents
                                            .size()));
        } else {
            StringBuilder queryStr =
                    new StringBuilder(
                            "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount");
            for (int i = 0; i < contents.size(); i++) {
                queryStr.append(" and contents[" + i + "] = :content" + i);
            }
            query = getSession().createQuery(queryStr.toString());
        }
        query.setParameter("tft", target);
        query.setParameter("contentCount", contents.size());
        int paramPos = 0;
        for (String c : contents) {
            query.setParameter("content" + paramPos++, c);
        }
        query.setComment("TextFlowTargetHistoryDAO.findContentInHistory-"
                + contents.size());
        return (Long) query.uniqueResult() != 0;
    }

    public boolean findConflictInHistory(HTextFlowTarget target,
            Integer verNum, String username) {
        Query query =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget.id =:id and t.textFlowRevision > :ver and t.lastModifiedBy.account.username != :username");
        query.setParameter("id", target.getId());
        query.setParameter("ver", verNum);
        query.setParameter("username", username);
        query.setComment("TextFlowTargetHistoryDAO.findConflictInHistory");
        Long count = (Long) query.uniqueResult();
        return count != 0;
    }

    /**
     * Query to get total wordCount of a person(translated_by_id or
     * reviewed_by_id) from HTextFlowTarget union HTextFlowTargetHistory tables
     * within given date range group by lastChangeDate (date portion only),
     * project version, locale and state.
     *
     * HTextFlowTargetHistory: gets all records translated from user in any
     * version, any locale and dateRange.
     *
     * HTextFlowTarget: gets all records translated from user in any version,
     * any locale and dateRange.
     *
     * @param user
     *            a HPerson person
     * @param fromDate
     *            date from
     * @param toDate
     *            date to
     *
     * @param userZoneOpt
     *            optional DateTimeZone of the user. Only present if it's
     *            different from system time zone
     * @param systemZone
     *            current system time zone
     * @param resultTransformer
     *            result transformer to transform query results
     * @return a list of transformed object
     */
    @NativeQuery(value = "need to use union", specificTo = "mysql due to usage of date() and convert_tz() functions.")
    public <T> List<T> getUserTranslationMatrix(
            HPerson user, DateTime fromDate, DateTime toDate,
            Optional<DateTimeZone> userZoneOpt, DateTimeZone systemZone,
            ResultTransformer resultTransformer) {
        // @formatter:off
        String queryHistory = "select history.id, iter.id as iteration, tft.locale as locale, tf.wordCount as wordCount, history.state as state, history.lastChanged as lastChanged " +
                "  from HTextFlowTargetHistory history " +
                "    join HTextFlowTarget tft on tft.id = history.target_id " +
                "    join HTextFlow tf on tf.id = tft.tf_id " +
                "    join HDocument doc on doc.id = tf.document_id " +
                "    join HProjectIteration iter on iter.id = doc.project_iteration_id " +
                "  where history.lastChanged >= :fromDate and history.lastChanged <= :toDate " +
                "    and history.last_modified_by_id = :user and (history.translated_by_id is not null or history.reviewed_by_id is not null)" +
                "    and history.state <> :untranslated and history.state <> :rejected and history.automatedEntry =:automatedEntry";

        String queryTarget = "select tft.id, iter.id as iteration, tft.locale as locale, tf.wordCount as wordCount, tft.state as state, tft.lastChanged as lastChanged " +
                "  from HTextFlowTarget tft " +
                "    join HTextFlow tf on tf.id = tft.tf_id " +
                "    join HDocument doc on doc.id = tf.document_id " +
                "    join HProjectIteration iter on iter.id = doc.project_iteration_id " +
                "  where tft.lastChanged >= :fromDate and tft.lastChanged <= :toDate " +
                "    and tft.last_modified_by_id = :user and (tft.translated_by_id is not null or tft.reviewed_by_id is not null)" +
                "    and tft.state <> :untranslated and tft.state <> :rejected and tft.automatedEntry =:automatedEntry";

        String convertedLastChanged = convertTimeZoneFunction("lastChanged",
                userZoneOpt, systemZone);
        // @formatter:on
        String dateOfLastChanged = stripTimeFromDateTimeFunction(convertedLastChanged);
        String queryString =
                "select " + dateOfLastChanged + ", iteration, locale, state, sum(wordCount)" +
                        "  from (" +
                        "  (" + queryHistory + ") union (" + queryTarget + ")" +
                        "  ) as all_translation" +
                        "  group by " + dateOfLastChanged + ", iteration, locale, state " +
                        "  order by lastChanged, iteration, locale, state";
        Query query = getSession().createSQLQuery(queryString)
            .setParameter("user", user.getId())
            .setInteger("untranslated", ContentState.New.ordinal())
                .setInteger("rejected", ContentState.Rejected.ordinal())
            .setBoolean("automatedEntry", false)
            .setTimestamp("fromDate", fromDate.toDate())
            .setTimestamp("toDate", toDate.toDate())
            .setResultTransformer(resultTransformer);
        return query.list();
    }

    @VisibleForTesting
    protected String convertTimeZoneFunction(String columnName,
            Optional<DateTimeZone> userZoneOpt, DateTimeZone systemZone) {
        if (userZoneOpt.isPresent()) {
            String userOffset = getOffsetAsString(userZoneOpt.get());
            String systemOffset = getOffsetAsString(systemZone);
            return String.format("CONVERT_TZ(%s, '%s', '%s')", columnName, systemOffset, userOffset);
        }
        // no need to convert timezone
        return columnName;
    }

    // This is so we can override it in test and be able to test it against h2
    @VisibleForTesting
    protected String stripTimeFromDateTimeFunction(String columnName) {
        return "date(" + columnName + ")";
    }

    @SuppressWarnings("unchecked")
    private <T> T loadById(Object object, Class<T> entityClass) {
        return (T) getSession().byId(entityClass).load(
                ((BigInteger) object).longValue());
    }

    private static String getOffsetAsString(DateTimeZone zone) {
        int standardOffset = zone.getStandardOffset(0);
        String prefix = "";
        if (standardOffset < 0) {
            prefix = "-";
            standardOffset = -standardOffset;
        }
        return String.format("%s%02d:00", prefix,
                TimeUnit.MILLISECONDS.toHours(standardOffset));
    }

}
