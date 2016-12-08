package org.zanata.dao;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationFinder;

@Named("textFlowTargetDAO")
@RequestScoped
public class TextFlowTargetDAO extends AbstractDAOImpl<HTextFlowTarget, Long>
        implements TranslationFinder {

    public TextFlowTargetDAO() {
        super(HTextFlowTarget.class);
    }

    public TextFlowTargetDAO(Session session) {
        super(HTextFlowTarget.class, session);
    }

    /**
     * @param textFlow
     * @param locale
     * @return
     */
    public HTextFlowTarget getByNaturalId(HTextFlow textFlow, HLocale locale) {
        return (HTextFlowTarget) getSession()
                .byNaturalId(HTextFlowTarget.class).using("textFlow", textFlow)
                .using("locale", locale).load();
    }

    public List<HTextFlowTarget> getByTextFlowId(Long tfId, int offset,
            int maxResults) {
        Query q =
                getSession()
                        .createQuery(
                                "from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.id = :tfId");
        q.setParameter("tfId", tfId);
        q.setFirstResult(offset);
        q.setMaxResults(maxResults);
        q.setCacheable(true)
                .setComment("TextFlowTargetDAO.getByTextFlowId");
        return q.list();
    }

    public int countTextFlowTargetsInTextFlow(Long tfId) {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.id = :tfId");
        q.setParameter("tfId", tfId);
        q.setCacheable(true).setComment(
                "TextFlowTargetDAO.countTextFlowTargetsInTextFlow");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0 : totalCount.intValue();
    }

    public int getTotalTextFlowTargets() {
        Query q =
                getSession()
                        .createQuery("select count(*) from HTextFlowTarget");
        q.setCacheable(true);
        q.setComment("TextFlowTargetDAO.getTotalTextFlowTargets");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalActiveTextFlowTargets() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlowTarget t where t.textFlow.obsolete=0");
        q.setCacheable(true);
        q.setComment("TextFlowTargetDAO.getTotalActiveTextFlowTargets");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalObsoleteTextFlowTargets() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlowTarget t where t.textFlow.obsolete=1");
        q.setCacheable(true);
        q.setComment("TextFlowTargetDAO.getTotalObsoleteTextFlowTargets");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalApprovedOrTranslatedTextFlowTargets() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlowTarget t where t.state = :state or t.state = :state2 and t.textFlow.obsolete=0");
        q.setCacheable(true);
        q.setComment("TextFlowTargetDAO.getTotalApprovedOrTranslatedTextFlowTargets");
        Long totalCount =
                (Long) q.setParameter("state", ContentState.Approved)
                        .setParameter("state2", ContentState.Translated)
                        .uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalRejectedOrFuzzyTextFlowTargets() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlowTarget t where t.state = :state or t.state = :state2 and t.textFlow.obsolete=0");
        q.setCacheable(true);
        q.setComment("TextFlowTargetDAO.getTotalRejectedOrFuzzyTextFlowTargets");
        Long totalCount =
                (Long) q.setParameter("state", ContentState.NeedReview)
                        .setParameter("state2", ContentState.Rejected)
                        .uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalNewTextFlowTargets() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
        q.setCacheable(true);
        q.setComment("TextFlowTargetDAO.getTotalNewTextFlowTargets");
        Long totalCount =
                (Long) q.setParameter("state", ContentState.New).uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    /**
     * Finds all (including obsolete) translations for 'document' in 'locale'.
     *
     * @param document
     * @param localeId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<HTextFlowTarget> findAllTranslations(HDocument document,
            LocaleId localeId) {
        Query q =
                getSession().createQuery(
                        "select t from HTextFlowTarget t where "
                                + "t.textFlow.document =:document "
                                + "and t.locale.localeId =:localeId "
                                + "order by t.textFlow.pos");
        q.setParameter("document", document);
        q.setParameter("localeId", localeId);
        q.setCacheable(false);
        q.setComment("TextFlowTargetDAO.findAllTranslations");
        return q.list();
    }

    /**
     * Finds non-obsolete translations for 'document' in 'locale'.
     *
     * @param document
     * @param locale
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<HTextFlowTarget> findTranslations(HDocument document,
            HLocale locale) {
        Query q =
                getSession().createQuery(
                        "select t " + "from HTextFlowTarget t where "
                                + "t.textFlow.document =:document "
                                + "and t.locale =:locale "
                                + "and t.textFlow.obsolete=false "
                                + "order by t.textFlow.pos");
        q.setParameter("document", document);
        q.setParameter("locale", locale);
        q.setCacheable(true);
        q.setComment("TextFlowTargetDAO.findTranslations");
        return q.list();
    }

    /**
     * Finds the best matching translations for a single text flow and a locale.
     * Other parameters (context, document id, and project) can also influence
     * what the best match is. A matching translation is one where the source
     * text is exactly the same as the translation within the document and in
     * the same locale.
     *
     * @param textFlow
     *            The text flow for which to find a matching translation.
     * @param targetLocaleId
     *            The locale in which to find matches.
     * @param checkContext
     *            Whether to check for a matching context
     * @param checkDocument
     *            Whether to check for a matching document id
     * @param checkProject
     *            Whether to check for a matching project
     * @return The single best possible match found in the system for the given
     *         text flow.
     */
    @Override
    @NativeQuery
    public Optional<HTextFlowTarget> searchBestMatchTransMemory(
            HTextFlow textFlow, LocaleId targetLocaleId,
            LocaleId sourceLocaleId, boolean checkContext,
            boolean checkDocument, boolean checkProject) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("SELECT tft.* FROM HTextFlowTarget tft ")
                .append("JOIN HTextFlow tf ON tf.id = tft.tf_id ")
                .append("JOIN HLocale locale ON locale.id = tft.locale ")
                .append("JOIN HDocument hDoc ON hDoc.id = tf.document_id ")
                .append("JOIN HProjectIteration iter ON iter.id = hDoc.project_iteration_id ")
                .append("JOIN HProject project ON project.id = iter.project_id ")
                .append("WHERE tf.contentHash = :contentHash AND locale.localeId = :localeId ")
                .append("AND tft.tf_id = tf.id ")
                .append("AND tft.locale = locale.id ")
                .append("AND tf.document_id = hDoc.id ")
                .append("AND hDoc.project_iteration_id = iter.id ")
                .append("AND iter.project_id = project.id ")
                .append("AND tft.state in (2, 3) AND tft.tf_id <> :textFlowId AND iter.status <> 'O' AND project.status <> 'O' ");
        if (checkContext) {
            queryBuilder.append("AND tf.resId = :resId ");
        }
        if (checkDocument) {
            queryBuilder.append("AND hDoc.docId = :docId ");
        }
        if (checkProject) {
            queryBuilder.append("AND project.id = :projectId ");
        }
        queryBuilder
                .append("ORDER BY ")
                .append(" CASE WHEN tf.resId = :resId THEN 0 ELSE 1 END, ")
                .append(" CASE WHEN hDoc.docId = :docId THEN 0 ELSE 1 END, ")
                .append(" CASE WHEN iter.project_id = :projectId THEN 0 ELSE 1 END, ")
                .append(" tft.lastChanged DESC ")
                .append("LIMIT 1");

        SQLQuery sqlQuery =
                getSession().createSQLQuery(queryBuilder.toString());
        sqlQuery.setParameter("textFlowId", textFlow.getId());
        sqlQuery.setParameter("contentHash", textFlow.getContentHash());
        sqlQuery.setParameter("resId", textFlow.getResId());
        sqlQuery.setParameter("docId", textFlow.getDocument().getDocId());
        sqlQuery.setParameter("projectId", textFlow.getDocument()
                .getProjectIteration().getProject().getId());
        sqlQuery.setParameter("localeId", targetLocaleId.getId());
        sqlQuery.addEntity(HTextFlowTarget.class);
        return Optional.ofNullable((HTextFlowTarget) sqlQuery.uniqueResult());
    }

    /**
     * Look up the {@link HTextFlowTarget} for the given hLocale in hTextFlow,
     * creating a new one if none is present.
     *
     * @param hTextFlow
     *            The parent text flow.
     * @param hLocale
     *            The locale for the text flow target.
     */
    public HTextFlowTarget getOrCreateTarget(HTextFlow hTextFlow,
            HLocale hLocale) {
        HTextFlowTarget hTextFlowTarget = getTextFlowTarget(hTextFlow, hLocale);

        if (hTextFlowTarget == null) {
            hTextFlowTarget = new HTextFlowTarget(hTextFlow, hLocale);
            hTextFlowTarget.setVersionNum(0); // this will be incremented when
                                              // content is set (below)
            // TODO getTargets just to make sure hTextFlowTarget is persisted in
            // the end
            hTextFlow.getTargets().put(hLocale.getId(), hTextFlowTarget);
            // getSession().persist(hTextFlowTarget);
        }
        return hTextFlowTarget;
    }

    /**
     * Look up the {@link HTextFlowTarget} for the given hLocale in hTextFlow.
     * If none can be found, return null.
     *
     * @param hTextFlow
     *            The parent text flow.
     * @param hLocale
     *            The locale for the text flow target.
     */
    public HTextFlowTarget getTextFlowTarget(HTextFlow hTextFlow,
            HLocale hLocale) {
        HTextFlowTarget hTextFlowTarget =
                (HTextFlowTarget) getSession()
                        .createQuery(
                                "select tft from HTextFlowTarget tft "
                                        + "where tft.textFlow = :textFlow "
                                        + "and tft.locale = :locale")
                        .setParameter("textFlow", hTextFlow)
                        .setParameter("locale", hLocale)
                        .setComment("TextFlowTargetDAO.getTextFlowTarget")
                        .uniqueResult();
        return hTextFlowTarget;
    }

    public HTextFlowTarget
            getTextFlowTarget(Long hTextFlowId, LocaleId localeId) {
        HTextFlowTarget hTextFlowTarget =
                (HTextFlowTarget) getSession()
                        .createQuery(
                                "select tft from HTextFlowTarget tft "
                                        + "where tft.textFlow.id = :hTextFlowId "
                                        + "and tft.locale.localeId = :localeId")
                        .setParameter("hTextFlowId", hTextFlowId)
                        .setParameter("localeId", localeId)
                        .setComment("TextFlowTargetDAO.getTextFlowTarget")
                        .uniqueResult();
        return hTextFlowTarget;
    }

    @SuppressWarnings("unchecked")
    public List<HTextFlowTarget> findByTextFlowIdList(List<Long> idList,
            LocaleId localeId) {
        if (idList == null || idList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Query query =
                getSession()
                        .createQuery(
                                "select tft from HTextFlowTarget tft "
                                        + "where tft.textFlow.id in (:idList) "
                                        + "and tft.locale.localeId = :localeId");

        query.setParameterList("idList", idList);
        query.setParameter("localeId", localeId);
        query.setCacheable(false).setComment(
                "TextFlowTargetDAO.findByTextFlowIdList");
        return query.list();
    }

    public HTextFlowTarget getTextFlowTarget(HTextFlow hTextFlow,
            LocaleId localeId) {
        HTextFlowTarget hTextFlowTarget =
                (HTextFlowTarget) getSession()
                        .createQuery(
                                "select tft from HTextFlowTarget tft "
                                        + "where tft.textFlow = :textFlow "
                                        + "and tft.locale.localeId = :localeId")
                        .setParameter("textFlow", hTextFlow)
                        .setParameter("localeId", localeId)
                        .setComment("TextFlowTargetDAO.getTextFlowTarget")
                        .uniqueResult();
        return hTextFlowTarget;
    }

    public Long getTextFlowTargetId(HTextFlow hTextFlow, LocaleId localeId) {
        Query q =
                getSession().createQuery(
                        "select tft.id from HTextFlowTarget tft "
                                + "where tft.textFlow = :textFlow "
                                + "and tft.locale.localeId = :localeId");
        q.setParameter("textFlow", hTextFlow);
        q.setParameter("localeId", localeId);
        q.setComment("TextFlowTargetDAO.getTextFlowTargetId");
        Long id = (Long) q.uniqueResult();
        return id;
    }

    public HTextFlowTarget getLastTranslated(String projectSlug,
            String iterationSlug, LocaleId localeId) {
        StringBuilder query = new StringBuilder();
        query.append("from HTextFlowTarget tft ");
        query.append("where tft.textFlow.document.projectIteration.slug = :iterationSlug ");
        query.append("and tft.textFlow.document.projectIteration.project.slug = :projectSlug ");
        query.append("and tft.locale.localeId = :localeId ");
        query.append("order by tft.lastChanged DESC");

        Query q = getSession().createQuery(query.toString());
        q.setParameter("iterationSlug", iterationSlug);
        q.setParameter("projectSlug", projectSlug);
        q.setParameter("localeId", localeId);
        q.setCacheable(true);
        q.setMaxResults(1);
        q.setComment("TextFlowTargetDAO.getLastTranslated");

        return (HTextFlowTarget) q.uniqueResult();
    }

    /**
     * @param document
     *            copyTrans copy target document
     * @param targetLocale
     *            target locale
     * @return number of translation candidates in given locale and is from
     *         given docId (exclude translations from target document's
     *         iteration)
     */
    public long getTranslationCandidateCountWithDocIdAndLocale(
            HDocument document,
            HLocale targetLocale) {
        String queryString =
                "select count(*) from HTextFlowTarget tft " +
                        "where tft.textFlow.document.docId = :docId " +
                        "and tft.locale = :locale " +
                        "and tft.textFlow.obsolete = false " +
                        "and tft.textFlow.document.obsolete = false " +
                        "and tft.textFlow.document.projectIteration <> :self";
        Query query =
                getSession()
                        .createQuery(queryString)
                        .setParameter("docId", document.getDocId())
                        .setParameter("locale", targetLocale)
                        .setParameter("self", document.getProjectIteration())
                        .setCacheable(true)
                        .setComment(
                                "TextFlowTargetDAO.getTranslationCandidateCountWithDocIdAndLocale");
        return (Long) query.uniqueResult();
    }

    /**
     * @param document
     *            copyTrans copy target document
     * @param targetLocale
     *            target locale
     * @return number of translation candidates in given locale and is from
     *         given project (exclude translations from target document's
     *         iteration)
     */
    public long getTranslationCandidateCountWithProjectAndLocale(
            HDocument document, HLocale targetLocale) {
        HProjectIteration projectIteration = document.getProjectIteration();
        HProject project = projectIteration.getProject();
        String queryString =
                "select count(*) from HTextFlowTarget tft " +
                        "where tft.textFlow.document.projectIteration.project = :project " +
                        "and tft.locale = :locale " +
                        "and tft.textFlow.obsolete = false " +
                        "and tft.textFlow.document.obsolete = false " +
                        "and tft.textFlow.document.projectIteration.status <> :obsoleteStatus " +
                        "and tft.textFlow.document.projectIteration <> :self";

        Query query =
                getSession()
                        .createQuery(queryString)
                        .setParameter("project", project)
                        .setParameter("locale", targetLocale)
                        .setParameter("self", projectIteration)
                        .setParameter("obsoleteStatus", EntityStatus.OBSOLETE)
                        .setCacheable(true)
                        .setComment(
                                "TextFlowTargetDAO.getTranslationCandidateCountWithProjectAndLocale");
        return (Long) query.uniqueResult();
    }
}
