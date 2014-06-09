package org.zanata.dao;

import java.util.List;

import com.google.common.base.Optional;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationFinder;

@Name("textFlowTargetDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
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
    public Optional<HTextFlowTarget> searchBestMatchTransMemory(
            HTextFlow textFlow, LocaleId targetLocaleId,
            LocaleId sourceLocaleId, boolean checkContext,
            boolean checkDocument, boolean checkProject) {

        StringBuilder queryStr =
                new StringBuilder(
                        "select match\n"
                                + "from HTextFlowTarget match\n"
                                + "where match.textFlow.contentHash = :contentHash\n"
                                + "and match.locale.localeId = :targetLocaleId\n"
                                +
                                // It's fine to reuse translation in Translated
                                // state even if it came from a reviewable
                                // project
                                "and match.state in (:approvedState, :translatedState)\n"
                                +
                                // Do not reuse its own translation
                                "and match.textFlow.id != :textFlowId\n"
                                +
                                // Do not reuse matches from obsolete entities
                                // (iteration, project)
                                // Obsolete document translations ARE reused
                                "and match.textFlow.document.projectIteration.status != :obsoleteEntityStatus\n"
                                + "and match.textFlow.document.projectIteration.project.status != :obsoleteEntityStatus\n");
        if (checkContext) {
            queryStr.append("and match.textFlow.resId = :resId\n");
        }
        if (checkDocument) {
            queryStr.append("and match.textFlow.document.docId = :docId\n");
        }
        if (checkProject) {
            queryStr.append("and match.textFlow.document.projectIteration.project.id = :projectId\n");
        }
        queryStr.append("order by\n"
                + "  (case when match.textFlow.resId = :resId then 0 else 1 end),\n"
                + "  (case when match.textFlow.document.docId\n"
                + "    = :docId then 0 else 1 end),\n"
                + "  (case when match.textFlow.document.projectIteration.project.id\n"
                + "    = :projectId then 0 else 1 end),\n"
                + "  match.lastChanged desc\n");

        Query q = getSession().createQuery(queryStr.toString());

        q.setParameter("textFlowId", textFlow.getId());
        q.setParameter("contentHash", textFlow.getContentHash());
        q.setParameter("resId", textFlow.getResId());
        q.setParameter("docId", textFlow.getDocument().getDocId());
        q.setParameter("projectId", textFlow.getDocument()
                .getProjectIteration().getProject().getId());
        q.setParameter("targetLocaleId", targetLocaleId);
        q.setParameter("approvedState", ContentState.Approved);
        q.setParameter("translatedState", ContentState.Translated);
        q.setParameter("obsoleteEntityStatus", EntityStatus.OBSOLETE);
        q.setCacheable(false);
        // don't try to cache scrollable results
        q.setComment("TextFlowTargetDAO.findMatchingTranslations");
        q.setMaxResults(1); // Get the first one (should be the best match
                            // because of the order by clause)
        return Optional.fromNullable((HTextFlowTarget) q.uniqueResult());
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
            // TODO pahuang getTargets just to make sure hTextFlowTarget is persisted in the end
            hTextFlow.getTargets().put(hLocale.getId(), hTextFlowTarget);
//            getSession().persist(hTextFlowTarget);
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
}
