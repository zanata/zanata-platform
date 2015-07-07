/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.*;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.TextContainerAnalyzerDiscriminator;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.rest.editor.dto.suggestion.Suggestion;
import org.zanata.rest.editor.dto.suggestion.SuggestionDetail;
import org.zanata.rest.editor.dto.suggestion.TextFlowSuggestionDetail;
import org.zanata.rest.editor.dto.suggestion.TransMemoryUnitSuggestionDetail;
import org.zanata.search.LevenshteinTokenUtil;
import org.zanata.search.LevenshteinUtil;
import org.zanata.service.TranslationMemoryService;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import static com.google.common.collect.Collections2.filter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Alternative;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Alternative
@Name("translationMemoryServiceImpl")
@Scope(ScopeType.STATELESS)
@Slf4j
public class TranslationMemoryServiceImpl implements TranslationMemoryService {

    @In
    private FullTextEntityManager entityManager;

    private static final Version LUCENE_VERSION = Version.LUCENE_29;

    // sort desc by lastChanged of HTextFlowTarget
    private final Sort lastChangedSort = new Sort(new SortField(
            IndexFieldLabels.LAST_CHANGED_FIELD, SortField.STRING, true));

    private final TermQuery newStateQuery = new TermQuery(new Term(
            IndexFieldLabels.CONTENT_STATE_FIELD, ContentState.New.toString()));

    private final TermQuery needReviewStateQuery = new TermQuery(new Term(
            IndexFieldLabels.CONTENT_STATE_FIELD,
            ContentState.NeedReview.toString()));

    private final TermQuery rejectedStateQuery = new TermQuery(new Term(
            IndexFieldLabels.CONTENT_STATE_FIELD,
            ContentState.Rejected.toString()));

    @Override
    public TransMemoryDetails
            getTransMemoryDetail(HLocale hLocale, HTextFlow tf) {
        HTextFlowTarget tft = tf.getTargets().get(hLocale.getId());

        String iterationName = tf.getDocument().getProjectIteration().getSlug();
        String projectName =
                tf.getDocument().getProjectIteration().getProject().getName();
        String msgContext =
                (tf.getPotEntryData() == null) ? null : tf.getPotEntryData()
                        .getContext();
        String username = null;
        if (tft.getLastModifiedBy() != null
                && tft.getLastModifiedBy().hasAccount()) {
            username = tft.getLastModifiedBy().getAccount().getUsername();
        }
        return new TransMemoryDetails(HSimpleComment.toString(tf.getComment()),
                HSimpleComment.toString(tft.getComment()), projectName,
                iterationName, tf.getDocument().getDocId(), tf.getResId(),
                msgContext, tft.getState(), username, tft.getLastChanged());
    }

    /**
     * This is used by CopyTrans, with ContentHash search in lucene. Returns
     * first entry of the matches which sort by HTextFlowTarget.lastChanged DESC
     *
     * @param textFlow
     * @param targetLocaleId
     * @param sourceLocaleId
     * @param checkContext
     * @param checkDocument
     * @param checkProject
     */
    @Override
    public Optional<HTextFlowTarget> searchBestMatchTransMemory(
            final HTextFlow textFlow, LocaleId targetLocaleId,
            LocaleId sourceLocaleId, boolean checkContext,
            boolean checkDocument, boolean checkProject) {

        TransMemoryQuery query =
                buildTMQuery(textFlow, HasSearchType.SearchType.CONTENT_HASH,
                        checkContext, checkDocument, checkProject, false);

        Collection<Object[]> matches =
                findMatchingTranslation(targetLocaleId, sourceLocaleId, query,
                        0, true, HTextFlowTarget.class);

        if (matches.isEmpty()) {
            return Optional.<HTextFlowTarget> absent();
        }

        return Optional.of((HTextFlowTarget) matches.iterator().next()[1]);
    }

