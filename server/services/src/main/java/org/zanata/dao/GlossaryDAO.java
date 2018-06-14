/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.engine.ProjectionConstants;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.zanata.common.GlossarySortField;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.jpa.FullText;
import org.zanata.model.Glossary;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;
import org.zanata.webtrans.shared.rpc.LuceneQuery;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Named("glossaryDAO")
@RequestScoped
public class GlossaryDAO extends AbstractDAOImpl<HGlossaryEntry, Long> {
    private static final long serialVersionUID = -11578921177910314L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Inject @FullText
    private FullTextEntityManager entityManager;

    public GlossaryDAO() {
        super(HGlossaryEntry.class);
    }

    public GlossaryDAO(Session session) {
        super(HGlossaryEntry.class, session);
    }

    public HGlossaryEntry getEntryById(Long id) {
        return (HGlossaryEntry) getSession().load(HGlossaryEntry.class, id);
    }

    public List<HGlossaryEntry> getEntries(String qualifiedName) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("select term.glossaryEntry from HGlossaryTerm as term ")
            .append("where term.locale.localeId = term.glossaryEntry.srcLocale.localeId ")
            .append("and term.glossaryEntry.glossary.qualifiedName =:qualifiedName ")
            .append("order by term.content");
        Query query = getSession().createQuery(queryString.toString());
        query.setParameter("qualifiedName", qualifiedName)
                .setComment("GlossaryDAO.getEntries");
        @SuppressWarnings("unchecked")
        List<HGlossaryEntry> result = query.list();
        return result;
    }

    public int getEntriesCount(LocaleId srcLocale, String filter,
            String qualifiedName) {
        StringBuilder queryString = new StringBuilder();
        queryString
                .append("select count(term.glossaryEntry) from HGlossaryTerm as term ")
                .append("where term.glossaryEntry.srcLocale.localeId =:srcLocale ")
                .append("and term.locale.localeId = term.glossaryEntry.srcLocale.localeId ")
                .append("and term.glossaryEntry.glossary.qualifiedName =:qualifiedName");

        if (!StringUtils.isBlank(filter)) {
            queryString.append(" and lower(term.content) like lower(:filter)");
        }

        Query query = getSession().createQuery(queryString.toString())
                .setParameter("srcLocale", srcLocale)
                .setParameter("qualifiedName", qualifiedName)
                .setCacheable(true)
                .setComment("GlossaryDAO.getEntriesCount");

        if (!StringUtils.isBlank(filter)) {
            query.setParameter("filter", "%" + filter + "%");
        }
        Long totalCount = (Long) query.uniqueResult();
        return totalCount == null ? 0 : totalCount.intValue();
    }

    public List<HGlossaryEntry> getEntriesByLocale(LocaleId srcLocale,
            int offset, int maxResults, String filter,
            List<GlossarySortField> sortFields, String qualifiedName) {
        StringBuilder queryString = new StringBuilder();
        queryString
                .append("select term.glossaryEntry from HGlossaryTerm as term ")
                .append("where term.glossaryEntry.srcLocale.localeId =:srcLocale ")
                .append("and term.locale.localeId = term.glossaryEntry.srcLocale.localeId ")
                .append("and term.glossaryEntry.glossary.qualifiedName =:qualifiedName");

        if (!StringUtils.isBlank(filter)) {
            queryString.append(" and lower(term.content) like lower(:filter) escape '!'");
        }
        if (sortFields != null && !sortFields.isEmpty()) {
            queryString.append(" ORDER BY ");
            List<String> sortQuery = Lists.newArrayList();
            for (GlossarySortField sortField : sortFields) {
                String order = sortField.isAscending() ? " ASC" : " DESC";
                sortQuery.add(sortField.getEntityField() + order);
            }
            queryString.append(Joiner.on(", ").join(sortQuery));
        }

        Query query = getSession().createQuery(queryString.toString())
                .setParameter("srcLocale", srcLocale)
                .setParameter("qualifiedName", qualifiedName)
                .setCacheable(true)
                .setComment("GlossaryDAO.getEntriesByLocale");

        if (StringUtils.isNotBlank(filter)) {
            String escapeFilter = escapeQuery(filter);
            query.setParameter("filter", "%" + escapeFilter + "%");
        }
        query.setFirstResult(offset).setMaxResults(maxResults);
        @SuppressWarnings("unchecked")
        List<HGlossaryEntry> result = query.list();
        return result;
    }

    public int getEntryCountBySourceLocales(LocaleId localeId,
            String qualifiedName) {
        String queryBuilder = "select count(*) from HGlossaryEntry e " +
                "where e.srcLocale.localeId = :localeId " +
                "and e.glossary.qualifiedName = :qualifiedName ";
        Query query = getSession()
                .createQuery(queryBuilder)
                .setCacheable(true)
                .setParameter("localeId", localeId)
                .setParameter("qualifiedName", qualifiedName)
                .setComment("GlossaryDAO.getEntryCountBySourceLocales");

        Long totalCount = (Long) query.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public Map<LocaleId, Integer> getTranslationLocales(LocaleId srcLocale,
            String qualifiedName) {
        String queryBuilder =
                "select t.locale, count(*) from HGlossaryTerm t " +
                        "where t.locale.localeId <> t.glossaryEntry.srcLocale.localeId " +
                        "and t.glossaryEntry.srcLocale.localeId =:srcLocale " +
                        "and t.glossaryEntry.glossary.qualifiedName =:qualifiedName " +
                        "group by t.locale";

        Query query = getSession().createQuery(queryBuilder)
                .setParameter("srcLocale", srcLocale)
                .setParameter("qualifiedName", qualifiedName)
                .setComment("GlossaryDAO.getTranslationLocales");

        @SuppressWarnings("unchecked")
        List<Object[]> list = query.list();
        return generateLocaleStats(list);
    }

    /**
     * Returns map of statistics group by locale id.
     * Object[0] - HLocale
     * Object[1] - Integer word count
     */
    private Map<LocaleId, Integer> generateLocaleStats(List<Object[]> list) {
        Map<LocaleId, Integer> localeStats = Maps.newHashMap();
        for (Object[] obj : list) {
            HLocale locale = (HLocale) obj[0];
            Long count = (Long) obj[1];
            int countInt = count == null ? 0 : Math.toIntExact(count);
            localeStats.put(locale.getLocaleId(), countInt);
        }
        return localeStats;
    }

    public HGlossaryTerm getTermByEntryAndLocale(Long glossaryEntryId,
            LocaleId locale, String qualifiedName) {
        String queryBuilder = "from HGlossaryTerm as t " +
                "WHERE t.locale.localeId =:locale " +
                "AND t.glossaryEntry.id= :glossaryEntryId " +
                "AND t.glossaryEntry.glossary.qualifiedName =:qualifiedName";

        Query query = getSession().createQuery(queryBuilder)
                .setParameter("locale", locale)
                .setParameter("qualifiedName", qualifiedName)
                .setParameter("glossaryEntryId", glossaryEntryId)
                .setComment("GlossaryDAO.getTermByEntryAndLocale");
        return (HGlossaryTerm) query.uniqueResult();
    }

    public List<HGlossaryTerm> getTermByEntryId(Long entryId) {
        String queryBuilder = "from HGlossaryTerm as t " +
                "WHERE t.glossaryEntry.id= :entryId ";

        Query query = getSession().createQuery(queryBuilder)
                .setParameter("entryId", entryId)
                .setComment("GlossaryDAO.getTermByEntryId");
        @SuppressWarnings("unchecked")
        List<HGlossaryTerm> result = query.list();
        return result;
    }
    public HGlossaryEntry getEntryByTerm(String term, String qualifiedName) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("select term.glossaryEntry from HGlossaryTerm as term ")
                .append("where term.locale.localeId = term.glossaryEntry.srcLocale.localeId ")
                .append("and term.glossaryEntry.glossary.qualifiedName =:qualifiedName ")
                .append("and term.content =:term ");
        Query query = getSession().createQuery(queryString.toString());
        query.setParameter("qualifiedName", qualifiedName)
                .setParameter("term", term)
                .setComment("GlossaryDAO.getEntries")
                .setMaxResults(1);
        return (HGlossaryEntry) query.uniqueResult();
    }

    public HGlossaryEntry getEntryByContentHash(String contentHash,
            String qualifiedName) {
        String queryBuilder = "from HGlossaryEntry as e " +
                "WHERE e.contentHash =:contentHash " +
                "AND e.glossary.qualifiedName =:qualifiedName";

        Query query = getSession().createQuery(queryBuilder)
                .setParameter("contentHash", contentHash)
                .setParameter("qualifiedName", qualifiedName);
        query.setComment("GlossaryDAO.getEntryByContentHash");
        return (HGlossaryEntry) query.uniqueResult();
    }

    public List<HGlossaryTerm> findTermByIdList(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return Lists.newArrayList();
        }
        Query query =
                getSession().createQuery(
                        "FROM HGlossaryTerm WHERE id in (:idList)");
        query.setParameterList("idList", idList)
                .setCacheable(false)
                .setComment("GlossaryDAO.findTermByIdList");
        @SuppressWarnings("unchecked")
        List<HGlossaryTerm> result = query.list();
        return result;
    }

    /**
     * Perform lucene search in HGlossaryTerm in srcLocale
     * Object[0] - Float score
     * Object[1] - HGlossaryTerm srcTerm
     */
    public List<Object[]> getSearchResult(String searchText,
            SearchType searchType, LocaleId srcLocale, final int maxResult,
            String qualifiedName) throws ParseException {
        if (StringUtils.length(searchText) > LuceneQuery.QUERY_MAX_LENGTH) {
            throw new RuntimeException(
                "Query string exceed max length: " + LuceneQuery.QUERY_MAX_LENGTH + "='" +
                    StringUtils.left(searchText, 80) + "'");
        }

        String queryText;

        switch (searchType) {
        case RAW:
            queryText = searchText;
            break;
        case FUZZY:
            // search by N-grams
            queryText = QueryParser.escape(searchText);
            break;
        case EXACT:
            queryText = "\"" + QueryParser.escape(searchText) + "\"";
            break;
        default:
            throw new RuntimeException("Unknown query type: " + searchType);
        }
        if (StringUtils.isEmpty(queryText)) {
            return Lists.newArrayList();
        }
        QueryParser parser = new QueryParser("content", new StandardAnalyzer());
        org.apache.lucene.search.Query textQuery = parser.parse(queryText);

        TermQuery qualifiedNameQuery =
                new TermQuery(new Term(IndexFieldLabels.GLOSSARY_QUALIFIED_NAME,
                        qualifiedName));

        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(textQuery, BooleanClause.Occur.MUST)
                .add(qualifiedNameQuery, BooleanClause.Occur.MUST).build();

        FullTextQuery ftQuery =
                entityManager.createFullTextQuery(booleanQuery,
                        HGlossaryTerm.class);
        ftQuery.enableFullTextFilter("glossaryLocaleFilter").setParameter(
                "locale", srcLocale);
        ftQuery.setProjection(
                ProjectionConstants.SCORE,
                ProjectionConstants.THIS,
                ProjectionConstants.OBJECT_CLASS,
                ProjectionConstants.ID);
        @SuppressWarnings("unchecked")
        List<Object[]> matches =
                ftQuery.setMaxResults(maxResult).getResultList();
        return matches;
    }

    @NativeQuery
    public int deleteAllEntries(String qualifiedName) {

        /*
         * DELETE FROM HGlossaryTerm where glossaryEntryId in (select e.id from HGlossaryEntry e INNER JOIN Glossary g on e.glossaryId = g.id where g.qualifiedName =: qualifiedName)
         */

        String deleteTermQuery =
                "DELETE FROM HGlossaryTerm WHERE glossaryEntryId IN (SELECT e.id FROM HGlossaryEntry e INNER JOIN Glossary g ON e.glossaryId = g.id WHERE g.qualifiedName =:qualifiedName)";
        Query query = getSession().createSQLQuery(deleteTermQuery);
        query.setParameter("qualifiedName", qualifiedName)
                .setComment("GlossaryDAO.deleteAllEntries-terms");
        int rowCount = query.executeUpdate();

        String deleteEntryQuery =
                "DELETE FROM HGlossaryEntry where glossaryId IN (SELECT id FROM Glossary WHERE qualifiedName =:qualifiedName)";
        Query query2 = getSession().createSQLQuery(deleteEntryQuery);
        query2.setParameter("qualifiedName", qualifiedName)
                .setComment("GlossaryDAO.deleteAllEntries-entries");
        query2.executeUpdate();

        return rowCount;
    }

    public Glossary getGlossaryByQualifiedName(String qualifiedName) {
        Query query = getSession().createQuery(
                "from Glossary where qualifiedName =:qualifiedName");
        query.setParameter("qualifiedName", qualifiedName)
                .setComment("GlossaryDAO.getGlossaryByQualifiedName");
        return (Glossary) query.uniqueResult();
    }

    public void persistGlossary(Glossary glossary) {
        getSession().saveOrUpdate(glossary);
    }
}
