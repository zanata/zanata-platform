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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.zanata.common.LocaleId;
import org.zanata.model.tm.TransMemoryUnitVariant;

/**
 * This is a field bridge used to index all Translation Unit variants in a translation unit.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TransUnitVariantClassBridge implements FieldBridge
{
   @Override
   public void set(String s, Object value, Document document, LuceneOptions luceneOptions)
   {
      TransMemoryUnitVariant variant = (TransMemoryUnitVariant)value;

      String textToIndex = variant.getPlainTextSegment();
      Field field = new Field("tuv." + variant.getLanguage(), textToIndex,
            luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
      field.setBoost(luceneOptions.getBoost());
      document.add(field);
   }
}