    /**
     * This is used by TMMerge. Returns first entry of the matches which sort by
     * similarityPercent, sourceContents, and contents size.
     *
     * @param textFlow
     * @param targetLocaleId
     * @param sourceLocaleId
     * @param checkContext
     * @param checkDocument
     * @param checkProject
     * @param thresholdPercent
     */
    @Override
    public Optional<TransMemoryResultItem> searchBestMatchTransMemory(
            HTextFlow textFlow, LocaleId targetLocaleId,
            LocaleId sourceLocaleId, boolean checkContext,
            boolean checkDocument, boolean checkProject, int thresholdPercent) {

        TransMemoryQuery query =
                buildTMQuery(textFlow, HasSearchType.SearchType.FUZZY_PLURAL,
                        checkContext, checkDocument, checkProject, true);

        List<TransMemoryResultItem> tmResults =
                searchTransMemory(targetLocaleId, sourceLocaleId, query);

        // findTMAboveThreshold
        Collection<TransMemoryResultItem> aboveThreshold =
                filter(tmResults, new TransMemoryAboveThresholdPredicate(
                        thresholdPercent));

        if (aboveThreshold.isEmpty()) {
            return Optional.<TransMemoryResultItem> absent();
        }
        return Optional.of((TransMemoryResultItem) aboveThreshold.iterator()
                .next());
    }

    @Override
    public List<TransMemoryResultItem> searchTransMemory(
            LocaleId targetLocaleId, LocaleId sourceLocaleId,
            TransMemoryQuery transMemoryQuery) {

        Collection<Object[]> matches =
                findMatchingTranslation(targetLocaleId, sourceLocaleId,
                        transMemoryQuery, SEARCH_MAX_RESULTS, false,
                        HTextFlowTarget.class, TransMemoryUnit.class);

        Map<TMKey, TransMemoryResultItem> matchesMap =
                new LinkedHashMap<TMKey, TransMemoryResultItem>(matches.size());
        for (Object[] match : matches) {
            processIndexMatch(transMemoryQuery, matchesMap, match,
                    sourceLocaleId, targetLocaleId);
        }
        List<TransMemoryResultItem> results =
                Lists.newArrayList(matchesMap.values());
        Collections.sort(results, TransMemoryResultComparator.COMPARATOR);
        return results;
    }

    @Override
    public List<Suggestion> searchTransMemoryWithDetails(
            LocaleId targetLocaleId, LocaleId sourceLocaleId,
            TransMemoryQuery transMemoryQuery) {
        return new QueryMatchProcessor(transMemoryQuery, sourceLocaleId, targetLocaleId)
                .process();
    }

    private TransMemoryQuery buildTMQuery(HTextFlow textFlow,
            HasSearchType.SearchType searchType, boolean checkContext,
            boolean checkDocument, boolean checkProject,
            boolean includeOwnTranslation) {
        TransMemoryQuery.Condition project =
                new TransMemoryQuery.Condition(checkProject, textFlow
                        .getDocument().getProjectIteration().getProject()
                        .getSlug());
        TransMemoryQuery.Condition document =
                new TransMemoryQuery.Condition(checkDocument, textFlow
                        .getDocument().getDocId());
        TransMemoryQuery.Condition res =
                new TransMemoryQuery.Condition(checkContext,
                        textFlow.getResId());

        TransMemoryQuery query;
        if (searchType.equals(HasSearchType.SearchType.CONTENT_HASH)) {
            query =
                    new TransMemoryQuery(textFlow.getContentHash(), searchType,
                            project, document, res);
        } else {
            query =
                    new TransMemoryQuery(textFlow.getContents(), searchType,
                            project, document, res);
        }

        if (!includeOwnTranslation) {
            query.setIncludeOwnTranslation(false, textFlow.getId().toString());
        }
        return query;
    }

