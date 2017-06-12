package org.zanata.hibernate.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
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
 * <li>Project id is indexed as 'project'</li>
 * <li>Iteration id is indexed as 'projectVersion'</li>
 * <li>Document full path + name is indexed as 'documentId'</li>
 * </ul>
 *
 * @author David Mason, damason@redhat.com
 *
 */
public class ContainingWorkspaceBridge extends AbstractFieldBridge {

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

        addStringField(IndexFieldLabels.PROJECT_ID_FIELD, project.getId().toString(),
                luceneDocument, luceneOptions);
        addStringField(IndexFieldLabels.PROJECT_VERSION_ID_FIELD,
                iteration.getId().toString(),
                luceneDocument, luceneOptions);
        addStringField(IndexFieldLabels.DOCUMENT_ID_FIELD, doc.getDocId(),
                luceneDocument, luceneOptions);
    }

    private void addStringField(String fieldName, String fieldValue,
            Document luceneDocument, LuceneOptions luceneOptions) {
        FieldType fieldType = translateFieldType(luceneOptions);
        Field field = new Field(fieldName, fieldValue, fieldType);
        field.setBoost(luceneOptions.getBoost());
        luceneDocument.add(field);
    }
}
