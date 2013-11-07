package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

//@Immutable
public class DocumentStatus implements IsSerializable, Serializable {
    private static final long serialVersionUID = 1L;

    private DocumentId documentId;
    private Date lastTranslatedDate;
    private String lastTranslatedBy;

    // for GWT
    @SuppressWarnings("unused")
    public DocumentStatus() {
        this(null, null, null);
    }

    public DocumentStatus(DocumentId documentId, Date lastTranslatedDate,
            String lastTranslatedBy) {
        this.documentId = documentId;
        this.lastTranslatedDate = lastTranslatedDate;
        this.lastTranslatedBy = lastTranslatedBy;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public Date getLastTranslatedDate() {
        return lastTranslatedDate;
    }

    public String getLastTranslatedBy() {
        return lastTranslatedBy;
    }

    public void update(DocumentId documentId, Date lastTranslatedDate,
            String lastTranslatedBy) {
        this.documentId = documentId;
        this.lastTranslatedBy = lastTranslatedBy;
        this.lastTranslatedDate = lastTranslatedDate;
    }

}
