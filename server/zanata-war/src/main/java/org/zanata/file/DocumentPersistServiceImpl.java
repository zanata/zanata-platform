/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.file;

import java.io.InputStream;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HRawDocument;

import static org.zanata.util.JavaslangNext.TODO;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class DocumentPersistServiceImpl implements DocumentPersistService {
    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(DocumentPersistServiceImpl.class);

    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private FilePersistService filePersistService;

    /**
     * Unless the result is null (no raw document found), the caller is
     * responsible for closing the InputStream.
     *
     * @param id
     *            the document ID
     * @return base filename, file size, input stream (unbuffered), or null if
     *         there is no document, or no raw file
     */
    @Override
    @Nullable
    public PersistedDocumentInfo
            getSourceDocumentForStreaming(GlobalDocumentId id) {
        HDocument hDocument = documentDAO.getByGlobalId(id);
        if (hDocument == null) return null;
        HRawDocument rawDocument = hDocument.getRawDocument();
        if (rawDocument != null) {
            if (!filePersistService.hasPersistedDocument(id)) {
                log.error("Can't find file with ID {} for Document {}",
                        rawDocument.getFileId(), hDocument.getId());
                throw new RuntimeException("Missing file for raw document");
            }
            return new PersistedDocumentInfo(hDocument.getName(),
                    filePersistService.getRawDocumentSize(rawDocument),
                    filePersistService
                            .getRawDocumentContentAsStream(rawDocument));
        } else {
            // convert text flows to the appropriate file type

            // if gettext: ...
            // TODO see POTStreamingOutput

            // if properties/utf8, xliff: ...
            // TODO FormatAdapterStreamingOutput

            return TODO();
        }
    }
}
