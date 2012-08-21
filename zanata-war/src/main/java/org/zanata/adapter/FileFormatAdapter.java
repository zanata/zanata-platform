/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * Common interface for classes wrapping Okapi filters.
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public interface FileFormatAdapter
{

   /**
    * Extract source strings from the given document content.
    * 
    * @param documentContent
    * @param sourceLocale
    * @return representation of the strings in the document
    */
   // TODO may want to use a string locale id so it can be used both for Zanata and Okapi locale classes
   Resource parseDocumentFile(InputStream documentContent, LocaleId sourceLocale);

   /**
    * Extract translation strings from the given translation document.
    * 
    * @param translatedDocumentContent translated document to parse
    * @return representation of the translations in the document
    */
   TranslationsResource parseTranslationFile(InputStream translatedDocumentContent);

   /**
    * Write translated file to the given output, using the given list of translations.
    * 
    * @param output stream to write translated document
    * @param original source document
    * @param translations to use in generating translated file
    * @param locale to use for translated document
    * @throws IOException
    */
   void writeTranslatedFile(OutputStream output, InputStream original, Map<String, TextFlowTarget> translations, String locale) throws IOException;

}
