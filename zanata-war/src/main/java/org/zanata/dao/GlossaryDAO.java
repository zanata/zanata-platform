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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.LocaleId;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Named("glossaryDAO")

@javax.enterprise.context.Dependent
public class GlossaryDAO extends AbstractDAOImpl<HGlossaryEntry, Long> {
    @Inject
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

    @SuppressWarnings("unchecked")
    public List<HGlossaryEntry> getEntriesByLocaleId(LocaleId locale) {
        Query query =
                getSession()
                        .createQuery(
                                "from HGlossaryEntry as e WHERE e.id IN (SELECT t.glossaryEntry.id FROM HGlossaryTerm as t WHERE t.locale.localeId= :localeId)");
        query.setParameter("localeId", locale);
        query.setComment("GlossaryDAO.getEntriesByLocaleId");
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<HGlossaryEntry> getEntries() {
        Query query = getSession().createQuery("from HGlossaryEntry");
        query.setComment("GlossaryDAO.getEntries");
        return query.list();
    }

    public HGlossaryTerm getTermByEntryAndLocale(Long glossaryEntryId,
            LocaleId locale) {
        Query query =
                getSession()
                        .createQuery(
                                "from HGlossaryTerm as t WHERE t.locale.localeId= :locale AND glossaryEntry.id= :glossaryEntryId");
        query.setParameter("locale", locale);
        query.setParameter("glossaryEntryId", glossaryEntryId);
        query.setComment("GlossaryDAO.getTermByEntryAndLocale");
        return (HGlossaryTerm) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<HGlossaryTerm> getTermByGlossaryEntryId(Long glossaryEntryId) {
        Query query =
                getSession()
                        .createQuery(
                                "from HGlossaryTerm as t WHERE t.glossaryEntry.id= :glossaryEntryId");
        query.setParameter("glossaryEntryId", glossaryEntryId);
        query.setComment("GlossaryDAO.getTermByGlossaryEntryId");
        return query.list();
    }

    public HGlossaryEntry getEntryBySrcLocaleAndContent(LocaleId localeid,
            String content) {
        Query query =
                getSession()
                        .createQuery(
                                "from HGlossaryEntry as e "
                                        + "WHERE e.srcLocale.localeId= :localeid "
                                        + "AND e.id IN "
                                        + "(SELECT t.glossaryEntry.id FROM HGlossaryTerm as t "
                                        + "WHERE t.locale.localeId=e.srcLocale.localeId "
                                        + "AND t.content= :content)");
        query.setParameter("localeid", localeid);
        query.setParameter("content", content);
        query.setComment("GlossaryDAO.getEntryBySrcLocaleAndContent");
        return (HGlossaryEntry) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<HGlossaryTerm> findByIdList(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<HGlossaryTerm>();
        }
        Query query =
                getSession().createQuery(
                        "FROM HGlossaryTerm WHERE id in (:idList)");
        query.setParameterList("idList", idList);
        query.setCacheable(false).setComment("GlossaryDAO.getByIdList");
        return query.list();
    }

    public List<Object[]> getSearchResult(String searchText,
            SearchType searchType, LocaleId srcLocale, final int maxResult)
            throws ParseException {
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
            return new ArrayList<Object[]>();
        }

        QueryParser parser =
                new QueryParser(Version.LUCENE_29, "content",
                        new StandardAnalyzer(Version.LUCENE_29));
        org.apache.lucene.search.Query textQuery = parser.parse(queryText);
        FullTextQuery ftQuery =
                entityManager.createFullTextQuery(textQuery,
                        HGlossaryTerm.class);
        ftQuery.enableFullTextFilter("glossaryLocaleFilter").setParameter(
                "locale", srcLocale);
        ftQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);
        @SuppressWarnings("unchecked")
        List<Object[]> matches =
                ftQuery.setMaxResults(maxResult).getResultList();
        return matches;
    }

    public Map<HLocale, Integer> getGlossaryTermCountByLocale() {
        Map<HLocale, Integer> result = new HashMap<HLocale, Integer>();

        Query query =
                getSession()
                        .createQuery(
                                "select term.locale, count(*) from HGlossaryTerm term GROUP BY term.locale.localeId");
        query.setComment("GlossaryDAO.getGlossaryTermCountByLocale");

        @SuppressWarnings("unchecked")
        List<Object[]> list = query.list();

        for (Object[] obj : list) {
            HLocale locale = (HLocale) obj[0];
            Long count = (Long) obj[1];
            int countInt = count == null ? 0 : count.intValue();
            result.put(locale, countInt);
        }

        return result;
    }

    public int deleteAllEntries() {
        Query query = getSession().createQuery("Delete HTermComment");
        query.setComment("GlossaryDAO.deleteAllEntries-comments");
        query.executeUpdate();

        Query query2 = getSession().createQuery("Delete HGlossaryTerm");
        query2.setComment("GlossaryDAO.deleteAllEntries-terms");
        int rowCount = query2.executeUpdate();

        Query query3 = getSession().createQuery("Delete HGlossaryEntry");
        query3.setComment("GlossaryDAO.deleteAllEntries-entries");
        query3.executeUpdate();

        return rowCount;
    }

    public int deleteAllEntries(LocaleId targetLocale) {
        Query query =
                getSession()
                        .createQuery(
                                "Delete HTermComment c WHERE c.glossaryTerm.id IN (SELECT t.id FROM HGlossaryTerm t WHERE t.locale.localeId= :locale)");
        query.setParameter("locale", targetLocale);
        query.setComment("GlossaryDAO.deleteLocaleEntries-comments");
        query.executeUpdate();

        Query query2 =
                getSession()
                        .createQuery(
                                "Delete HGlossaryTerm t WHERE t.locale IN (SELECT l FROM HLocale l WHERE localeId= :locale)");
        query2.setParameter("locale", targetLocale);
        query2.setComment("GlossaryDAO.deleteLocaleEntries-terms");
        int rowCount = query2.executeUpdate();

        Query query3 =
                getSession()
                        .createQuery(
                                "Delete HGlossaryEntry e WHERE size(e.glossaryTerms) = 0");
        query3.setComment("GlossaryDAO.deleteLocaleEntries-entries");
        query3.executeUpdate();

        return rowCount;
    }
}
