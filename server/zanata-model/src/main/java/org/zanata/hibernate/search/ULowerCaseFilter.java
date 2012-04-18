package org.zanata.hibernate.search;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import com.ibm.icu.lang.UCharacter;

/**
 * Uses ICU case folding to convert tokens to lowercase.
 * 
 * @author David Mason, damason@redhat.com
 */
public class ULowerCaseFilter extends TokenFilter
{

   private TermAttribute termText;

   public ULowerCaseFilter(TokenStream input)
   {
      super(input);
      termText = (TermAttribute) addAttribute(TermAttribute.class);
   }

   public final boolean incrementToken() throws IOException
   {
      boolean hasToken = input.incrementToken();
      if (hasToken)
      {
         final char[] buffer = termText.termBuffer();
         final int length = termText.termLength();
         for (int i = 0; i < length; i++)
         {
            buffer[i] = (char) UCharacter.foldCase(buffer[i], true);
         }
      }
      return hasToken;
   }
}
