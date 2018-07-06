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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;
import org.zanata.adapter.xliff.XliffCommon;
import org.zanata.adapter.xliff.XliffReader;
import org.zanata.adapter.xliff.XliffWriter;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
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
    @Override
    public Resource parseDocumentFile(ParserOptions options)
            throws FileFormatAdapterException, IllegalArgumentException {
        XliffReader xliffReader = new XliffReader();
        File tempFile;
        Resource doc;
        try {
            tempFile = new File(options.getRawFile());
            doc = xliffReader.extractTemplate(tempFile, options.getLocale(),
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

    @NotNull
    @Override
    public TranslationsResource parseTranslationFile(
            @NotNull ParserOptions options)
            throws FileFormatAdapterException {
        XliffReader xliffReader = new XliffReader();
        TranslationsResource targetDoc;
        File transFile = null;
        try {
            transFile = new File(options.getRawFile());
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
    public void writeTranslatedFile(@NotNull OutputStream output,
            @NotNull WriterOptions options, boolean approvedOnly)
            throws FileFormatAdapterException, IllegalArgumentException {
        // write source string with empty translation
        boolean createSkeletons = true;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("filename", "extension");
            XliffWriter.writeFile(tempFile, options.getTranslatedDoc().getSource(),
                    options.getTranslatedDoc().getLocale().getId(),
                    options.getTranslatedDoc().getTranslation(), createSkeletons, approvedOnly);
            FileUtil.writeFileToOutputStream(tempFile, output);
        } catch (IOException e) {
            throw new FileFormatAdapterException(
                    "Unable to generate translated file", e);
        } finally {
            FileUtil.tryDeleteFile(tempFile);
        }
    }

    @NotNull
    @Override
    public String generateTranslationFilename(@NotNull HDocument document,
            @NotNull String locale) throws IllegalArgumentException {
        return FileFormatAdapter.DefaultImpls.generateTranslationFilename(this, document, locale);
    }

}