    /**
     * return match[0] = (float)score, match[1] = entity(HTextFlowTarget or
     * TransMemoryUnit)
     *
     * @param targetLocaleId
     * @param sourceLocaleId
     * @param transMemoryQuery
     * @param maxResults
     */
    private Collection<Object[]> findMatchingTranslation(
            LocaleId targetLocaleId, LocaleId sourceLocaleId,
            TransMemoryQuery transMemoryQuery, int maxResults,
            boolean sortByDate, Class<?>... entities) {
        try {
            if (entities == null || entities.length < 1) {
                throw new RuntimeException(
                        "Need entity type (HTextFlowTarget.class or TransMemoryUnit.class) for TM search");
            }
            List<Object[]> matches =
                    getSearchResult(transMemoryQuery, sourceLocaleId,
                            targetLocaleId, maxResults, sortByDate, entities);

            // filter out invalid target
            return Collections2.filter(matches,
                    ValidTargetFilterPredicate.PREDICATE);

        } catch (ParseException e) {
            if (transMemoryQuery.getSearchType() == HasSearchType.SearchType.RAW) {
                // TODO tell the user
                log.info("Can't parse raw query {}", transMemoryQuery);
            } else {
                // escaping failed!
                log.error("Can't parse query " + transMemoryQuery, e);
            }
        } catch (RuntimeException e) {
            log.error("Runtime exception:" + e.getMessage());
        }
        return Lists.newArrayList();
    }

    private void processIndexMatch(TransMemoryQuery transMemoryQuery,
            Map<TMKey, TransMemoryResultItem> matchesMap, Object[] match,
            LocaleId sourceLocaleId, LocaleId targetLocaleId) {
        Object entity = match[1];
        if (entity instanceof HTextFlowTarget) {
            HTextFlowTarget textFlowTarget = (HTextFlowTarget) entity;

            ArrayList<String> textFlowContents =
                    Lists.newArrayList(textFlowTarget.getTextFlow()
                            .getContents());
            ArrayList<String> targetContents =
                    Lists.newArrayList(textFlowTarget.getContents());
            TransMemoryResultItem.MatchType matchType =
                    fromContentState(textFlowTarget.getState());
            TransMemoryResultItem item = createOrGetResultItem(transMemoryQuery, matchesMap, match, matchType,
                    textFlowContents, targetContents);
            addTextFlowTargetToResultMatches(textFlowTarget, item);
        } else if (entity instanceof TransMemoryUnit) {
            TransMemoryUnit transUnit = (TransMemoryUnit) entity;
            ArrayList<String> sourceContents =
                    Lists.newArrayList(transUnit.getTransUnitVariants()
                            .get(sourceLocaleId.getId()).getPlainTextSegment());
            ArrayList<String> targetContents =
                    Lists.newArrayList(transUnit.getTransUnitVariants()
                            .get(targetLocaleId.getId()).getPlainTextSegment());
            TransMemoryResultItem item = createOrGetResultItem(transMemoryQuery, matchesMap, match,
                    TransMemoryResultItem.MatchType.Imported, sourceContents, targetContents);
            addTransMemoryUnitToResultMatches(item, transUnit);
        }
    }

    private static double calculateSimilarityPercentage(TransMemoryQuery query,
            List<String> sourceContents) {
        double percent;
        if (query.getSearchType() == HasSearchType.SearchType.CONTENT_HASH) {
            return 100;
        } else if (query.getSearchType() == HasSearchType.SearchType.FUZZY_PLURAL) {
            percent =
                    100 * LevenshteinTokenUtil.getSimilarity(
                            query.getQueries(), sourceContents);
            if (percent > 99.99) {
                // make sure we only get 100% similarity if every character
                // matches
                percent =
                        100 * LevenshteinUtil.getSimilarity(query.getQueries(),
                                sourceContents);
            }
        } else {
            final String searchText = query.getQueries().get(0);
            percent =
                    100 * LevenshteinTokenUtil.getSimilarity(searchText,
                            sourceContents);
            if (percent > 99.99) {
                // make sure we only get 100% similarity if every character
                // matches
                percent =
                        100 * LevenshteinUtil.getSimilarity(searchText,
                                sourceContents);
            }
        }
        return percent;
    }

    private static TransMemoryResultItem.MatchType fromContentState(
            ContentState contentState) {
        switch (contentState) {
        case Approved:
            return TransMemoryResultItem.MatchType.ApprovedInternal;

        case Translated:
            return TransMemoryResultItem.MatchType.TranslatedInternal;

        default:
            throw new RuntimeException("Cannot map content state: "
                    + contentState);
        }
    }

