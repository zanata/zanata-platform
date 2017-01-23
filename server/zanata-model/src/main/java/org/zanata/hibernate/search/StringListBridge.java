package org.zanata.hibernate.search;

import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * Index a list of strings in multiple fields, appending the string index to the
 * field name to produce unique fields.
 *
 * e.g. For a field labeled 'fieldName' for a list of 3 strings
 * <ul>
 * <li>First string is indexed as 'fieldName0'</li>
 * <li>Second string is indexed as 'fieldName1'</li>
 * <li>Third string is indexed as 'fieldName2'</li>
 * </ul>
 *
 * @author David Mason, damason@redhat.com
 */
public class StringListBridge implements FieldBridge {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(StringListBridge.class);

    @Override
    public void set(String name, Object value, Document luceneDocument,
            LuceneOptions luceneOptions) {
        if (!(value instanceof List<?>)) {
            throw new IllegalArgumentException(
                    "this bridge must be applied to a List");
        }
        @SuppressWarnings("unchecked")
        List<String> strings = (List<String>) value;
        for (int i = 0; i < strings.size(); i++) {
            addStringField(name + i, strings.get(i), luceneDocument,
                    luceneOptions);
        }
    }

    private void addStringField(String fieldName, String fieldValue,
            Document luceneDocument, LuceneOptions luceneOptions) {
        Field field = new Field(fieldName, fieldValue, luceneOptions.getStore(),
                luceneOptions.getIndex(), luceneOptions.getTermVector());
        field.setBoost(luceneOptions.getBoost());
        luceneDocument.add(field);
    }
}
