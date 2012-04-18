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
   public static final String DOCUMENT_ID_FIELD = "documentId";
   public static final String LOCALE_ID_FIELD = "locale";

   public static final String CONTENT_CASE_FOLDED = "content-nocase";
   public static final String CONTENT_CASE_PRESERVED = "content-case";
}
