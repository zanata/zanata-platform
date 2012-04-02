package org.zanata.hibernate.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HIterationProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;

/**
 * Index the project, version and document for a HTextFlow field.
 * 
 * For a field labeled 'fieldName'
 * <ul>
 *   <li>Project slug is indexed as 'fieldName.project'</li>
 *   <li>Iteration slug is indexed as 'fieldName.iteration'</li>
 *   <li>Document full path + name is indexed as 'fieldName.documentId'</li>
 * </ul>
 * 
 * @author David Mason, damason@redhat.com
 *
 */
public class ContainingWorkspaceBridge implements FieldBridge
{
   public static final String PROJECT_FIELD = ".project";
   public static final String ITERATION_FIELD = ".iteration";
   public static final String DOCUMENT_ID_FIELD = ".documentId";

   @Override
   public void set(String name, Object value, Document luceneDocument, LuceneOptions luceneOptions)
   {
      HTextFlow textFlow = (HTextFlow) value;
      HDocument doc = textFlow.getDocument();
      HProjectIteration iteration = doc.getProjectIteration();
      HIterationProject project = iteration.getProject();

      addStringField(name + PROJECT_FIELD, project.getSlug(), luceneDocument, luceneOptions);
      addStringField(name + ITERATION_FIELD, iteration.getSlug(), luceneDocument, luceneOptions);
      addStringField(name + DOCUMENT_ID_FIELD, doc.getDocId(), luceneDocument, luceneOptions);
   }

   private void addStringField(String fieldName, String fieldValue, Document luceneDocument, LuceneOptions luceneOptions)
   {
      Field field = new Field(fieldName, fieldValue, luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
      field.setBoost(luceneOptions.getBoost());
      luceneDocument.add(field);
   }

}
