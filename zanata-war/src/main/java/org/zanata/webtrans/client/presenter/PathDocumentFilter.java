package org.zanata.webtrans.client.presenter;

import java.util.HashSet;

import org.zanata.webtrans.shared.model.DocumentInfo;
import com.beust.jcommander.internal.Sets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * Filters documents by their full path + name, with substring and exact
 * modes.
 *
 * If there is no pattern set, this filter will accept all documents.
 *
 * @author David Mason, damason@redhat.com
 *
 */
public final class PathDocumentFilter
{
   private static final String DOCUMENT_FILTER_LIST_DELIMITER = ",";

   private HashSet<String> patterns = new HashSet<String>();
   private boolean isFullText = false;
   private boolean caseSensitive = false;

   public boolean accept(DocumentInfo value)
   {
      if (patterns.isEmpty())
      {
         return true;
      }
      String fullPath = value.getPath() + value.getName();
      if (!caseSensitive)
      {
         fullPath = fullPath.toLowerCase();
      }
      for (String pattern : patterns)
      {
         if (!caseSensitive)
         {
            pattern = pattern.toLowerCase();
         }
         if (isFullText)
         {
            if (fullPath.equals(pattern))
            {
               return true;
            }
         }
         else if (fullPath.contains(pattern))
         {
            return true;
         }
      }
      return false; // didn't match any patterns
   }

   public void setPattern(String pattern)
   {
      patterns.clear();
      String[] patternCandidates = pattern.split(DOCUMENT_FILTER_LIST_DELIMITER);

      for (String candidate : patternCandidates)
      {
         candidate = candidate.trim();
         if (candidate.length() != 0)
         {
            patterns.add(candidate);
         }
      }
   }

   public void setFullText(boolean fullText)
   {
      isFullText = fullText;
   }

   public void setCaseSensitive(boolean caseSensitive)
   {
      this.caseSensitive = caseSensitive;
   }
}
