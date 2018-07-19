package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 * Copy of org.zanata.model.type.TranslationSourceType for GWT
 */
public class TranslationSourceType implements IsSerializable, Serializable {
    private String metadata;
    private String abbr;

    // hard coded to Google
    public final static TranslationSourceType MACHINE_TRANS =
        new TranslationSourceType("MT", "Google");

    public final static TranslationSourceType COPY_TRANS =
        new TranslationSourceType("CT");
    public final static TranslationSourceType COPY_VERSION =
        new TranslationSourceType("CV");
    public final static TranslationSourceType MERGE_VERSION =
        new TranslationSourceType("MV");
    public final static TranslationSourceType TM_MERGE =
        new TranslationSourceType("TM");
    public final static TranslationSourceType GWT_EDITOR_ENTRY =
        new TranslationSourceType("GWT");
    public final static TranslationSourceType JS_EDITOR_ENTRY =
        new TranslationSourceType("JS");
    public final static TranslationSourceType API_UPLOAD =
        new TranslationSourceType("API");
    public final static TranslationSourceType WEB_UPLOAD =
        new TranslationSourceType("WEB");
    public final static TranslationSourceType UNKNOWN =
        new TranslationSourceType("UNK");

    // for GWT
    @SuppressWarnings("unused")
    private TranslationSourceType() {
    }

    private TranslationSourceType(String abbr) {
        this(abbr, null);
    }

    private TranslationSourceType(String abbr, String metadata) {
        this.abbr = abbr;
        this.metadata = metadata;
    }

    public static TranslationSourceType getInstance(String abbr) {
        switch (abbr) {
            case "CT":
                return COPY_TRANS;
            case "CV":
                return COPY_VERSION;
            case "MV":
                return MERGE_VERSION;
            case "TM":
                return TM_MERGE;
            case "GWT":
                return GWT_EDITOR_ENTRY;
            case "JS":
                return JS_EDITOR_ENTRY;
            case "API":
                return API_UPLOAD;
            case "WEB":
                return WEB_UPLOAD;
            case "MT":
                return MACHINE_TRANS;
            default:
                return UNKNOWN;
        }
    }

    public String getMetadata() {
        return metadata;
    }

    public String getAbbr() {
        return abbr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslationSourceType that = (TranslationSourceType) o;
        return Objects.equals(metadata, that.metadata) &&
            Objects.equals(abbr, that.abbr);
    }

    @Override
    public int hashCode() {

        return Objects.hash(metadata, abbr);
    }
}
