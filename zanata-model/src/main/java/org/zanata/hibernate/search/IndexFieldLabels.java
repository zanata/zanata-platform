package org.zanata.hibernate.search;

/**
 * Labels used in lucene indexes, for use in lucene indexing annotations, field
 * bridges and lucene searches.
 * 
 * @author David Mason, damason@redhat.com
 */
public interface IndexFieldLabels
{
   public static final String PROJECT_FIELD = "project";
   public static final String ITERATION_FIELD = "iteration";
   /**
    * Represents the full path and name of the document
    */
   public static final String DOCUMENT_ID_FIELD = "documentId";
   public static final String LOCALE_ID_FIELD = "locale";
   public static final String CONTENT_STATE_FIELD = "state";

   public static final String CONTENT_CASE_FOLDED = "content-nocase";
   public static final String CONTENT_CASE_PRESERVED = "content-case";

   public static final String CONTENT_FIELDS_CASE_FOLDED[] = {
      CONTENT_CASE_FOLDED + 0,
      CONTENT_CASE_FOLDED + 1,
      CONTENT_CASE_FOLDED + 2,
      CONTENT_CASE_FOLDED + 3,
      CONTENT_CASE_FOLDED + 4,
      CONTENT_CASE_FOLDED + 5
      };

   public static final String CONTENT_FIELDS_CASE_PRESERVED[] = {
      CONTENT_CASE_PRESERVED + 0,
      CONTENT_CASE_PRESERVED + 1,
      CONTENT_CASE_PRESERVED + 2,
      CONTENT_CASE_PRESERVED + 3,
      CONTENT_CASE_PRESERVED + 4,
      CONTENT_CASE_PRESERVED + 5
      };

}
