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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.file.GlobalDocumentId;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.util.ServiceLocator;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import javax.inject.Inject;

import static org.zanata.adapter.AdapterUtils.readStream;

/**
 * Adapter to read and write {@link org.zanata.common.DocumentType#GETTEXT}
 *
 * TODO: Convert to okapi gettext adapter once all client conversion is migrated
 * to server
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GettextAdapter implements FileFormatAdapter {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GettextAdapter.class);

    @Inject
    private DocumentDAO documentDAO;

    @Override
    public Resource parseDocumentFile(URI fileUri, LocaleId sourceLocale,
            Optional<String> params)
            throws FileFormatAdapterException, IllegalArgumentException {
        if (sourceLocale == null) {
            throw new IllegalArgumentException("Source locale cannot be null");
        }
        if (fileUri == null) {
            throw new IllegalArgumentException("Document URI cannot be null");
        }
        PoReader2 reader = new PoReader2();
        BufferedInputStream inputStream = readStream(fileUri);
        Resource doc = reader.extractTemplate(new InputSource(inputStream),
                sourceLocale, "");
        try {
            inputStream.close();
        } catch (IOException e) {
        }
        return doc;
    }

    @Override
    public TranslationsResource parseTranslationFile(URI fileUri,
            LocaleId sourceLocaleId, String localeId, Optional<String> params)
            throws FileFormatAdapterException, IllegalArgumentException {
        if (StringUtils.isEmpty(localeId)) {
            throw new IllegalArgumentException(
                    "locale id string cannot be null or empty");
        }
        PoReader2 reader = new PoReader2();
        BufferedInputStream inputStream = readStream(fileUri);
        TranslationsResource resource =
                reader.extractTarget(new InputSource(inputStream));
        try {
            inputStream.close();
        } catch (IOException e) {
        }
        return resource;
    }

    @Override
    public void writeTranslatedFile(OutputStream output, URI originalFile,
            Resource resource, TranslationsResource translationsResource,
            String locale, Optional<String> params)
            throws FileFormatAdapterException, IllegalArgumentException {
        PoWriter2 writer = new PoWriter2(true, true);
        try {
            writer.writePo(output, Charsets.UTF_8.name(), resource,
                    translationsResource);
        } catch (IOException e) {
            throw new FileFormatAdapterException(
                    "Unable to generate translated file", e);
        }
    }

    private HDocument getDocument(GlobalDocumentId documentId) {
        return documentDAO.getByGlobalId(documentId);
    }

    private DocumentDAO getDocumentDAO() {
        return ServiceLocator.instance().getInstance(DocumentDAO.class);
    }

    private ResourceUtils getResourceUtils() {
        return ServiceLocator.instance().getInstance(ResourceUtils.class);
    }
}
