package org.zanata.hibernate.search;

/**
 * Labels used in lucene indexes, for use in lucene indexing annotations, field
 * bridges and lucene searches.
 *
 * @author David Mason, damason@redhat.com
 */
public interface IndexFieldLabels {
    public static final String PROJECT_FIELD = "project";
    public static final String ITERATION_FIELD = "iteration";
    /**
     * Represents the full path and name of the document
     */
    public static final String DOCUMENT_ID_FIELD = "documentId";
    public static final String LOCALE_ID_FIELD = "locale";
    public static final String CONTENT_STATE_FIELD = "state";

    public static final String TF_CONTENT = "textFlow.content-nocase";
    public static final String CONTENT = "content-nocase";

    public static final String TF_CONTENT_FIELDS[] = { TF_CONTENT + 0,
            TF_CONTENT + 1, TF_CONTENT + 2, TF_CONTENT + 3, TF_CONTENT + 4,
            TF_CONTENT + 5 };

    public static final String CONTENT_FIELDS[] = { CONTENT + 0, CONTENT + 1,
            CONTENT + 2, CONTENT + 3, CONTENT + 4, CONTENT + 5 };

    public static final String TRANS_UNIT_VARIANT_FIELD = "tuv.";

}
