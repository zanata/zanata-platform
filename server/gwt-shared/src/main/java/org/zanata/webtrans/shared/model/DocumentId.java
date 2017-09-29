package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.IsSerializable;

//@Immutable
public class DocumentId implements Identifier<Long>, Comparable<DocumentId>,
        IsSerializable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SEPARATOR = ":";

    private Long id;

    private String docId;

    // for GWT
    public DocumentId() {
    }

    public DocumentId(Long id, String docId) {
        this.id = id;
        this.docId = docId;
    }

    @Override
    public String toString() {
        return id + SEPARATOR + docId;
    }

    public static DocumentId valueOf(String value) {
        String[] parts = value.split(SEPARATOR);
        Preconditions.checkArgument(parts.length == 2);
        return new DocumentId(Long.parseLong(parts[0]), parts[1]);
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
    public int compareTo(DocumentId o) {
        if (o == this) {
            return 0;
        }
        if (o == null) {
            return -1;
        }
        DocumentId compareTo = o;
        return this.getDocId().compareTo(compareTo.getDocId());
    }
}
