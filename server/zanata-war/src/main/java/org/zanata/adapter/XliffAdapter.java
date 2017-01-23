/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import org.zanata.adapter.xliff.XliffCommon;
import org.zanata.adapter.xliff.XliffReader;
import org.zanata.adapter.xliff.XliffWriter;
import org.zanata.common.LocaleId;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import com.google.common.base.Optional;
import org.zanata.util.FileUtil;

/**
 * Adapter to read and write {@link org.zanata.common.DocumentType#XLIFF} file
 *
 * TODO: Convert to okapi xliff adapter once all client conversion is migrated
 * to server
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class XliffAdapter implements FileFormatAdapter {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(XliffAdapter.class);

    @Override
    public Resource parseDocumentFile(URI fileUri, LocaleId sourceLocale,
            Optional<String> filterParams)
            throws FileFormatAdapterException, IllegalArgumentException {
        if (sourceLocale == null) {
            throw new IllegalArgumentException("Source locale cannot be null");
        }
        if (fileUri == null) {
            throw new IllegalArgumentException("Document URI cannot be null");
        }
        XliffReader xliffReader = new XliffReader();
        File tempFile = null;
        Resource doc = null;
        try {
            tempFile = new File(fileUri);
            doc = xliffReader.extractTemplate(tempFile, sourceLocale,
                    tempFile.getName(),
                    XliffCommon.ValidationType.CONTENT.name());
        } catch (IOException e) {
            throw new FileFormatAdapterException(
                    "Could not open the URL. The URL is OK but the input stream could not be opened.\n"
                            + e.getMessage(),
                    e);
        }
        return doc;
    }

    @Override
    public TranslationsResource parseTranslationFile(URI fileUri,
            LocaleId sourceLocaleId, String localeId, Optional<String> params)
            throws FileFormatAdapterException, IllegalArgumentException {
        XliffReader xliffReader = new XliffReader();
        TranslationsResource targetDoc = null;
        File transFile = null;
        try {
            transFile = new File(fileUri);
            targetDoc = xliffReader.extractTarget(transFile);
        } catch (FileNotFoundException e) {
            throw new FileFormatAdapterException(
                    "Could not open the URL. The URL is OK but the input stream could not be opened.\n"
                            + e.getMessage(),
                    e);
        } finally {
            FileUtil.tryDeleteFile(transFile);
        }
        return targetDoc;
    }

    @Override
    public void writeTranslatedFile(OutputStream output, URI originalFile,
            Resource resource, TranslationsResource translationsResource,
            String locale, Optional<String> params)
            throws FileFormatAdapterException, IllegalArgumentException {
        // write source string with empty translation
        boolean createSkeletons = true;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("filename", "extension");
            XliffWriter.writeFile(tempFile, resource, locale,
                    translationsResource, createSkeletons);
            FileUtil.writeFileToOutputStream(tempFile, output);
        } catch (IOException e) {
            throw new FileFormatAdapterException(
                    "Unable to generate translated file", e);
        } finally {
            FileUtil.tryDeleteFile(tempFile);
        }
    }
}