    /**
     * Look up the result item for the given source and target contents.
     *
     * If no item is found, a new one is added to the map and returned.
     *
     * @return the item for the given source and target contents, which may be newly created.
     */
    private TransMemoryResultItem createOrGetResultItem(TransMemoryQuery transMemoryQuery, Map<TMKey,
            TransMemoryResultItem> matchesMap, Object[] match, TransMemoryResultItem.MatchType matchType,
                                                        ArrayList<String> sourceContents, ArrayList<String> targetContents) {
        TMKey key = new TMKey(sourceContents, targetContents);
        TransMemoryResultItem item = matchesMap.get(key);
        if (item == null) {
            float score = (Float) match[0];
            double percent =
                    calculateSimilarityPercentage(transMemoryQuery,
                            sourceContents);
            item =
                    new TransMemoryResultItem(sourceContents, targetContents,
                            matchType, score, percent);
            matchesMap.put(key, item);
        }
        return item;
    }

    private void addTransMemoryUnitToResultMatches(TransMemoryResultItem item, TransMemoryUnit transMemoryUnit) {
        item.incMatchCount();
        item.addOrigin(transMemoryUnit.getTranslationMemory().getSlug());
    }

    private void addTextFlowTargetToResultMatches(HTextFlowTarget textFlowTarget, TransMemoryResultItem item) {
        item.incMatchCount();

        // TODO change sourceId to include type, then include the id of imported matches
        item.addSourceId(textFlowTarget.getTextFlow().getId());

        // Workaround: since Imported does not have a details view in the current editor,
        //             I am treating it as the lowest priority, so will be overwritten by
        //             other match types.
        //             A better fix is to have the DTO hold all the match types so the editor
        //             can show them in whatever way is most sensible.
        ContentState state = textFlowTarget.getState();
        if (state == ContentState.Approved || item.getMatchType() == TransMemoryResultItem.MatchType.Imported) {
            item.setMatchType(fromContentState(state));
        }
    }

    /**
     * NB just because this Comparator returns 0 doesn't mean the matches are
     * identical.
     */
    private static enum TransMemoryResultComparator implements
            Comparator<TransMemoryResultItem> {
        COMPARATOR;

        @Override
        public int compare(TransMemoryResultItem m1, TransMemoryResultItem m2) {
            int result;
            result =
                    Double.compare(m1.getSimilarityPercent(),
                            m2.getSimilarityPercent());
            if (result != 0) {
                // sort higher similarity first
                return -result;
            }

            result = compare(m1.getSourceContents(), m2.getSourceContents());
            if (result != 0) {
                // sort longer string lists first (more plural forms)
                return -result;
            }

            return -m1.getMatchType().compareTo(m2.getMatchType());
        }

        private int compare(List<String> list1, List<String> list2) {
            for (int i = 0; i < list1.size() && i < list2.size(); i++) {
                int comp = list1.get(i).compareTo(list2.get(i));
                if (comp != 0) {
                    return comp;
                }
            }
            return list1.size() - list2.size();
        }
    }

    private static class TMKey {
        private final List<String> textFlowContents;
        private final List<String> targetContents;

        private TMKey(List<String> textFlowContents, List<String> targetContents) {
            this.textFlowContents = textFlowContents;
            this.targetContents = targetContents;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TMKey) {
                TMKey o = (TMKey) obj;
                return textFlowContents.equals(o.textFlowContents)
                        && targetContents.equals(o.targetContents);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(textFlowContents, targetContents);
        }
    }

