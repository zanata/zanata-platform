/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.analyzer.Discriminator;
import org.zanata.common.LocaleId;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import com.ibm.icu.util.ULocale;

/**
 * Analyzer Discriminator to determine the analyzer to use based on the object being indexed.
 * Currently only supports {@link HTextFlow} and {@link HTextFlowTarget}.
 *
 * This is a replacement for Lucene's {@link org.hibernate.search.annotations.AnalyzerDef}
 * annotations as they cannot be used with Analyzer implementations (e.g. {@link StandardAnalyzer}).
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TextContainerAnalyzerDiscriminator implements Discriminator
{

   @Override
   public String getAnalyzerDefinitionName(Object value, Object entity, String field)
   {
      LocaleId localeId;

      if( entity instanceof HTextFlow)
      {
         HTextFlow tf = (HTextFlow)entity;
         localeId = tf.getDocument().getLocale().getLocaleId();
      }
      else if( entity instanceof HTextFlowTarget )
      {
         HTextFlowTarget tft = (HTextFlowTarget)entity;
         localeId = tft.getLocale().getLocaleId();
      }
      else
      {
         throw new IllegalArgumentException("Illegal text container type: " + entity.getClass().getName());
      }

      return getAnalyzerDefinitionName( localeId.getId() );
   }

   /**
    * Finds the name of an analyzer definition for a given locale.
    * This method is exposed mainly for determining an analyzer when querying.
    *
    * @param localeId Language that is being queried.
    * @return The name of an analyzer definition as annotated using {@link org.hibernate.search.annotations.AnalyzerDef}
    */
   public static String getAnalyzerDefinitionName( String localeId )
   {
      // CJK languages
      ULocale uLocale = new ULocale(localeId);
      String langCode = uLocale.getLanguage();

      if( langCode.equalsIgnoreCase("zh") || langCode.equalsIgnoreCase("ja") || langCode.equalsIgnoreCase("ko") )
      {
         return "UnigramAnalyzer";
      }
      // All other languages
      else
      {
         return "StandardAnalyzer";
      }
   }
}
