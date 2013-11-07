package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

//@Immutable
public class DocumentId implements Identifier<Long>, Comparable,
        IsSerializable, Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String docId;

    // for GWT
    @SuppressWarnings("unused")
    private DocumentId() {
    }

    public DocumentId(Long id, String docId) {
        this.id = id;
        this.docId = docId;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public int hashCode() {
        return id.intValue();
    }

    @Override
    public Long getValue() {
        return id;
    }

    public Long getId() {
        return id;
    }

    public String getDocId() {
        return docId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return obj instanceof DocumentId && ((DocumentId) obj).id.equals(id);
    }

    @Override
    public int compareTo(Object o) {
        if (o == this) {
            return 0;
        }
        if (o == null) {
            return -1;
        }
        if (o instanceof DocumentId) {
            DocumentId compareTo = (DocumentId) o;
            return this.getDocId().compareTo(compareTo.getDocId());
        }
        return -1;
    }
}
