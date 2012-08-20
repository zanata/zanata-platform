package org.zanata.hibernate.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.util.Version;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.ParameterizedBridge;

import lombok.extern.slf4j.Slf4j;

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
 * 
 */
@Slf4j
public class StringListBridge implements FieldBridge, ParameterizedBridge
{

   private Analyzer analyzer;
   private boolean caseSensitive = false;
   private boolean multiNgrams = false;

   @Override
   public void setParameterValues(@SuppressWarnings("rawtypes") Map parameters)
   {
      if (parameters.containsKey("case"))
      {
         String caseBehaviour = (String) parameters.get("case");
         if ("fold".equals(caseBehaviour))
         {
            caseSensitive = false;
         }
         else if ("preserve".equals(caseBehaviour))
         {
            caseSensitive = true;
         }
         else
         {
            log.warn("invalid value for parameter \"case\": \"{0}\", default will be used", caseBehaviour);
            caseSensitive = false;
         }
      }
      if (parameters.containsKey("ngrams"))
      {
         String ngrams = (String) parameters.get("ngrams");
         if ("multisize".equals(ngrams))
         {
            multiNgrams = true;
         }
         else
         {
            log.warn("invalid value for parameter \"ngrams\": \"{0}\", default will be used", ngrams);
            multiNgrams = false;
         }
      }
   }

   @Override
   public void set(String name, Object value, Document luceneDocument, LuceneOptions luceneOptions)
   {
      if (!(value instanceof List<?>))
      {
         throw new IllegalArgumentException("this bridge must be applied to a List");
      }
      @SuppressWarnings("unchecked")
      List<String> strings = (List<String>) value;
      for (int i = 0; i < strings.size(); i++)
      {
         addStringField(name + i, strings.get(i), luceneDocument, luceneOptions);
      }
   }

   private void addStringField(String fieldName, String fieldValue, Document luceneDocument, LuceneOptions luceneOptions)
   {
      Field field = new Field(fieldName, fieldValue, luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
      field.setBoost(luceneOptions.getBoost());
      luceneDocument.add(field);
   }

}
