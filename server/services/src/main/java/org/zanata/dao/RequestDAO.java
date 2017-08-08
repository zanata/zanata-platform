package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.zanata.model.LanguageRequest;
import org.zanata.model.Request;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("requestDAO")
@RequestScoped
public class RequestDAO extends AbstractDAOImpl<Request, Long> {

    public RequestDAO() {
        super(Request.class);
    }

    public RequestDAO(Session session) {
        super(Request.class, session);
    }

    public Request getById(Long requestId) {
        return findById(requestId);
    }

    public List<Request> getHistoryByEntityId(String entityId) {
        String query =
            "from Request req where req.entityId= :entityId order by req.validFrom";
        Query q = getSession().createQuery(query)
            .setParameter("entityId", entityId)
            .setCacheable(true).setComment(
                "requestDAO.getHistoryByEntityId");
        return q.list();
    }

    public Request getEntityById(String entityId) {
        String query =
            "from Request req where req.entityId= :entityId and req.validTo is null";
        Query q = getSession().createQuery(query)
            .setParameter("entityId", entityId)
            .setCacheable(true).setComment(
                "requestDAO.getEntityById");
        return (Request) q.uniqueResult();
    }

    public String generateEntityId() {
        return UUID.randomUUID().toString();
    }
}
