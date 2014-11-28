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

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;

@Name("textFlowTargetHistoryDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
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
            Long versionId, Long personId, Date from, Date to) {

        StringBuilder queryString = new StringBuilder();
        queryString
                .append("select sum(wordCount), state, localeId from ")
                    .append("(select wordCount, id, state, localeId from ")
                        .append("(select h.state, tft.id, h.translated_by_id, tf.wordCount, locale.localeId ")
                        .append("from HTextFlowTargetHistory h ")
                        .append("JOIN HTextFlowTarget tft ON tft.id = h.target_id ")
                        .append("JOIN HLocale locale ON locale.id = tft.locale ")
                        .append("JOIN HTextFlow tf ON tf.id = tft.tf_id ")
                        .append("JOIN HDocument doc ON doc.id = tf.document_Id ")
                        .append("where doc.project_iteration_id =:versionId ")
                        .append("and h.state <> 0 ")
                        .append("and h.translated_by_id =:personId ")
                        .append("and h.lastChanged between :from and :to ")
                        .append("and tft.translated_by_id <> h.translated_by_id ")
                        .append("and h.lastChanged = ")
                            .append("(select max(lastChanged) from HTextFlowTargetHistory where h.target_id = target_id) ")
                        .append("union all ")
                        .append("select tft.state, tft.id, tft.translated_by_id, tf.wordCount, locale.localeId ")
                        .append("from HTextFlowTarget tft ")
                        .append("JOIN HLocale locale ON locale.id = tft.locale ")
                        .append("JOIN HTextFlow tf ON tf.id = tft.tf_id ")
                        .append("JOIN HDocument doc ON doc.id = tf.document_Id ")
                        .append("where doc.project_iteration_id =:versionId ")
                        .append("and tft.state <> 0 ")
                        .append("and tft.translated_by_id =:personId ")
                        .append("and tft.lastChanged between :from and :to ")
                        .append(") as target_history_union ")
                    .append("group by state, id, localeId, wordCount) as target_history_group ")
                .append("group by state, localeId");


        Query query = getSession().createSQLQuery(queryString.toString());
        query.setParameter("versionId", versionId);
        query.setParameter("personId", personId);
        query.setTimestamp("from", from);
        query.setTimestamp("to", to);
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

}
