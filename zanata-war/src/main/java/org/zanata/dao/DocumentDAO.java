package org.zanata.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.hibernate.LobHelper;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.file.GlobalDocumentId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.StatusCount;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.util.StatisticsUtil;

import com.google.common.base.Optional;

@Name("documentDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class DocumentDAO extends AbstractDAOImpl<HDocument, Long> {
    public DocumentDAO() {
        super(HDocument.class);
    }

    public DocumentDAO(Session session) {
        super(HDocument.class, session);
    }

    public HDocument getByDocIdAndIteration(HProjectIteration iteration,
            String id) {
        return (HDocument) getSession().byNaturalId(HDocument.class)
                .using("docId", id).using("projectIteration", iteration).load();
    }

    public HDocument getById(Long id) {
        return (HDocument) getSession().get(HDocument.class, id);
    }

    public Set<LocaleId> getTargetLocales(HDocument hDoc) {
        // TODO should this use UNIQUE?
        Query q =
                getSession().createQuery(
                        "select tft.locale from HTextFlowTarget tft "
                                + "where tft.textFlow.document = :document");
        q.setParameter("document", hDoc);
        q.setComment("DocumentDAO.getTargetLocales");
        // TODO q.setCacheable(true); ??
        @SuppressWarnings("unchecked")
        List<LocaleId> locales = q.list();
        return new HashSet<LocaleId>(locales);
    }

    public int getTotalDocument() {
        Query q = getSession().createQuery("select count(*) from HDocument");
        q.setCacheable(true);
        q.setComment("DocumentDAO.getTotalDocument");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalActiveDocument() {
        Query q =
                getSession().createQuery(
                        "select count(*) from HDocument doc "
                                + "where doc.obsolete = false");
        q.setCacheable(true);
        q.setComment("DocumentDAO.getTotalActiveDocument");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalObsoleteDocument() {
        Query q =
                getSession().createQuery(
                        "select count(*) from HDocument doc "
                                + "where doc.obsolete = true");
        q.setCacheable(true);
        q.setComment("DocumentDAO.getTotalObsoleteDocument");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    /**
     * Returns the total message count for a document
     */
    public Long getTotalCountForDocument(HDocument document) {
        Session session = getSession();
        Long totalCount =
                (Long) session
                        .createQuery(
                                "select count(tf) from HTextFlow tf "
                                        + "where tf.document = :doc "
                                        + "and tf.obsolete = false")
                        .setParameter("doc", document)
                        .setComment("DocumentDAO.getTotalCountForDocument")
                        .setCacheable(true).uniqueResult();

        if (totalCount == null) {
            totalCount = 0L;
        }

        return totalCount;

    }

    public Long getTotalWordCountForDocument(HDocument document) {
        Session session = getSession();

        Long totalWordCount =
                (Long) session
                        .createQuery(
                                "select sum(tf.wordCount) from HTextFlow tf "
                                        + "where tf.document = :doc "
                                        + "and tf.obsolete = false")
                        .setParameter("doc", document).setCacheable(true)
                        .setComment("DocumentDAO.getTotalWordCountForDocument")
                        .uniqueResult();

        if (totalWordCount == null) {
            totalWordCount = 0L;
        }

        return totalWordCount;

    }

    public HTextFlowTarget getLastTranslatedTarget(Long documentId,
            LocaleId localeId) {
        Session session = getSession();

        StringBuilder query = new StringBuilder();

        query.append("from HTextFlowTarget tft ");
        query.append("where tft.textFlow.document.id = :docId ");
        query.append("and tft.locale.localeId = :localeId ");
        query.append("order by tft.lastChanged DESC");

        return (HTextFlowTarget) session.createQuery(query.toString())
                .setParameter("docId", documentId)
                .setParameter("localeId", localeId).setCacheable(true)
                .setMaxResults(1).setComment("DocumentDAO.getLastTranslated")
                .uniqueResult();
    }

    @Nullable
    public HTextFlowTarget getLastTranslatedTargetOrNull(Long documentId) {
        Session session = getSession();

        StringBuilder query = new StringBuilder();

        query.append("from HTextFlowTarget tft ");
        query.append("where tft.textFlow.document.id = :docId ");
        query.append("order by tft.lastChanged DESC");

        return (HTextFlowTarget) session.createQuery(query.toString())
                .setParameter("docId", documentId).setCacheable(true)
                .setMaxResults(1).setComment("DocumentDAO.getLastTranslated")
                .uniqueResult();
    }

    /**
     * @see ProjectIterationDAO#getStatisticsForContainer(Long, LocaleId)
     * @param docId
     * @param localeId
     * @return
     */
    public ContainerTranslationStatistics getStatistics(long docId,
            LocaleId localeId) {
        Session session = getSession();

        // calculate unit counts
        @SuppressWarnings("unchecked")
        List<StatusCount> stats =
                session.createQuery(
                        "select new org.zanata.model.StatusCount("
                                + "tft.state, count(tft)) "
                                + "from HTextFlowTarget tft "
                                + "where tft.textFlow.document.id = :id "
                                + "  and tft.locale.localeId = :locale "
                                + "  and tft.textFlow.obsolete = false "
                                + "group by tft.state")
                        .setParameter("id", docId)
                        .setParameter("locale", localeId)
                        .setComment("DocumentDAO.getStatistics-units")
                        .setCacheable(true).list();
        Long totalCount = getTotalCountForDocument(getById(docId));

        TransUnitCount unitCount = new TransUnitCount();
        for (StatusCount count : stats) {
            unitCount.set(count.status, count.count.intValue());
        }
        unitCount.set(ContentState.New,
                StatisticsUtil.calculateUntranslated(totalCount, unitCount));

        // calculate word counts
        @SuppressWarnings("unchecked")
        List<StatusCount> wordStats =
                session.createQuery(
                        "select new org.zanata.model.StatusCount("
                                + "tft.state, sum(tft.textFlow.wordCount)) "
                                + "from HTextFlowTarget tft "
                                + "where tft.textFlow.document.id = :id "
                                + "  and tft.locale.localeId = :locale "
                                + "  and tft.textFlow.obsolete = false "
                                + "group by tft.state")
                        .setParameter("id", docId)
                        .setParameter("locale", localeId).setCacheable(true)
                        .setComment("DocumentDAO.getStatistics-words").list();
        Long totalWordCount = getTotalWordCountForDocument(getById(docId));
        TransUnitWords wordCount = new TransUnitWords();
        for (StatusCount count : wordStats) {
            wordCount.set(count.status, count.count.intValue());
        }
        long newWordCount =
                StatisticsUtil.calculateUntranslated(totalWordCount, wordCount);
        wordCount.set(ContentState.New, (int) newWordCount);

        ContainerTranslationStatistics result =
                new ContainerTranslationStatistics();
        result.addStats(new TranslationStatistics(unitCount, localeId
                .toString()));
        result.addStats(new TranslationStatistics(wordCount, localeId
                .toString()));

        return result;
    }

    /**
     * Returns document statistics for multiple locales.
     *
     * @see DocumentDAO#getStatistics(long, org.zanata.common.LocaleId)
     * @param docId
     * @param localeIds
     *            If empty or null, data for all locales will be returned.
     * @return Map of document statistics indexed by locale. Some locales may
     *         not have entries if there is no data stored for them.
     */
    public Map<LocaleId, ContainerTranslationStatistics> getStatistics(
            long docId, LocaleId... localeIds) {
        Session session = getSession();
        Map<LocaleId, ContainerTranslationStatistics> returnStats =
                new HashMap<LocaleId, ContainerTranslationStatistics>();
        Map<String, TransUnitCount> transUnitCountMap =
                new HashMap<String, TransUnitCount>();
        Map<String, TransUnitWords> transUnitWordsMap =
                new HashMap<String, TransUnitWords>();

        StringBuilder query = new StringBuilder();
        query.append("select new map (tft.state as state, count(tft) as count, ");
        query.append("          sum(tft.textFlow.wordCount) as wordCount, "
                + "tft.locale.localeId as locale) ");
        query.append("from HTextFlowTarget tft ");
        query.append("where tft.textFlow.document.id = :id ");
        if (localeIds != null && localeIds.length > 0) {
            query.append("  and tft.locale.localeId in (:locales) ");
        }
        query.append("  and tft.textFlow.obsolete = false ");
        query.append("group by tft.state, tft.locale");

        // calculate unit counts
        @SuppressWarnings("unchecked")
        Query hQuery =
                session.createQuery(query.toString())
                        .setParameter("id", docId)
                        .setComment(
                                "DocumentDAO.getStatisticsMultipleLocales-units")
                        .setCacheable(true);

        if (localeIds != null && localeIds.length > 0) {
            hQuery.setParameterList("locales", localeIds);
        }

        List<Map<String, Object>> stats = hQuery.list();
        // Collect the results for all states
        for (Map<String, Object> row : stats) {
            ContentState state = (ContentState) row.get("state");
            Long count = (Long) row.get("count");
            Long wordCount = (Long) row.get("wordCount");
            LocaleId localeId = (LocaleId) row.get("locale");

            TransUnitCount transUnitCount =
                    transUnitCountMap.get(localeId.getId());
            if (transUnitCount == null) {
                transUnitCount = new TransUnitCount();
                transUnitCountMap.put(localeId.getId(), transUnitCount);
            }

            TransUnitWords transUnitWords =
                    transUnitWordsMap.get(localeId.getId());
            if (transUnitWords == null) {
                transUnitWords = new TransUnitWords();
                transUnitWordsMap.put(localeId.getId(), transUnitWords);
            }

            transUnitCount.set(state, count.intValue());
            transUnitWords.set(state, wordCount.intValue());
        }

        Map<String, Object> totalCounts =
                (Map<String, Object>) session
                        .createQuery(
                                "select new map ( count(tf) as count, "
                                        + "sum(tf.wordCount) as wordCount ) "
                                        + "from HTextFlow tf "
                                        + "where tf.document.id = :id "
                                        + "and tf.obsolete = false")
                        .setParameter("id", docId)
                        .setComment(
                                "DocumentDAO.getStatisticsMultipleLocales-words")
                        .setCacheable(true).uniqueResult();

        // Calculate the 'New' counts
        Long totalCount = (Long) totalCounts.get("count");
        Long totalWordCount = (Long) totalCounts.get("wordCount");
        for (TransUnitCount stat : transUnitCountMap.values()) {
            stat.set(ContentState.New,
                    StatisticsUtil.calculateUntranslated(totalCount, stat));
        }
        for (TransUnitWords stat : transUnitWordsMap.values()) {
            stat.set(ContentState.New,
                    StatisticsUtil.calculateUntranslated(totalWordCount, stat));
        }

        // Merge into a single Stats object
        for (String locale : transUnitCountMap.keySet()) {
            ContainerTranslationStatistics newStats =
                    new ContainerTranslationStatistics();
            newStats.addStats(new TranslationStatistics(transUnitCountMap
                    .get(locale), locale));
            newStats.addStats(new TranslationStatistics(transUnitWordsMap
                    .get(locale), locale));

            if (newStats.getStats(locale, StatUnit.MESSAGE) != null
                    && newStats.getStats(locale, StatUnit.WORD) != null) {
                returnStats.put(new LocaleId(locale), newStats);
            }
        }

        return returnStats;
    }

    public HDocument getByGlobalId(GlobalDocumentId id) {
        return getByProjectIterationAndDocId(id.getProjectSlug(),
                id.getVersionSlug(), id.getDocId());
    }

    public HDocument getByProjectIterationAndDocId(final String projectSlug,
            final String iterationSlug, final String docId) {
        // TODO caching might be better with
        // getByDocIdAndIteration(ProjectIterationDAO.getBySlug(), docId)
        Session session = getSession();
        Query q =
                session.createQuery("from HDocument d "
                        + "where d.projectIteration.slug = :iterationSlug "
                        + "and d.projectIteration.project.slug = :projectSlug "
                        + "and d.docId = :docId " + "and d.obsolete = false");
        q.setParameter("iterationSlug", iterationSlug)
                .setParameter("projectSlug", projectSlug)
                .setParameter("docId", docId);
        q.setComment("DocumentDAO.getByProjectIterationAndDocId");
        q.setCacheable(true);
        final HDocument doc = (HDocument) q.uniqueResult();
        return doc;
    }

    public List<HDocument> getByProjectIterationAndDocIdList(
            final String projectSlug, final String iterationSlug,
            List<String> docIdList) {
        Session session = getSession();
        Query q =
                session.createQuery("from HDocument d "
                        + "where d.projectIteration.slug = :iterationSlug "
                        + "and d.projectIteration.project.slug = :projectSlug "
                        + "and d.docId in (:docIdList) "
                        + "and d.obsolete = false");
        q.setParameter("iterationSlug", iterationSlug)
                .setParameter("projectSlug", projectSlug)
                .setParameterList("docIdList", docIdList);
        q.setComment("DocumentDAO.getByProjectIterationAndDocIdList");
        q.setCacheable(true);
        List<HDocument> docs = q.list();
        return docs;
    }

    public List<HDocument> getAllByProjectIteration(final String projectSlug,
            final String iterationSlug) {
        Session session = getSession();
        Query q =
                session.createQuery("from HDocument d "
                        + "where d.projectIteration.slug = :iterationSlug "
                        + "and d.projectIteration.project.slug = :projectSlug "
                        + "and d.obsolete = false " + "order by d.name");
        q.setParameter("iterationSlug", iterationSlug).setParameter(
                "projectSlug", projectSlug);
        q.setComment("DocumentDAO.getAllByProjectIteration");
        // TODO q.setCacheable(true); ??
        @SuppressWarnings("unchecked")
        final List<HDocument> documents = q.list();
        return documents;
    }

    /**
     * Calculates a translated document's hash.
     *
     * @param projectSlug
     *            Project identifier
     * @param iterationSlug
     *            Iteration identifier
     * @param docId
     *            Document identifier
     * @param locale
     *            Translated document's locale.
     * @return A Hash string (checksum) for a translated document.
     */
    public String
            getTranslatedDocumentStateHash(final String projectSlug,
                    final String iterationSlug, final String docId,
                    final HLocale locale) {
        // NB: This method uses a native SQL query tested on mysql and h2
        // databases.
        Session session = getSession();
        StringBuilder nativeSql = new StringBuilder();
        nativeSql.append("select MD5(group_concat(hashState)) from ");
        nativeSql.append("( ");
        nativeSql.append("   select ");
        nativeSql.append("   concat( ");
        nativeSql.append("   d.id, '|',  ");
        nativeSql.append("   d.versionNum, '|',  ");
        nativeSql
                .append("   ifnull(group_concat(poth.versionNum separator '|'), ''), '|',  ");
        nativeSql
                .append("   ifnull(group_concat(tft.id separator '|'), ''), '|',  ");
        nativeSql
                .append("   ifnull(group_concat(tft.versionNum separator '|'), ''),  '|',  ");
        nativeSql
                .append("   ifnull(group_concat(tf.id separator '|'), ''),  '|',  ");
        nativeSql
                .append("   ifnull(group_concat(tf.revision separator '|'), ''),  '|',  ");
        nativeSql
                .append("   ifnull(group_concat(c.comment separator '|'), '')) as hashState  ");
        nativeSql.append("   from  ");
        nativeSql.append("   HDocument d ");
        nativeSql
                .append("   inner join HTextFlow tf on tf.document_id = d.id ");
        nativeSql
                .append("   inner join HProjectIteration i on d.project_iteration_id = i.id ");
        nativeSql.append("   inner join HProject p on i.project_id = p.id ");
        nativeSql
                .append("   left outer join HTextFlowTarget tft on tft.tf_id = tf.id and tft.locale = :localeId ");
        nativeSql
                .append("   left outer join HSimpleComment c on c.id = tft.comment_id  ");
        nativeSql
                .append("   left outer join HPoTargetHeader poth on poth.document_id = d.id and poth.targetLanguage = :localeId ");
        nativeSql.append("   where  ");
        nativeSql.append("   d.docId = :docId ");
        nativeSql.append("   and p.slug = :projectSlug ");
        nativeSql.append("   and i.slug = :iterationSlug ");
        nativeSql.append("   group by d.id, d.versionNum ");
        nativeSql.append(")  as T");

        Query query =
                session.createSQLQuery(nativeSql.toString())
                        .setParameter("localeId", locale.getId())
                        .setParameter("docId", docId)
                        .setParameter("projectSlug", projectSlug)
                        .setParameter("iterationSlug", iterationSlug);
        // Transform the results from byte[] into Strings when necessary
        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                if (tuple[0] instanceof byte[]) {
                    return new String((byte[]) tuple[0]);
                }
                return tuple[0];
            }

            @Override
            public List transformList(List collection) {
                return collection; // no transformation needed
            }
        });
        String stateHash = (String) query.uniqueResult();
        return stateHash;
    }

    /**
     * Do not use this method when adding a new raw document, instead use
     * {@link #addRawDocument(HDocument, HRawDocument)}
     *
     * @see AbstractDAOImpl#makePersistent(Object)
     */
    @Override
    public HDocument makePersistent(HDocument entity) {
        // TODO consider how to deal with old rawDocument.
        if (entity.getRawDocument() != null) {
            getSession().saveOrUpdate(entity.getRawDocument());
        }
        return super.makePersistent(entity);
    }

    /**
     * Add a raw document to a document, cleanly removing any existing raw
     * document associated with the document.
     *
     * @param doc
     * @param rawDoc
     * @return
     */
    public HRawDocument addRawDocument(HDocument doc, HRawDocument rawDoc) {
        HRawDocument oldRawDoc = doc.getRawDocument();

        if (oldRawDoc != null && !oldRawDoc.equals(rawDoc)) {
            getSession().delete(oldRawDoc);
        }

        if (rawDoc != null) {
            getSession().saveOrUpdate(rawDoc);
        }

        doc.setRawDocument(rawDoc);
        makePersistent(doc);
        return rawDoc;
    }

    public Optional<String> getAdapterParams(String projectSlug,
            String iterationSlug, String docId) {
        HDocument doc =
                getByProjectIterationAndDocId(projectSlug, iterationSlug, docId);
        if (doc != null) {
            HRawDocument rawDoc = doc.getRawDocument();
            if (rawDoc != null) {
                return Optional.fromNullable(rawDoc.getAdapterParameters());
            }
        }
        return Optional.<String> absent();
    }

    public List<HDocument> getDocumentsByIds(List<Long> docIds) {
        StringBuilder query = new StringBuilder();
        query.append("from HDocument doc where doc.id in (:docIds)");

        Query q = getSession().createQuery(query.toString());
        q.setParameterList("docIds", docIds);
        q.setCacheable(true);
        q.setComment("DocumentDAO.getDocumentsByIds");

        List<HDocument> docs = q.list();

        return docs;
    }

    public LobHelper getLobHelper() {
        return getSession().getLobHelper();
    }
}
