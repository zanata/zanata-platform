package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.zanata.model.HRawDocument;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("rawDocumentDAO")
@RequestScoped
public class RawDocumentDAO extends AbstractDAOImpl<HRawDocument, Long> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RawDocumentDAO.class);

    public RawDocumentDAO() {
        super(HRawDocument.class);
    }

    public RawDocumentDAO(Session session) {
        super(HRawDocument.class, session);
    }

    public HRawDocument getByDocumentId(Long docId) {
        Query q = getSession().createQuery(
                "from HRawDocument rawDoc where rawDoc.document.id = :docId");
        q.setParameter("docId", docId);
        q.setCacheable(true);
        q.setComment("RawDocumentDAO.getByDocumentId");
        return (HRawDocument) q.uniqueResult();
    }
}
