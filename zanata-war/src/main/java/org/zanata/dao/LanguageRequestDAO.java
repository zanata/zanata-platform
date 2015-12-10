package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.model.HAccount;
import org.zanata.model.LanguageRequest;
import org.zanata.model.type.RequestState;

import java.util.List;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("languageRequestDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LanguageRequestDAO extends AbstractDAOImpl<LanguageRequest, Long> {

    public LanguageRequestDAO() {
        super(LanguageRequest.class);
    }

    public LanguageRequestDAO(Session session) {
        super(LanguageRequest.class, session);
    }

    public LanguageRequest findRequesterPendingRequests(HAccount requester,
        LocaleId localeId) {
        StringBuilder query = new StringBuilder();
        query.append("from LanguageRequest req ")
            .append("where req.locale.localeId = :localeId ")
            .append("and req.request.requester.id = :requesterId ")
            .append("and req.request.state =:state");

        Query q = getSession().createQuery(query.toString())
                .setParameter("requesterId", requester.getId())
                .setParameter("localeId", localeId)
                .setParameter("state", RequestState.NEW)
            .setCacheable(true).setComment(
                        "requestDAO.findRequesterPendingRequests");
        return (LanguageRequest) q.uniqueResult();
    }

    public List<LanguageRequest> findPendingRequests(
        List<LocaleId> localeIds) {
        StringBuilder query = new StringBuilder();
        query.append("from LanguageRequest req ")
            .append("where req.request.state =:state ")
            .append("and req.locale.localeId in (:localeIds)");

        Query q = getSession().createQuery(query.toString())
            .setParameterList("localeIds", localeIds)
            .setParameter("state", RequestState.NEW)
            .setCacheable(true).setComment(
                "requestDAO.findPendingRequests");
        return q.list();
    }
}
