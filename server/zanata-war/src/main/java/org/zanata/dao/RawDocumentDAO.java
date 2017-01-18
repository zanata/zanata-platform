package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.zanata.model.HRawDocument;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("rawDocumentDAO")
@RequestScoped
public class RawDocumentDAO extends AbstractDAOImpl<HRawDocument, Long> {
    private static final Logger log =
            org.slf4j.LoggerFactory.getLogger(RawDocumentDAO.class);

    public RawDocumentDAO() {
        super(HRawDocument.class);
    }

    public RawDocumentDAO(Session session) {
        super(HRawDocument.class, session);
    }

    public HRawDocument getByDocumentId(Long docId) {
        Query q =
                getSession()
                        .createQuery(
                                "from HRawDocument rawDoc where rawDoc.document.id = :docId");
        q.setParameter("docId", docId);
        q.setCacheable(true);
        q.setComment("RawDocumentDAO.getByDocumentId");

        return (HRawDocument) q.uniqueResult();
    }
}
