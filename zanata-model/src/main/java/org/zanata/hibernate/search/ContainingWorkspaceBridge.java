package org.zanata.hibernate.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;

/**
 * Index the project, version and document for a HTextFlow field.
 *
 * The provided field name is not used
 * <ul>
 * <li>Project slug is indexed as 'project'</li>
 * <li>Iteration slug is indexed as 'iteration'</li>
 * <li>Document full path + name is indexed as 'documentId'</li>
 * </ul>
 *
 * @author David Mason, damason@redhat.com
 *
 */
public class ContainingWorkspaceBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document luceneDocument,
            LuceneOptions luceneOptions) {
        HDocument doc;
        if (value instanceof HTextFlow) {
            doc = ((HTextFlow) value).getDocument();
        } else if (value instanceof HDocument) {
            doc = (HDocument) value;
        } else {
            throw new IllegalArgumentException(
                    "ContainingWorkspaceBridge used on a non HDocument or HTextFlow type");
        }

        HProjectIteration iteration = doc.getProjectIteration();
        HProject project = iteration.getProject();

        addStringField(IndexFieldLabels.PROJECT_FIELD, project.getSlug(),
                luceneDocument, luceneOptions);
        addStringField(IndexFieldLabels.ITERATION_FIELD, iteration.getSlug(),
                luceneDocument, luceneOptions);
        addStringField(IndexFieldLabels.DOCUMENT_ID_FIELD, doc.getDocId(),
                luceneDocument, luceneOptions);
    }

    private void addStringField(String fieldName, String fieldValue,
            Document luceneDocument, LuceneOptions luceneOptions) {
        Field field =
                new Field(fieldName, fieldValue, luceneOptions.getStore(),
                        luceneOptions.getIndex(), luceneOptions.getTermVector());
        field.setBoost(luceneOptions.getBoost());
        luceneDocument.add(field);
    }

}
