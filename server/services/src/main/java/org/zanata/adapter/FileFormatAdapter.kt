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
package org.zanata.adapter

import java.io.OutputStream
import java.net.URI

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.zanata.common.LocaleId
import org.zanata.exception.FileFormatAdapterException
import org.zanata.model.HDocument
import org.zanata.rest.dto.resource.Resource
import org.zanata.rest.dto.resource.TranslationsResource

import org.zanata.common.dto.TranslatedDoc

/**
 * Common interface for classes wrapping Okapi filters. Each implementation must
 * have a public no-arg constructor.
 *
 * @author David Mason, [damason@redhat.com](mailto:damason@redhat.com)
 */
interface FileFormatAdapter {

    // for parseDocumentFile(), parseTranslationFile()
    data class ParserOptions(
            /** location of the document to parse */
            // (formerly originalFile/originalDoc/documentUri)
            val rawFile: URI?,
            // document locale
            /** locale of document */
            val locale: LocaleId,
            /** adapter-specific parameter string. See documentation for
             * individual adapters. */
            val params: String
    )

    // for writeTranslatedFile()
    data class WriterOptions(
            val parserOptions: ParserOptions,
            val translatedDoc: TranslatedDoc
    )

    /**
     * Extract source strings from the given document content.
     *
     * @param documentUri
     * @param sourceLocale
     * @param params
     * adapter-specific parameter string. See documentation for
     * individual adapters.
     * @return representation of the strings in the document
     * @throws IllegalArgumentException
     * if documentUri or sourceLocale is null
     * @throws FileFormatAdapterException
     * if the document cannot be parsed
     */
    @Throws(FileFormatAdapterException::class, IllegalArgumentException::class)
    fun parseDocumentFile(options: ParserOptions): Resource

    /**
     * Extract translation strings from the given translation document.
     *
     * @param localeId
     * translation locale id
     * @return representation of the translations in the document
     * @throws FileFormatAdapterException
     * if the document cannot be parsed
     * @throws IllegalArgumentException
     * if translatedDocumentContent or localeId is null
     */
    @Throws(FileFormatAdapterException::class, IllegalArgumentException::class)
    fun parseTranslationFile(options: ParserOptions): TranslationsResource

    /**
     * Write translated file to the given output, using the given list of
     * translations.
     *
     * @param output
     * stream to write translated document
     * @throws FileFormatAdapterException
     * if there is any problem parsing the original file or writing
     * the translated file
     * @throws IllegalArgumentException
     * if any parameters are null
     */
    @Throws(FileFormatAdapterException::class, IllegalArgumentException::class)
    fun writeTranslatedFile(output: OutputStream, sourceOptions: ParserOptions,
                            translatedDoc: TranslatedDoc, approvedOnly: Boolean)


    /**
     * Generate translation file with locale and translation extension from DocumentType
     *
     * Source file name will be used if translation extension cannot be found
     * in HRawDocument#DocumentType
     *
     * @param document
     * document to provide name and extension information
     * @param locale
     * to provide for
     */
    @Throws(IllegalArgumentException::class)
    fun generateTranslationFilename(document: HDocument, locale: String): String {
        val srcExt = FilenameUtils.getExtension(document.name)
        val documentType = document.rawDocument.type
        val transExt = documentType.extensions[srcExt]
        return if (StringUtils.isEmpty(transExt)) {
            document.name
        } else FilenameUtils.removeExtension(document
                .name) + "." + transExt
    }

}
