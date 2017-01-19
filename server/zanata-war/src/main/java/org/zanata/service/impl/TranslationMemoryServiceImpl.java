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

import static com.google.common.collect.Collections2.filter;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.TextContainerAnalyzerDiscriminator;
import org.zanata.jpa.FullText;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
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
import org.zanata.util.SysProperties;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType;
import org.zanata.webtrans.shared.rpc.LuceneQuery;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("translationMemoryServiceImpl")
@RequestScoped
public class TranslationMemoryServiceImpl implements TranslationMemoryService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TranslationMemoryServiceImpl.class);

    private static final int SEARCH_MAX_RESULTS =
            SysProperties.getInt(SysProperties.TM_MAX_RESULTS, 20);
    private static final float BOOST_CONTENT =
            SysProperties.getFloat(SysProperties.TM_BOOST_CONTENT, 10.0F);
    private static final float BOOST_TFTID =
            SysProperties.getFloat(SysProperties.TM_BOOST_TFTID, 10.0F);
    private static final float BOOST_PROJECT =
            SysProperties.getFloat(SysProperties.TM_BOOST_PROJECT, 2.0F);
    private static final float BOOST_DOCID =
            SysProperties.getFloat(SysProperties.TM_BOOST_DOCID, 1.5F);
    private static final float BOOST_RESID =
            SysProperties.getFloat(SysProperties.TM_BOOST_RESID, 1.5F);
    private static final float BOOST_ITERATION =
            SysProperties.getFloat(SysProperties.TM_BOOST_ITERATION, 1.0F);

    // private static final float BOOST_PROJITERSLUG = SysProperties.getFloat(
    // SysProperties.TM_BOOST_PROJITERSLUG, 1.5f);
    private static final double MINIMUM_SIMILARITY = 1.0;
    private static final String LUCENE_KEY_WORDS = "(\\s*)(AND|OR|NOT)(\\s+)";
    @Inject
    @FullText
    private FullTextEntityManager entityManager;
    @Inject
    private UrlUtil urlUtil;
    // sort desc by lastChanged of HTextFlowTarget
    private final Sort lastChangedSort = new Sort(SortField.FIELD_SCORE,
            new SortField(IndexFieldLabels.LAST_CHANGED_FIELD,
                    SortField.Type.STRING, true));
    private final TermQuery newStateQuery =
            new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD,
                    ContentState.New.toString()));
    private final TermQuery needReviewStateQuery =
            new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD,
                    ContentState.NeedReview.toString()));
    private final TermQuery rejectedStateQuery =
            new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD,
                    ContentState.Rejected.toString()));

    @Override
    public TransMemoryDetails getTransMemoryDetail(HLocale hLocale,
            HTextFlow tf) {
        HTextFlowTarget tft = tf.getTargets().get(hLocale.getId());
        HDocument document = tf.getDocument();
        HProjectIteration version = document.getProjectIteration();
        HProject project = version.getProject();
        String msgContext = (tf.getPotEntryData() == null) ? null
                : tf.getPotEntryData().getContext();
        String username = null;
        if (tft.getLastModifiedBy() != null
                && tft.getLastModifiedBy().hasAccount()) {
            username = tft.getLastModifiedBy().getAccount().getUsername();
        }
        String url = urlUtil.editorTransUnitUrl(project.getSlug(),
                version.getSlug(), hLocale.getLocaleId(),
                document.getSourceLocaleId(), document.getDocId(), tf.getId());
        return new TransMemoryDetails(HSimpleComment.toString(tf.getComment()),
                HSimpleComment.toString(tft.getComment()), project.getName(),
                version.getSlug(), tf.getDocument().getDocId(), tf.getResId(),
                msgContext, tft.getState(), username, tft.getLastChanged(),
                url);
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
                        0, Optional.empty(), HTextFlowTarget.class);
        if (matches.isEmpty()) {
            return Optional.empty();
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
        Collection<TransMemoryResultItem> aboveThreshold = filter(tmResults,
                new TransMemoryAboveThresholdPredicate(thresholdPercent));
        if (aboveThreshold.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(aboveThreshold.iterator().next());
    }

    @Override
    public List<TransMemoryResultItem> searchTransMemory(
            LocaleId targetLocaleId, LocaleId sourceLocaleId,
            TransMemoryQuery transMemoryQuery) {
        // NB: If we want to, we could pass the TFT id from the editor
        // via GWT-RPC(TransMemoryQuery), allowing Lucene to rank results
        // by metadata too.
        Optional<Long> textFlowTargetId = Optional.empty();
        Collection<Object[]> matches = findMatchingTranslation(targetLocaleId,
                sourceLocaleId, transMemoryQuery, SEARCH_MAX_RESULTS,
                textFlowTargetId, HTextFlowTarget.class, TransMemoryUnit.class);
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
            TransMemoryQuery transMemoryQuery,
            Optional<Long> textFlowTargetId) {
        return new QueryMatchProcessor(transMemoryQuery, sourceLocaleId,
                targetLocaleId, textFlowTargetId).process();
    }

    private TransMemoryQuery buildTMQuery(HTextFlow textFlow,
            HasSearchType.SearchType searchType, boolean checkContext,
            boolean checkDocument, boolean checkProject,
            boolean includeOwnTranslation) {
        TransMemoryQuery.Condition project = new TransMemoryQuery.Condition(
                checkProject, textFlow.getDocument().getProjectIteration()
                        .getProject().getSlug());
        TransMemoryQuery.Condition document = new TransMemoryQuery.Condition(
                checkDocument, textFlow.getDocument().getDocId());
        TransMemoryQuery.Condition res = new TransMemoryQuery.Condition(
                checkContext, textFlow.getResId());
        TransMemoryQuery query;
        if (searchType.equals(HasSearchType.SearchType.CONTENT_HASH)) {
            query = new TransMemoryQuery(textFlow.getContentHash(), searchType,
                    project, document, res);
        } else {
            query = new TransMemoryQuery(textFlow.getContents(), searchType,
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
            Optional<Long> textFlowTargetId, @Nonnull Class<?>... entityTypes) {
        try {
            if (entityTypes.length == 0) {
                throw new RuntimeException(
                        "Need entity type (HTextFlowTarget.class or TransMemoryUnit.class) for TM search");
            }
            List<Object[]> matches = getSearchResult(transMemoryQuery,
                    sourceLocaleId, targetLocaleId, maxResults,
                    textFlowTargetId, entityTypes);
            // filter out invalid target
            // TODO filter by entityTypes as well
            // TODO returning a filtered collection might be overkill
            return Collections2.filter(matches,
                    new ValidTargetFilterPredicate(targetLocaleId));
        } catch (ParseException e) {
            if (e.getCause() instanceof BooleanQuery.TooManyClauses) {
                log.warn(
                        "BooleanQuery.TooManyClauses, query too long to parse \'"
                                + StringUtils.left(
                                        transMemoryQuery.getQueries().get(0),
                                        80)
                                + "...\'");
            } else {
                if (transMemoryQuery
                        .getSearchType() == HasSearchType.SearchType.RAW) {
                    // TODO tell the user
                    log.info("Can\'t parse raw query {}", transMemoryQuery);
                } else {
                    // escaping failed!
                    log.error("Can\'t parse query " + transMemoryQuery, e);
                }
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
            ArrayList<String> textFlowContents = Lists
                    .newArrayList(textFlowTarget.getTextFlow().getContents());
            ArrayList<String> targetContents =
                    Lists.newArrayList(textFlowTarget.getContents());
            TransMemoryResultItem.MatchType matchType =
                    fromContentState(textFlowTarget.getState());
            double percent = calculateSimilarityPercentage(transMemoryQuery,
                    textFlowContents);
            if (percent < MINIMUM_SIMILARITY) {
                log.debug("Ignoring TM - {} with less than {}% matching.",
                        textFlowContents, MINIMUM_SIMILARITY);
                return;
            }
            TransMemoryResultItem item =
                    createOrGetResultItem(matchesMap, match, matchType,
                            textFlowContents, targetContents, percent);
            addTextFlowTargetToResultMatches(textFlowTarget, item);
        } else if (entity instanceof TransMemoryUnit) {
            TransMemoryUnit transUnit = (TransMemoryUnit) entity;
            ArrayList<String> sourceContents =
                    Lists.newArrayList(transUnit.getTransUnitVariants()
                            .get(sourceLocaleId.getId()).getPlainTextSegment());
            ArrayList<String> targetContents =
                    Lists.newArrayList(transUnit.getTransUnitVariants()
                            .get(targetLocaleId.getId()).getPlainTextSegment());
            double percent = calculateSimilarityPercentage(transMemoryQuery,
                    sourceContents);
            if (percent < MINIMUM_SIMILARITY) {
                log.debug("Ignoring TM - {} with less than {}% matching.",
                        sourceContents, MINIMUM_SIMILARITY);
                return;
            }
            TransMemoryResultItem item = createOrGetResultItem(matchesMap,
                    match, TransMemoryResultItem.MatchType.Imported,
                    sourceContents, targetContents, percent);
            addTransMemoryUnitToResultMatches(item, transUnit);
        }
    }

    private static double calculateSimilarityPercentage(TransMemoryQuery query,
            List<String> sourceContents) {
        double percent;
        if (query.getSearchType() == HasSearchType.SearchType.CONTENT_HASH) {
            return 100;
        } else if (query
                .getSearchType() == HasSearchType.SearchType.FUZZY_PLURAL) {
            percent = 100 * LevenshteinTokenUtil
                    .getSimilarity(query.getQueries(), sourceContents);
            if (percent > 99.99) {
                // make sure we only get 100% similarity if every character
                // matches
                percent = 100 * LevenshteinUtil
                        .getSimilarity(query.getQueries(), sourceContents);
            }
        } else {
            final String searchText = query.getQueries().get(0);
            percent = 100 * LevenshteinTokenUtil.getSimilarity(searchText,
                    sourceContents);
            if (percent > 99.99) {
                // make sure we only get 100% similarity if every character
                // matches
                percent = 100 * LevenshteinUtil.getSimilarity(searchText,
                        sourceContents);
            }
        }
        return percent;
    }

    private static TransMemoryResultItem.MatchType
            fromContentState(ContentState contentState) {
        switch (contentState) {
        case Approved:
            return TransMemoryResultItem.MatchType.ApprovedInternal;

        case Translated:
            return TransMemoryResultItem.MatchType.TranslatedInternal;

        default:
            throw new RuntimeException(
                    "Cannot map content state: " + contentState);

        }
    }

    /**
     * Look up the result item for the given source and target contents.
     *
     * If no item is found, a new one is added to the map and returned.
     *
     * @return the item for the given source and target contents, which may be
     *         newly created.
     */
    private TransMemoryResultItem createOrGetResultItem(
            Map<TMKey, TransMemoryResultItem> matchesMap, Object[] match,
            TransMemoryResultItem.MatchType matchType,
            ArrayList<String> sourceContents, ArrayList<String> targetContents,
            double percent) {
        TMKey key = new TMKey(sourceContents, targetContents);
        TransMemoryResultItem item = matchesMap.get(key);
        if (item == null) {
            float score = (Float) match[0];
            item = new TransMemoryResultItem(sourceContents, targetContents,
                    matchType, score, percent);
            matchesMap.put(key, item);
        }
        return item;
    }

    private void addTransMemoryUnitToResultMatches(TransMemoryResultItem item,
            TransMemoryUnit transMemoryUnit) {
        item.incMatchCount();
        item.addOrigin(transMemoryUnit.getTranslationMemory().getSlug());
    }

    private void addTextFlowTargetToResultMatches(
            HTextFlowTarget textFlowTarget, TransMemoryResultItem item) {
        item.incMatchCount();
        // TODO change sourceId to include type, then include the id of imported
        // matches
        item.addSourceId(textFlowTarget.getTextFlow().getId());
        // Workaround: since Imported does not have a details view in the
        // current editor,
        // I am treating it as the lowest priority, so will be overwritten by
        // other match types.
        // A better fix is to have the DTO hold all the match types so the
        // editor
        // can show them in whatever way is most sensible.
        ContentState state = textFlowTarget.getState();
        if (state == ContentState.Approved || item
                .getMatchType() == TransMemoryResultItem.MatchType.Imported) {
            item.setMatchType(fromContentState(state));
        }
    }

    /**
     * NB just because this Comparator returns 0 doesn't mean the matches are
     * identical.
     */
    private static enum TransMemoryResultComparator
            implements Comparator<TransMemoryResultItem> {
        COMPARATOR;

        @Override
        public int compare(TransMemoryResultItem m1, TransMemoryResultItem m2) {
            int result;
            result = Double.compare(m1.getSimilarityPercent(),
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

        private TMKey(List<String> textFlowContents,
                List<String> targetContents) {
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

    private void validateQueryLength(String query) {
        if (StringUtils.length(query) > LuceneQuery.QUERY_MAX_LENGTH) {
            throw new RuntimeException("Query string exceed max length: "
                    + LuceneQuery.QUERY_MAX_LENGTH + "=\'"
                    + StringUtils.left(query, 80) + "\'");
        }
    }

    private List<Object[]> getSearchResult(TransMemoryQuery query,
            LocaleId sourceLocale, LocaleId targetLocale, int maxResult,
            Optional<Long> textFlowTargetId, Class<?>... entities)
            throws ParseException {
        String queryText = null;
        String[] multiQueryText = null;
        switch (query.getSearchType()) {
        // 'Lucene' in the editor
        case RAW:
            queryText = query.getQueries().get(0);
            validateQueryLength(queryText);
            if (StringUtils.isBlank(queryText)) {
                return Lists.newArrayList();
            }
            break;

        // 'Fuzzy' in the editor
        case FUZZY:
            validateQueryLength(query.getQueries().get(0));
            queryText = escape(query.getQueries().get(0));
            if (StringUtils.isBlank(queryText)) {
                return Lists.newArrayList();
            }
            break;

        // 'Phrase' in the editor
        case EXACT:
            validateQueryLength(query.getQueries().get(0));
            queryText = "\"" + escape(query.getQueries().get(0)) + "\"";
            if (StringUtils.isBlank(queryText)) {
                return Lists.newArrayList();
            }
            break;

        // 'Fuzzy' in the editor, plus it is a plural entry
        case FUZZY_PLURAL:
            multiQueryText = new String[query.getQueries().size()];
            for (int i = 0; i < query.getQueries().size(); i++) {
                multiQueryText[i] = escape(query.getQueries().get(i));
                if (StringUtils.isBlank(multiQueryText[i])) {
                    return Lists.newArrayList();
                }
            }
            break;

        // Used by copyTrans for 100% match with source string
        case CONTENT_HASH:
            queryText = query.getQueries().get(0);
            validateQueryLength(queryText);
            if (StringUtils.isBlank(queryText)) {
                return Lists.newArrayList();
            }
            break;

        default:
            throw new RuntimeException(
                    "Unknown query type: " + query.getSearchType());

        }
        // Use the TextFlowTarget index
        Query textQuery = generateQuery(query, sourceLocale, targetLocale,
                textFlowTargetId, queryText, multiQueryText,
                IndexFieldLabels.TF_CONTENT_FIELDS);
        log.debug("Executing Lucene query: {}", textQuery);
        FullTextQuery ftQuery =
                entityManager.createFullTextQuery(textQuery, entities);
        ftQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);
        if (maxResult > 0) {
            ftQuery.setMaxResults(maxResult);
        }
        ftQuery.setSort(lastChangedSort);
        List<Object[]> resultList = (List<Object[]>) ftQuery.getResultList();
        if (!resultList.isEmpty() && resultList.size() == maxResult) {
            log.warn(
                    "Lucene query returned {} results (out of approx {}). Increasing {} might produce more matches.",
                    resultList.size(), ftQuery.getResultSize(),
                    SysProperties.TM_MAX_RESULTS);
            logQueryResults(resultList);
        }
        return resultList;
    }

    @VisibleForTesting
    protected static String escape(String string) {
        return QueryParser.escape(string).replaceAll(LUCENE_KEY_WORDS,
                "$1\"$2\"$3");
    }

    private void logQueryResults(List<Object[]> resultList) {
        if (log.isTraceEnabled()) {
            // resultList.get() could be a little slow if resultList is a
            // LinkedList, but in practice HSearch seems to use ArrayLists,
            // plus we only iterate up to 10 elements.
            int numToLog = Math.min(resultList.size(), 10);
            for (int i = 0; i < numToLog; i++) {
                Object[] arr = resultList.get(i);
                Number score = (Number) arr[0];
                Object entity = arr[1];
                log.trace("{}[{}]: {}", i, score, entity);
            }
        }
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
     * @param srcContentFields
     * @return
     * @throws ParseException
     */
    private Query generateQuery(TransMemoryQuery query, LocaleId sourceLocale,
            LocaleId targetLocale, Optional<Long> textFlowTargetId,
            String queryText, String[] multiQueryText,
            String[] srcContentFields) throws ParseException {
        Query textFlowTargetQuery = generateTextFlowTargetQuery(query,
                sourceLocale, targetLocale, textFlowTargetId, queryText,
                multiQueryText, srcContentFields);
        if (query.getSearchType() == HasSearchType.SearchType.CONTENT_HASH) {
            return textFlowTargetQuery;
        } else {
            String tmQueryText =
                    query.getSearchType() == HasSearchType.SearchType.FUZZY_PLURAL
                            ? multiQueryText[0] : queryText;
            Query transUnitQuery = generateTransMemoryQuery(sourceLocale,
                    targetLocale, tmQueryText);
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
     * @param srcContentFields
     * @return
     * @throws ParseException
     */
    private Query generateTextFlowTargetQuery(TransMemoryQuery queryParams,
            LocaleId sourceLocale, LocaleId targetLocale,
            Optional<Long> textFlowTargetId, String queryText,
            String[] multiQueryText, String[] srcContentFields)
            throws ParseException {
        BooleanQuery query = new BooleanQuery();
        Query contentQuery = buildContentQuery(queryParams, sourceLocale,
                queryText, multiQueryText, srcContentFields);
        contentQuery.setBoost(BOOST_CONTENT);
        query.add(contentQuery, BooleanClause.Occur.MUST);
        if (textFlowTargetId.isPresent()) {
            HTextFlowTarget tft = entityManager.find(HTextFlowTarget.class,
                    textFlowTargetId.get());
            if (tft != null) {
                HTextFlow tf = tft.getTextFlow();
                HDocument doc = tf.getDocument();
                HProjectIteration iter = doc.getProjectIteration();
                HProject proj = iter.getProject();
                addTermQueryWithBoost(query, "id",
                        String.valueOf(textFlowTargetId), BOOST_TFTID);
                addTermQueryWithBoost(query, "project", proj.getSlug(),
                        BOOST_PROJECT);
                addTermQueryWithBoost(query, "documentId", doc.getDocId(),
                        BOOST_DOCID);
                addTermQueryWithBoost(query, "textFlow.resId", tf.getResId(),
                        BOOST_RESID);
                addTermQueryWithBoost(query, "iteration", iter.getSlug(),
                        BOOST_ITERATION);
                // TODO add projiterslug to the index, replacing iteration slug
                // String projIterSlug = proj.getSlug()+iter.getSlug();
                // addTermQueryWithBoost(query, "projIterSlug", projIterSlug,
                // BOOST_PROJITERSLUG);
            } else {
                log.warn("Ignoring invalid textFlowTargetId: {}",
                        textFlowTargetId);
            }
        }
        TermQuery localeQuery =
                new TermQuery(new Term(IndexFieldLabels.LOCALE_ID_FIELD,
                        targetLocale.getId()));
        query.add(localeQuery, BooleanClause.Occur.MUST);
        buildContextQuery(query, queryParams);
        // exclude own translation
        if (!queryParams.getIncludeOwnTranslation().isCheck()) {
            TermQuery tmIdQuery = new TermQuery(new Term(IndexFieldLabels.TF_ID,
                    queryParams.getIncludeOwnTranslation().getValue()));
            query.add(tmIdQuery, BooleanClause.Occur.MUST_NOT);
        }
        query.add(newStateQuery, BooleanClause.Occur.MUST_NOT);
        query.add(needReviewStateQuery, BooleanClause.Occur.MUST_NOT);
        query.add(rejectedStateQuery, BooleanClause.Occur.MUST_NOT);
        return query;
    }

    private static void addTermQueryWithBoost(BooleanQuery query, String fld,
            String txt, float boost) {
        TermQuery q = new TermQuery(new Term(fld, txt));
        q.setBoost(boost);
        query.add(q, BooleanClause.Occur.SHOULD);
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
            String[] srcContentFields) throws ParseException {
        if (query.getSearchType() == HasSearchType.SearchType.CONTENT_HASH) {
            return new TermQuery(
                    new Term(IndexFieldLabels.TF_CONTENT_HASH, queryText));
        } else {
            // Analyzer is determined by the source language,
            // because we are querying the source text.
            String analyzerDefName = TextContainerAnalyzerDiscriminator
                    .getAnalyzerDefinitionName(sourceLocale.getId());
            Analyzer sourceAnalyzer = entityManager.getSearchFactory()
                    .getAnalyzer(analyzerDefName);
            if (query
                    .getSearchType() == HasSearchType.SearchType.FUZZY_PLURAL) {
                int queriesSize = multiQueryText.length;
                if (queriesSize > srcContentFields.length) {
                    log.warn("query contains {} fields, but we only index {}",
                            queriesSize, srcContentFields.length);
                }
                String[] searchFields = new String[queriesSize];
                System.arraycopy(srcContentFields, 0, searchFields, 0,
                        queriesSize);
                return MultiFieldQueryParser.parse(multiQueryText, searchFields,
                        sourceAnalyzer);
            } else {
                MultiFieldQueryParser parser = new MultiFieldQueryParser(
                        srcContentFields, sourceAnalyzer);
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
        String analyzerDefName = TextContainerAnalyzerDiscriminator
                .getAnalyzerDefinitionName(sourceLocale.getId());
        Analyzer analyzer =
                entityManager.getSearchFactory().getAnalyzer(analyzerDefName);
        QueryParser parser =
                new QueryParser(IndexFieldLabels.TRANS_UNIT_VARIANT_FIELD
                        + sourceLocale.getId(), analyzer);
        Query sourceContentQuery = parser.parse(queryText);
        WildcardQuery targetContentQuery = new WildcardQuery(
                new Term(IndexFieldLabels.TRANS_UNIT_VARIANT_FIELD
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

    private static final class TransMemoryAboveThresholdPredicate
            implements Predicate<TransMemoryResultItem> {
        private final int approvedThreshold;

        public TransMemoryAboveThresholdPredicate(int approvedThreshold) {
            this.approvedThreshold = approvedThreshold;
        }

        @Override
        public boolean apply(TransMemoryResultItem tmResult) {
            return (int) tmResult.getSimilarityPercent() >= approvedThreshold;
        }
    }

    private static class ValidTargetFilterPredicate
            implements Predicate<Object[]> {
        private final LocaleId localeId;

        public ValidTargetFilterPredicate(LocaleId localeId) {
            this.localeId = localeId;
        }

        @Override
        public boolean apply(Object[] input) {
            Object entity = input[1];
            if (entity instanceof HTextFlowTarget) {
                HTextFlowTarget target = (HTextFlowTarget) entity;
                if (!target.getLocaleId().equals(localeId)) {
                    log.error(
                            "Unexpected TextFlowTarget (locale {}): {}. You may need to re-index.",
                            target.getLocaleId(), target);
                    return false;
                } else if (!target.getState().isTranslated()) {
                    log.error(
                            "Unexpected TextFlowTarget (state {}): {}. You may need to re-index.",
                            target.getState(), target);
                    return false;
                } else {
                    HProjectIteration version = target.getTextFlow()
                            .getDocument().getProjectIteration();
                    if (version.getStatus() == EntityStatus.OBSOLETE) {
                        log.debug(
                                "Discarding TextFlowTarget (obsolete iteration {}): {}",
                                version, target);
                        return false;
                    } else if (version.getProject()
                            .getStatus() == EntityStatus.OBSOLETE) {
                        log.debug(
                                "Discarding TextFlowTarget (obsolete project {}): {}",
                                version.getProject(), target);
                        return false;
                    }
                }
                return true;
            } else if (entity instanceof TransMemoryUnit) {
                TransMemoryUnit tmu = ((TransMemoryUnit) entity);
                boolean includesTargetLocale = tmu.getTransUnitVariants()
                        .containsKey(localeId.getId());
                if (!includesTargetLocale) {
                    log.error(
                            "Unexpected TransMemoryUnit (no TUV in locale {}): {}. You may need to re-index.",
                            localeId.getId(), tmu);
                }
                return includesTargetLocale;
            } else if (entity == null) {
                log.error(
                        "Query results include null entity. You may need to re-index.");
                return false;
            } else {
                String name = entity.getClass().getName();
                log.warn(
                        "Unexpected query result of type {}: {}. You may need to re-index.",
                        name, entity);
            }
            return true;
        }
    }

    /**
     * Responsible for running a query and collating the results.
     *
     * I am using a class to avoid having to pass several arguments through all
     * the helper methods, since that makes the code very hard to read.
     */
    private class QueryMatchProcessor {
        public static final boolean SORT_BY_DATE = false;
        private final TransMemoryQuery query;
        private final LocaleId srcLocale;
        private final LocaleId transLocale;
        private final Optional<Long> textFlowTargetId;
        private final Map<TMKey, Suggestion> suggestions;
        private boolean processed;

        public QueryMatchProcessor(TransMemoryQuery query, LocaleId srcLocale,
                LocaleId transLocale, Optional<Long> textFlowTargetId) {
            this.query = query;
            this.srcLocale = srcLocale;
            this.transLocale = transLocale;
            this.textFlowTargetId = textFlowTargetId;
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
         * @param resultRow
         *            in the form [Float score, Object entity]
         */

        private void processResultRow(Object[] resultRow) {
            try {
                final QueryMatch match = fromResultRow(resultRow);
                processMatch(match);
            } catch (IllegalArgumentException e) {
                log.error(
                        "Skipped result row because it does not contain an expected entity type: {}",
                        resultRow, e);
            }
        }

        /**
         * Run the full-text query.
         *
         * @return collection of [float, entity] where float is the match score
         *         and entity is a HTextFlowTarget or TransMemoryUnit.
         */

        private Collection<Object[]> runQuery() {
            return findMatchingTranslation(transLocale, srcLocale, query,
                    SEARCH_MAX_RESULTS, textFlowTargetId, HTextFlowTarget.class,
                    TransMemoryUnit.class);
        }

        /**
         * Ensure there is a suggestion item for a match row and add a detail
         * item to the suggestion.
         *
         * Note: this updates this.suggestions
         *
         * @param match
         *            the row to add
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
         *
         * @param match
         *            providing the contents and score for the suggestion
         * @return the created suggestion object
         */

        private Suggestion createSuggestion(QueryMatch match) {
            double similarity = calculateSimilarityPercentage(query,
                    match.getSourceContents());
            return new Suggestion(match.getScore(), similarity,
                    match.getSourceContents(), match.getTargetContents());
        }

        private QueryMatch fromResultRow(Object[] match) {
            // matches are [Float score, Object entity], see #runQuery()
            float score = (Float) match[0];
            Object entity = match[1];
            if (entity instanceof HTextFlowTarget) {
                return new TextFlowTargetQueryMatch(score,
                        (HTextFlowTarget) entity);
            }
            if (entity instanceof TransMemoryUnit) {
                return new TransMemoryUnitQueryMatch(score,
                        (TransMemoryUnit) entity);
            }
            throw new IllegalArgumentException(
                    "Result type must be TextFlowTarget or TransMemoryUnit, but was neither");
        }

        /**
         * Represents a single row of results from a full-text query,
         * abstracting the type of entity returned in the row.
         */

        private abstract class QueryMatch {
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

            public float getScore() {
                return this.score;
            }
        }

        /**
         * Represents a single row of results containing a text flow target.
         */

        private class TextFlowTargetQueryMatch extends QueryMatch {
            private final List<String> sourceContents;
            private final List<String> targetContents;
            private final HTextFlowTarget target;

            public TextFlowTargetQueryMatch(float score,
                    HTextFlowTarget textFlowTarget) {
                super(score);
                target = textFlowTarget;
                sourceContents = Lists.newArrayList(
                        textFlowTarget.getTextFlow().getContents());
                targetContents =
                        Lists.newArrayList(textFlowTarget.getContents());
            }

            @Override
            public SuggestionDetail createDetails() {
                return new TextFlowSuggestionDetail(target);
            }

            public List<String> getSourceContents() {
                return this.sourceContents;
            }

            public List<String> getTargetContents() {
                return this.targetContents;
            }
        }

        /**
         * Represents a single row of results containing a trans memory unit.
         */

        private class TransMemoryUnitQueryMatch extends QueryMatch {
            private final List<String> sourceContents;
            private final List<String> targetContents;
            private TransMemoryUnit tmUnit;

            public TransMemoryUnitQueryMatch(float score,
                    TransMemoryUnit transMemoryUnit) {
                super(score);
                tmUnit = transMemoryUnit;
                sourceContents = getContents(srcLocale);
                targetContents = getContents(transLocale);
            }

            private ArrayList<String> getContents(LocaleId locale) {
                return Lists.newArrayList(tmUnit.getTransUnitVariants()
                        .get(locale.getId()).getPlainTextSegment());
            }

            @Override
            public SuggestionDetail createDetails() {
                return new TransMemoryUnitSuggestionDetail(tmUnit);
            }

            public List<String> getSourceContents() {
                return this.sourceContents;
            }

            public List<String> getTargetContents() {
                return this.targetContents;
            }
        }
    }
}
