package org.zanata.hibernate.search;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class AbstractFieldBridge implements FieldBridge {
    /**
     * Copy of {@link Field#translateFieldType}
     */
    protected static final FieldType translateFieldType(
            LuceneOptions luceneOptions) {
        final FieldType ft = new FieldType();

        ft.setStored(luceneOptions.getStore() == Field.Store.YES);

        switch (luceneOptions.getIndex()) {
            case ANALYZED:
                ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                ft.setTokenized(true);
                break;
            case ANALYZED_NO_NORMS:
                ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                ft.setTokenized(true);
                ft.setOmitNorms(true);
                break;
            case NOT_ANALYZED:
                ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                ft.setTokenized(false);
                break;
            case NOT_ANALYZED_NO_NORMS:
                ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                ft.setTokenized(false);
                ft.setOmitNorms(true);
                break;
            case NO:
                break;
        }

        switch (luceneOptions.getTermVector()) {
            case NO:
                break;
            case YES:
                ft.setStoreTermVectors(true);
                break;
            case WITH_POSITIONS:
                ft.setStoreTermVectors(true);
                ft.setStoreTermVectorPositions(true);
                break;
            case WITH_OFFSETS:
                ft.setStoreTermVectors(true);
                ft.setStoreTermVectorOffsets(true);
                break;
            case WITH_POSITIONS_OFFSETS:
                ft.setStoreTermVectors(true);
                ft.setStoreTermVectorPositions(true);
                ft.setStoreTermVectorOffsets(true);
                break;
        }
        ft.freeze();
        return ft;
    }
}