    private List<Object[]> getSearchResult(TransMemoryQuery query,
            LocaleId sourceLocale, LocaleId targetLocale, int maxResult,
            boolean sortByDate, Class<?>... entities) throws ParseException {
        String queryText = null;
        String[] multiQueryText = null;

        switch (query.getSearchType()) {
        // 'Lucene' in the editor
        case RAW:
            queryText = query.getQueries().get(0);
            if (StringUtils.isBlank(queryText)) {
                return Lists.newArrayList();
            }
            break;

        // 'Fuzzy' in the editor
        case FUZZY:
            queryText = QueryParser.escape(query.getQueries().get(0));
            if (StringUtils.isBlank(queryText)) {
                return Lists.newArrayList();
            }
            break;

        // 'Phrase' in the editor
        case EXACT:
            queryText =
                    "\"" + QueryParser.escape(query.getQueries().get(0)) + "\"";
            if (StringUtils.isBlank(queryText)) {
                return Lists.newArrayList();
            }
            break;

        // 'Fuzzy' in the editor, plus it is a plural entry
        case FUZZY_PLURAL:
            multiQueryText = new String[query.getQueries().size()];
            for (int i = 0; i < query.getQueries().size(); i++) {
                multiQueryText[i] =
                        QueryParser.escape(query.getQueries().get(i));
                if (StringUtils.isBlank(multiQueryText[i])) {
                    return Lists.newArrayList();
                }
            }
            break;
        // Used by copyTrans for 100% match with source string
        case CONTENT_HASH:
            queryText = query.getQueries().get(0);
            if (StringUtils.isBlank(queryText)) {
                return Lists.newArrayList();
            }
            break;
        default:
            throw new RuntimeException("Unknown query type: "
                    + query.getSearchType());
        }

        // Use the TextFlowTarget index
        Query textQuery =
                generateQuery(query, sourceLocale, targetLocale, queryText,
                        multiQueryText, IndexFieldLabels.TF_CONTENT_FIELDS);

        FullTextQuery ftQuery =
                entityManager.createFullTextQuery(textQuery, entities);

        ftQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);

        if (maxResult > 0) {
            ftQuery.setMaxResults(maxResult);
        }

        if (sortByDate) {
            ftQuery.setSort(lastChangedSort);
        }

