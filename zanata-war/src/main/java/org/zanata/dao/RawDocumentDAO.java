package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HRawDocument;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("rawDocumentDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
@Slf4j
public class RawDocumentDAO extends AbstractDAOImpl<HRawDocument, Long> {
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
