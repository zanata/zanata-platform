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
package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.lucene.search.BooleanQuery;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryTerm;
import org.zanata.rest.service.ProjectService;
import org.zanata.search.LevenshteinUtil;
import org.zanata.security.ZanataIdentity;
import org.zanata.util.GlossaryUtil;
import org.zanata.util.ShortString;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Named("webtrans.gwt.GetGlossaryHandler")
@RequestScoped
@ActionHandlerFor(GetGlossary.class)
public class GetGlossaryHandler
        extends AbstractActionHandler<GetGlossary, GetGlossaryResult> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GetGlossaryHandler.class);

    // X2 in total: Global glossary and Project glossary
    private static final int MAX_RESULTS = 20;
    private static final Comparator<GlossaryResultItem> COMPARATOR =
            new GlossaryResultItemComparator();
    @Inject
    private GlossaryDAO glossaryDAO;
    @Inject
    private ZanataIdentity identity;

    @Override
    public GetGlossaryResult execute(GetGlossary action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();
        String searchText = action.getQuery();
        ShortString abbrev = new ShortString(searchText);
        SearchType searchType = action.getSearchType();
        log.debug("Fetching Glossary matches({}) for \"{}\"", searchType,
                abbrev);
        LocaleId localeId = action.getLocaleId();
        ArrayList<GlossaryResultItem> results;
        String projQualifiedName = ProjectService.getGlossaryQualifiedName(
                action.getProjectIterationId().getProjectSlug());
        try {
            List<Object[]> projMatches = glossaryDAO.getSearchResult(searchText,
                    searchType, action.getSrcLocaleId(), MAX_RESULTS,
                    projQualifiedName);
            List<Object[]> globalMatches = glossaryDAO.getSearchResult(
                    searchText, searchType, action.getSrcLocaleId(),
                    MAX_RESULTS, GlossaryUtil.GLOBAL_QUALIFIED_NAME);
            Map<GlossaryKey, GlossaryResultItem> matchesMap =
                    Maps.newLinkedHashMap();
            processMatches(globalMatches, matchesMap, searchText, localeId,
                    GlossaryUtil.GLOBAL_QUALIFIED_NAME);
            processMatches(projMatches, matchesMap, searchText, localeId,
                    projQualifiedName);
            results = Lists.newArrayList(matchesMap.values());
        } catch (ParseException e) {
            if (e.getCause() instanceof BooleanQuery.TooManyClauses) {
                log.warn(
                        "BooleanQuery.TooManyClauses, query too long to parse \'"
                                + StringUtils.left(searchText, 80) + "...\'");
            } else {
                if (searchType == SearchType.FUZZY) {
                    log.warn("Can\'t parse fuzzy query \'" + searchText + "\'");
                } else {
                    // escaping failed!
                    log.error("Can\'t parse query \'" + searchText + "\'", e);
                }
            }
            results = new ArrayList<>(0);
        }
        Collections.sort(results, COMPARATOR);
        log.debug("Returning {} Glossary matches for \"{}\"", results.size(),
                abbrev);
        return new GetGlossaryResult(action, results);
    }

    private void processMatches(List<Object[]> matches,
            Map<GlossaryKey, GlossaryResultItem> matchesMap, String searchText,
            LocaleId localeId, String qualifiedName) {
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

    @Override
    public void rollback(GetGlossary action, GetGlossaryResult result,
            ExecutionContext context) throws ActionException {
    }

    private static class GlossaryKey {
        private final String srcTermContent;
        private final String targetTermContent;
        private final String qualifiedName;

        public GlossaryKey(String qualifiedName, String srcTermContent,
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

    /**
     * NB just because this Comparator returns 0 doesn't mean the matches are
     * identical.
     */
    private static class GlossaryResultItemComparator
            implements Comparator<GlossaryResultItem> {

        @Override
        public int compare(GlossaryResultItem m1, GlossaryResultItem m2) {
            int result;
            result = Double.compare(m1.getSimilarityPercent(),
                    m2.getSimilarityPercent());
            if (result != 0) {
                return -result;
            }
            result = compare(m1.getSource().length(), m2.getSource().length());
            if (result != 0) {
                // shorter matches are preferred, if similarity is the same
                return result;
            }
            result = Double.compare(m1.getRelevanceScore(),
                    m2.getRelevanceScore());
            if (result != 0) {
                return -result;
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
}