        return (List<Object[]>) ftQuery.getResultList();
    }

    /**
     * Generate the query to match all source contents in all the searchable
     * indexes. (HTextFlowTarget and TransMemoryUnit)
     *
     * @param query
     * @param sourceLocale
     * @param targetLocale
     * @param queryText
     * @param multiQueryText
     * @param contentFields
     * @return
     * @throws ParseException
     */
    private Query generateQuery(TransMemoryQuery query, LocaleId sourceLocale,
            LocaleId targetLocale, String queryText, String[] multiQueryText,
            String contentFields[]) throws ParseException {
        Query textFlowTargetQuery =
                generateTextFlowTargetQuery(query, sourceLocale, targetLocale,
                        queryText, multiQueryText, contentFields);
        if (query.getSearchType() == HasSearchType.SearchType.CONTENT_HASH) {
            return textFlowTargetQuery;
        } else {
            String tmQueryText =
                    query.getSearchType() == HasSearchType.SearchType.FUZZY_PLURAL ? multiQueryText[0]
                            : queryText;
            Query transUnitQuery =
                    generateTransMemoryQuery(sourceLocale, targetLocale,
                            tmQueryText);
            // Join the queries for each different type
            return join(BooleanClause.Occur.SHOULD, textFlowTargetQuery,
                    transUnitQuery);
        }
    }

    /**
     * Generates the Hibernate Search Query that will search for
     * {@link HTextFlowTarget} objects for matches.
     *
     * @param queryParams
     * @param sourceLocale
     * @param targetLocale
     * @param queryText
     * @param multiQueryText
     * @param contentFields
     * @return
     * @throws ParseException
     */
    private Query generateTextFlowTargetQuery(TransMemoryQuery queryParams,
            LocaleId sourceLocale, LocaleId targetLocale, String queryText,
            String[] multiQueryText, String[] contentFields)
            throws ParseException {
        BooleanQuery query = new BooleanQuery();

        Query contentQuery =
                buildContentQuery(queryParams, sourceLocale, queryText,
                        multiQueryText, contentFields);
        query.add(contentQuery, BooleanClause.Occur.MUST);

        TermQuery localeQuery =
                new TermQuery(new Term(IndexFieldLabels.LOCALE_ID_FIELD,
                        targetLocale.getId()));
        query.add(localeQuery, BooleanClause.Occur.MUST);

        buildContextQuery(query, queryParams);

        // exclude own translation
        if (!queryParams.getIncludeOwnTranslation().isCheck()) {
            TermQuery tmIdQuery =
                    new TermQuery(new Term(IndexFieldLabels.TF_ID, queryParams
                            .getIncludeOwnTranslation().getValue()));

            query.add(tmIdQuery, BooleanClause.Occur.MUST_NOT);
        }

        query.add(newStateQuery, BooleanClause.Occur.MUST_NOT);
        query.add(needReviewStateQuery, BooleanClause.Occur.MUST_NOT);
        query.add(rejectedStateQuery, BooleanClause.Occur.MUST_NOT);

        return query;
    }

    /**
     * Build query for project, document and resId context
     *
     * @param queryParams
     * @return
     */
    private void buildContextQuery(BooleanQuery query,
            TransMemoryQuery queryParams) {

        if (queryParams.getProject() != null) {
            TermQuery projectQuery =
                    new TermQuery(new Term(IndexFieldLabels.PROJECT_FIELD,
                            queryParams.getProject().getValue()));

            if (queryParams.getProject().isCheck()) {
                query.add(projectQuery, BooleanClause.Occur.MUST);
            } else {
                query.add(projectQuery, BooleanClause.Occur.SHOULD);
            }
        }
        if (queryParams.getDocument() != null) {
            TermQuery docQuery =
                    new TermQuery(new Term(IndexFieldLabels.DOCUMENT_ID_FIELD,
                            queryParams.getDocument().getValue()));

            if (queryParams.getDocument().isCheck()) {
                query.add(docQuery, BooleanClause.Occur.MUST);
            } else {
                query.add(docQuery, BooleanClause.Occur.SHOULD);
            }
        }

        if (queryParams.getRes() != null) {
            TermQuery resIdQuery =
                    new TermQuery(new Term(IndexFieldLabels.TF_RES_ID,
                            queryParams.getRes().getValue()));
            if (queryParams.getRes().isCheck()) {
                query.add(resIdQuery, BooleanClause.Occur.MUST);
            } else {
                query.add(resIdQuery, BooleanClause.Occur.SHOULD);
            }
        }
    }

    private Query buildContentQuery(TransMemoryQuery query,
            LocaleId sourceLocale, String queryText, String[] multiQueryText,
            String[] contentFields) throws ParseException {

        if (query.getSearchType() == HasSearchType.SearchType.CONTENT_HASH) {
            return new TermQuery(new Term(IndexFieldLabels.TF_CONTENT_HASH,
                    queryText));
        } else {
            // Analyzer determined by the language
            String analyzerDefName =
                    TextContainerAnalyzerDiscriminator
                            .getAnalyzerDefinitionName(sourceLocale.getId());
            Analyzer analyzer =
                    entityManager.getSearchFactory().getAnalyzer(
                            analyzerDefName);

            if (query.getSearchType() == HasSearchType.SearchType.FUZZY_PLURAL) {
                int queriesSize = multiQueryText.length;
                if (queriesSize > contentFields.length) {
                    log.warn("query contains {} fields, but we only index {}",
                            queriesSize, contentFields.length);
                }
                String[] searchFields = new String[queriesSize];
                System.arraycopy(contentFields, 0, searchFields, 0, queriesSize);

                return MultiFieldQueryParser.parse(LUCENE_VERSION,
                        multiQueryText, searchFields, analyzer);
            } else {
                MultiFieldQueryParser parser =
                        new MultiFieldQueryParser(LUCENE_VERSION,
                                contentFields, analyzer);
                return parser.parse(queryText);
            }
        }
    }

    /**
     * Generates the Hibernate Search Query that will search for
     * {@link org.zanata.model.tm.TransMemoryUnit} objects for matches.
     *
     * @param sourceLocale
     * @param targetLocale
     * @param queryText
     * @return
     */
    private Query generateTransMemoryQuery(LocaleId sourceLocale,
            LocaleId targetLocale, String queryText) throws ParseException {
        // Analyzer determined by the language
        String analyzerDefName =
                TextContainerAnalyzerDiscriminator
                        .getAnalyzerDefinitionName(sourceLocale.getId());
        Analyzer analyzer =
                entityManager.getSearchFactory().getAnalyzer(analyzerDefName);

        QueryParser parser =
                new QueryParser(LUCENE_VERSION,
                        IndexFieldLabels.TRANS_UNIT_VARIANT_FIELD
                                + sourceLocale.getId(), analyzer);
        Query sourceContentQuery = parser.parse(queryText);
        WildcardQuery targetContentQuery =
                new WildcardQuery(new Term(
                        IndexFieldLabels.TRANS_UNIT_VARIANT_FIELD
                                + targetLocale.getId(), "*"));
        return join(BooleanClause.Occur.MUST, sourceContentQuery,
                targetContentQuery);
    }

    /**
     * Joins a given set of queries into a single one with the specified
     * occurrence condition.
     *
     * @param condition
     *            The occurrence condition all the joined queries will have.
     * @param queries
     *            The queries to be joined.
     * @return A single query that evaluates all the given sub-queries using the
     *         given occurence condition.
     */
    private static Query join(BooleanClause.Occur condition, Query... queries) {
        BooleanQuery joinedQuery = new BooleanQuery();
        for (Query q : queries) {
            joinedQuery.add(q, condition);
        }
        return joinedQuery;
    }

    private static final class TransMemoryAboveThresholdPredicate implements
            Predicate<TransMemoryResultItem> {
        private final int approvedThreshold;

        public TransMemoryAboveThresholdPredicate(int approvedThreshold) {
            this.approvedThreshold = approvedThreshold;
        }

        @Override
        public boolean apply(TransMemoryResultItem tmResult) {
            return (int) tmResult.getSimilarityPercent() >= approvedThreshold;
        }
    }

    private static enum ValidTargetFilterPredicate implements
            Predicate<Object[]> {
        PREDICATE;
        @Override
        public boolean apply(Object[] input) {
            Object entity = input[1];
            if (entity instanceof HTextFlowTarget) {
                HTextFlowTarget target = (HTextFlowTarget) entity;

                if (target == null || !target.getState().isTranslated()) {
                    return false;
                } else {
                    HProjectIteration version =
                            target.getTextFlow().getDocument()
                                    .getProjectIteration();
                    if (version.getStatus() == EntityStatus.OBSOLETE
                            || version.getProject().getStatus() == EntityStatus.OBSOLETE) {
                        return false;
                    }
                }
                return true;
            }
            return true;
        }
    }

    /**
     * Responsible for running a query and collating the results.
     *
     * I am using a class to avoid having to pass several arguments through
     * all the helper methods, since that makes the code very hard to read.
     */
    private class QueryMatchProcessor {
        public static final boolean SORT_BY_DATE = false;

        private final TransMemoryQuery query;
        private final LocaleId srcLocale;
        private final LocaleId transLocale;
        private final Map<TMKey, Suggestion> suggestions;
        private boolean processed;

        public QueryMatchProcessor(TransMemoryQuery query, LocaleId srcLocale, LocaleId transLocale) {
            this.query = query;
            this.srcLocale = srcLocale;
            this.transLocale = transLocale;
            suggestions = new HashMap<>();
            processed = false;
        }

        /**
         * Run the query, process and collate the results.
         *
         * Results are cached, so subsequent calls will return cached results
         * without running the query again.
         *
         * @return the collated results of the query.
         */
        public List<Suggestion> process() {
            if (!processed) {
                runQueryAndCacheSuggestions();
            }
            return new ArrayList<>(suggestions.values());
        }

        /**
         * When this has run, suggestions contains all the results of the query.
         */
        private void runQueryAndCacheSuggestions() {
            for (Object[] resultRow : runQuery()) {
                processResultRow(resultRow);
            }
            processed = true;
        }

        /**
         * Convert a result row to a match (if possible) then process the match.
         *
         * If the row does not contain an appropriate entity, an error is logged
         * and the row is skipped.
         *
         * @param resultRow in the form [Float score, Object entity]
         */
        private void processResultRow(Object[] resultRow) {
            try {
                final QueryMatch match = fromResultRow(resultRow);
                processMatch(match);
            } catch (IllegalArgumentException e) {
                log.error(
                        "Skipped result row because it does not contain " +
                                "an expected entity type: {}", resultRow, e);
            }
        }

        /**
         * Run the full-text query.
         * @return collection of [float, entity] where float is the match score
         *         and entity is a HTextFlowTarget or TransMemoryUnit.
         */
        private Collection<Object[]> runQuery() {
            return findMatchingTranslation(transLocale, srcLocale, query,
                    SEARCH_MAX_RESULTS, SORT_BY_DATE,
                    HTextFlowTarget.class, TransMemoryUnit.class);
        }

        /**
         * Ensure there is a suggestion item for a match row and add a
         * detail item to the suggestion.
         *
         * Note: this updates this.suggestions
         *
         * @param match the row to add
         */
        private void processMatch(QueryMatch match) {
            TMKey key = match.getKey();
            Suggestion suggestion = suggestions.get(key);
            if (suggestion == null) {
                suggestion = createSuggestion(match);
                suggestions.put(key, suggestion);
            }
            suggestion.getMatchDetails().add(match.createDetails());
        }

        /**
         * Generate and return a suggestion object for the given match.
         * @param match providing the contents and score for the suggestion
         * @return the created suggestion object
         */
        private Suggestion createSuggestion(QueryMatch match) {
            double similarity = calculateSimilarityPercentage(query, match.getSourceContents());
            return new Suggestion(match.getScore(), similarity, match.getSourceContents(), match.getTargetContents());
        }

        private QueryMatch fromResultRow(Object[] match) {
            // matches are [Float score, Object entity], see #runQuery()
            float score = (Float) match[0];
            Object entity = match[1];

            if (entity instanceof HTextFlowTarget) {
                return new TextFlowTargetQueryMatch(score, (HTextFlowTarget) entity);
            }
            if (entity instanceof TransMemoryUnit) {
                return new TransMemoryUnitQueryMatch(score, (TransMemoryUnit) entity);
            }

            throw new IllegalArgumentException("Result type must be TextFlowTarget or TransMemoryUnit, but was neither");
        }

        /**
         * Represents a single row of results from a full-text query,
         * abstracting the type of entity returned in the row.
         */
        private abstract class QueryMatch {
            @Getter
            private float score;

            protected QueryMatch(float score) {
                this.score = score;
            }

            public TMKey getKey() {
                return new TMKey(getSourceContents(), getTargetContents());
            }

            public abstract List<String> getSourceContents();

            public abstract List<String> getTargetContents();

            public abstract SuggestionDetail createDetails();
        }

        /**
         * Represents a single row of results containing a text flow target.
         */
        private class TextFlowTargetQueryMatch extends QueryMatch {

            @Getter
            private final List<String> sourceContents;

            @Getter
            private final List<String> targetContents;

            private final HTextFlowTarget target;

            public TextFlowTargetQueryMatch(float score, HTextFlowTarget textFlowTarget) {
                super(score);
                target = textFlowTarget;
                sourceContents = Lists.newArrayList(textFlowTarget.getTextFlow().getContents());
                targetContents = Lists.newArrayList(textFlowTarget.getContents());
            }

            @Override
            public SuggestionDetail createDetails() {
                return new TextFlowSuggestionDetail(target);
            }
        }

        /**
         * Represents a single row of results containing a trans memory unit.
         */
        private class TransMemoryUnitQueryMatch extends QueryMatch {

            @Getter
            private final List<String> sourceContents;

            @Getter
            private final List<String> targetContents;

            private TransMemoryUnit tmUnit;

            public TransMemoryUnitQueryMatch(float score, TransMemoryUnit transMemoryUnit) {
                super(score);
                tmUnit = transMemoryUnit;
                sourceContents = getContents(srcLocale);
                targetContents = getContents(transLocale);
            }

            private ArrayList<String> getContents(LocaleId locale) {
                return Lists.newArrayList(tmUnit.getTransUnitVariants().get(locale.getId()).getPlainTextSegment());
            }

            @Override
            public SuggestionDetail createDetails() {
                return new TransMemoryUnitSuggestionDetail(tmUnit);
            }
        }
    }
}
