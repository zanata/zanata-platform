/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.hibernate.search;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;

/**
 * Analyzer that tokenizes into ngrams of a specified length, with or without
 * case folding.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class ConfigurableNgramAnalyzer extends Analyzer
{

   private int ngramMinLength;
   private int ngramMaxLength;
   private boolean foldCase;

   /**
    * @param ngramLength the length of each ngram to generate
    * @param foldCase true to convert all characters to lowercase, allowing
    *           case-insensitive indexing and searching
    */
   public ConfigurableNgramAnalyzer(int ngramLength, boolean foldCase)
   {
      this(ngramLength, ngramLength, foldCase);
   }

   /**
    * Create analyzer that will tokenize repeatedly to make ngrams of all sizes
    * from ngramMinLength to ngramMaxLength, inclusive.
    * 
    * @param ngramMinLength length of the shortest ngrams to generate
    * @param ngramMaxLength length of the longest ngrams to generate
    * @param foldCase true to convert all characters to lowercase, allowing
    *           case-insensitive indexing and searching
    */
   public ConfigurableNgramAnalyzer(int ngramMinLength, int ngramMaxLength, boolean foldCase)
   {
      this.ngramMinLength = ngramMinLength;
      this.ngramMaxLength = ngramMaxLength;
      this.foldCase = foldCase;
   }

   @Override
   public TokenStream tokenStream(String fieldName, Reader reader)
   {
      TokenStream tokenStream;
      NGramTokenizer ngramTokenizer = new NGramTokenizer(reader, ngramMinLength, ngramMaxLength);
      if (foldCase)
      {
         tokenStream = new ULowerCaseFilter(ngramTokenizer);
      }
      else
      {
         tokenStream = ngramTokenizer;
      }
      return tokenStream;
   }

}
