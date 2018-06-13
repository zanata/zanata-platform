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

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.zanata.adapter.properties.PropReader;
import org.zanata.adapter.properties.PropWriter;
import static org.zanata.adapter.AdapterUtils.readStream;
import org.zanata.common.ContentState;
import org.zanata.common.DocumentType;
import org.zanata.common.LocaleId;
import org.zanata.common.dto.TranslatedDoc;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.FileUtil;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Properties file adapter to read and write.
 *
 * See {@link org.zanata.adapter.PropertiesLatinOneAdapter}
 * {@link org.zanata.adapter.PropertiesUTF8Adapter}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GenericPropertiesAdapter implements FileFormatAdapter {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GenericPropertiesAdapter.class);
    public static final String ISO_8859_1 = Charsets.ISO_8859_1.name();
    public static final String UTF_8 = Charsets.UTF_8.name();
    private final PropWriter.CHARSET charset;

    public GenericPropertiesAdapter(String charset) {
        this.charset = toPropWriterCharset(charset);
    }

    private PropWriter.CHARSET toPropWriterCharset(String charset) {
        if (charset.equals(ISO_8859_1)) {
            return PropWriter.CHARSET.Latin1;
        } else if (charset.equals(UTF_8)) {
            return PropWriter.CHARSET.UTF8;
        } else {
            throw new FileFormatAdapterException("Not supported charset \'"
                    + charset + "\' for properties file");
        }
    }

    @Override
    public Resource parseDocumentFile(ParserOptions options)
            throws FileFormatAdapterException, IllegalArgumentException {
        PropReader propReader =
                new PropReader(charset, options.getLocale(), ContentState.Approved);
        Resource doc = new Resource();
        try (BufferedInputStream inputStream = readStream(options.getRawFile())) {
            propReader.extractTemplate(doc, inputStream);
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
            throws FileFormatAdapterException, IllegalArgumentException {
        PropReader propReader =
                new PropReader(charset, null, ContentState.Approved);
        Resource srcDoc = new Resource();
        TranslationsResource targetDoc = new TranslationsResource();
        try (BufferedInputStream inputStream = readStream(options.getRawFile())) {
            propReader.extractTarget(targetDoc, inputStream, srcDoc);
        } catch (IOException e) {
            throw new FileFormatAdapterException(
                    "Could not open the URL. The URL is OK but the input stream could not be opened.\n"
                            + e.getMessage(),
                    e);
        }
        return targetDoc;
    }

    /**
     * Resource is not needed for properties file translation parser as its only
     * used for contentHash check
     */
    @Override
    public void writeTranslatedFile(@NotNull OutputStream output,
            @NotNull WriterOptions options, boolean approvedOnly)
            throws FileFormatAdapterException, IllegalArgumentException {
        // write source string with empty translation
        boolean createSkeletons = true;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("filename", ".properties");
            PropWriter.writeTranslationsFile(options.getTranslatedDoc(),
                    tempFile, charset, createSkeletons, approvedOnly);
            FileUtil.writeFileToOutputStream(tempFile, output);
        } catch (IOException e) {
            throw new FileFormatAdapterException(
                    "Unable to generate translated file", e);
        } finally {
            FileUtil.tryDeleteFile(tempFile);
        }
    }

    @Override @Nonnull
    public String generateTranslationFilename(@Nonnull HDocument document,
                                              @Nonnull String locale) {
        String srcExt = FilenameUtils.getExtension(document.getName());
        DocumentType documentType = document.getRawDocument().getType();
        String transExt = documentType.getExtensions().get(srcExt);
        LocaleId localeId = new LocaleId(locale);
        if (StringUtils.isEmpty(transExt)) {
            log.warn("Adding missing .properties extension to generated filename");
            return document.getName() + "_" +
                    localeId.toJavaName() + ".properties";
        }
        return FilenameUtils.removeExtension(document.getName()) + "_"
                + localeId.toJavaName()
                + "." + transExt;
    }

}
