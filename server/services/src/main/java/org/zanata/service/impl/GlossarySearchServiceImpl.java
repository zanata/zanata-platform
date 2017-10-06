/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.lucene.search.BooleanQuery;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.rest.service.GlossaryService;
import org.zanata.rest.service.ProjectService;
import org.zanata.search.LevenshteinUtil;
import org.zanata.service.GlossarySearchService;
import org.zanata.service.LocaleService;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.util.GlossaryUtil;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;


@RequestScoped
public class GlossarySearchServiceImpl implements GlossarySearchService {
    private static final long serialVersionUID = 7525324567708315447L;
    private static final Comparator<GlossaryResultItem> COMPARATOR =
            new GlossaryResultItemComparator();
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlossarySearchServiceImpl.class);


    private GlossaryDAO glossaryDAO;
    private LocaleService localeServiceImpl;
    private String contextPath;

    @Inject
    public GlossarySearchServiceImpl(GlossaryDAO glossaryDAO,
            LocaleService localeServiceImpl, @ContextPath String contextPath) {
        this.glossaryDAO = glossaryDAO;
        this.localeServiceImpl = localeServiceImpl;
        this.contextPath = contextPath;
    }

    public GlossarySearchServiceImpl() {
    }

    @Override
    public ArrayList<GlossaryResultItem> searchGlossary(
            @Nonnull LocaleId srcLocale,
            @Nonnull LocaleId transLocale,
            @Nonnull String searchText,
            @Nonnull SearchType searchType,
            int maxResults,
            @CheckForNull String projectSlug) {

        ArrayList<GlossaryResultItem> results;
        try {
            Map<GlossaryKey, GlossaryResultItem> matchesMap =
                    Maps.newLinkedHashMap();

            if (projectSlug != null) {
                String projQualifiedName = ProjectService.getGlossaryQualifiedName(
                        projectSlug);
                List<Object[]> projMatches = glossaryDAO.getSearchResult(searchText,
                        searchType, srcLocale, maxResults,
                        projQualifiedName);
                processMatches(projMatches, matchesMap, searchText, transLocale,
                        projQualifiedName);
            }
            List<Object[]> globalMatches = glossaryDAO.getSearchResult(
                    searchText, searchType, srcLocale,
                    maxResults, GlossaryUtil.GLOBAL_QUALIFIED_NAME);
            processMatches(globalMatches, matchesMap, searchText, transLocale,
                    GlossaryUtil.GLOBAL_QUALIFIED_NAME);
            results = Lists.newArrayList(matchesMap.values());
        } catch (ParseException e) {
            String errorMessage;
            if (e.getCause() instanceof BooleanQuery.TooManyClauses) {
                errorMessage = "BooleanQuery.TooManyClauses, query too long to parse \'"
                        + StringUtils.left(searchText, 80) + "...\'";
            } else if (searchType == SearchType.FUZZY) {
                errorMessage = "Can not parse fuzzy query \"" + searchText + "\"";
            } else {
                errorMessage = "Can not parse query \"" + searchText + "\"";
            }
            throw new ZanataServiceException(errorMessage, e);
        }
        Collections.sort(results, COMPARATOR);
        return results;
    }

    /**
     * Add a list of matches from glossaryDAO.getSearchResult() to the given map.
     *
     * This looks up translation for the given locale and calculates similarity
     * to the search term.
     *
     * @param matches results from glossaryDAO.getSearchResult()
     * @param matchesMap any non-duplicate terms from matches are put in here
     * @param searchText text used to generate percentage match
     * @param localeId for which to look up term translation
     * @param qualifiedName of glossary to look up term translation in
     */
    private void processMatches(List<Object[]> matches,
                               Map<GlossaryKey, GlossaryResultItem> matchesMap,
                               String searchText,
                               LocaleId localeId,
                               String qualifiedName) {
        for (Object[] match : matches) {
            HGlossaryTerm sourceTerm = (HGlossaryTerm) match[1];
            HGlossaryTerm targetTerm = null;
            if (sourceTerm != null) {
                targetTerm = glossaryDAO.getTermByEntryAndLocale(
                        sourceTerm.getGlossaryEntry().getId(), localeId,
                        qualifiedName);
            }
            if (targetTerm == null) {
                continue;
            }
            String srcTermContent = sourceTerm.getContent();
            String targetTermContent = targetTerm.getContent();
            GlossaryResultItem item = getOrCreateGlossaryResultItem(matchesMap,
                    qualifiedName, srcTermContent, targetTermContent,
                    (Float) match[0], searchText);
            item.addSourceId(sourceTerm.getId());
        }
    }

    private static GlossaryResultItem getOrCreateGlossaryResultItem(
            Map<GlossaryKey, GlossaryResultItem> matchesMap,
            String qualifiedName, String srcTermContent,
            String targetTermContent, float score, String searchText) {
        GlossaryKey key = new GlossaryKey(qualifiedName, targetTermContent,
                srcTermContent);
        GlossaryResultItem item = matchesMap.get(key);
        if (item == null) {
            double percent = 100
                    * LevenshteinUtil.getSimilarity(searchText, srcTermContent);
            item = new GlossaryResultItem(qualifiedName, srcTermContent,
                    targetTermContent, score, percent);
            matchesMap.put(key, item);
        }
        return item;
    }

    /**
     * Get glossary url with dswid parameter
     */
    @VisibleForTesting
    String glossaryUrl(String qualifiedName, String filter,
            LocaleId localeId) {
        String url = contextPath;
        if (GlossaryService.isProjectGlossary(qualifiedName)) {
            String projectSlug = GlossaryService.getProjectSlug(qualifiedName);
            url = url + "/glossary/project/" + projectSlug;
        } else {
            url = url + "/glossary";
        }
        boolean hasFilter = StringUtils.isNotBlank(filter);
        if (hasFilter) {
            url += "?filter=" + UrlUtil.encodeString(filter);
        }
        if (localeId != null) {
            String prefix = hasFilter ? "&" : "?";
            url += prefix + "locale=" + localeId;
        }
        return url;
    }

    @Override
    public ArrayList<GlossaryDetails> lookupDetails(
            @Nonnull LocaleId locale,
            @Nonnull List<Long> sourceIds) throws ZanataServiceException {
        HLocale hLocale = localeServiceImpl.getByLocaleId(locale);
        log.info("Fetching glossary details for entries {} in locale {}",
                sourceIds, hLocale);
        List<HGlossaryTerm> srcTerms = glossaryDAO.findTermByIdList(sourceIds);
        ArrayList<GlossaryDetails> items =
                new ArrayList<GlossaryDetails>(srcTerms.size());
        for (HGlossaryTerm srcTerm : srcTerms) {
            HGlossaryEntry entry = srcTerm.getGlossaryEntry();
            HGlossaryTerm hGlossaryTerm = entry.getGlossaryTerms().get(hLocale);
            String srcContent = srcTerm.getContent();
            String qualifiedName = entry.getGlossary().getQualifiedName();
            String url = glossaryUrl(qualifiedName, srcContent,
                    hLocale.getLocaleId());
            items.add(new GlossaryDetails(entry.getId(), srcContent,
                    hGlossaryTerm.getContent(), entry.getDescription(),
                    entry.getPos(), hGlossaryTerm.getComment(),
                    entry.getSourceRef(), entry.getSrcLocale().getLocaleId(),
                    hLocale.getLocaleId(), url, hGlossaryTerm.getVersionNum(),
                    hGlossaryTerm.getLastChanged()));
        }
        return items;
    }

    /**
     * NB just because this Comparator returns 0 doesn't mean the matches are
     * identical.
     */
    private static class GlossaryResultItemComparator
            implements Comparator<GlossaryResultItem>, Serializable {

        private static final long serialVersionUID = 2560160793728483264L;

        @Override
        public int compare(GlossaryResultItem m1, GlossaryResultItem m2) {
            int result;
            result = Double.compare(m2.getSimilarityPercent(),
                    m1.getSimilarityPercent());
            if (result != 0) {
                return result;
            }
            result = compare(m1.getSource().length(), m2.getSource().length());
            if (result != 0) {
                // shorter matches are preferred, if similarity is the same
                return result;
            }
            result = Double.compare(m2.getRelevanceScore(),
                    m1.getRelevanceScore());
            if (result != 0) {
                return result;
            }
            return m1.getSource().compareTo(m2.getSource());
        }

        private int compare(int a, int b) {
            if (a < b) {
                return -1;
            }
            if (a > b) {
                return 1;
            }
            return 0;
        }
    }

    private static class GlossaryKey {
        private final String srcTermContent;
        private final String targetTermContent;
        private final String qualifiedName;

        GlossaryKey(String qualifiedName, String srcTermContent,
                           String targetTermContent) {
            this.qualifiedName = qualifiedName;
            this.srcTermContent = srcTermContent;
            this.targetTermContent = targetTermContent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof GlossaryKey))
                return false;
            GlossaryKey that = (GlossaryKey) o;

            if (srcTermContent != null
                    ? !srcTermContent.equals(that.srcTermContent)
                    : that.srcTermContent != null)
                return false;
            if (targetTermContent != null
                    ? !targetTermContent.equals(that.targetTermContent)
                    : that.targetTermContent != null)
                return false;
            return qualifiedName != null
                    ? qualifiedName.equals(that.qualifiedName)
                    : that.qualifiedName == null;
        }

        @Override
        public int hashCode() {
            int result = srcTermContent != null ? srcTermContent.hashCode() : 0;
            result = 31 * result + (targetTermContent != null
                    ? targetTermContent.hashCode() : 0);
            result = 31 * result
                    + (qualifiedName != null ? qualifiedName.hashCode() : 0);
            return result;
        }
    }
}
