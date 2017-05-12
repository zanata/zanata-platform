/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import org.zanata.model.HRawDocument;

public interface FilePersistService extends Serializable {
    // TODO damason: add persistRawDocumentContentFromStream(HRawDocument,
    // InputStream)

    public void persistRawDocumentContentFromFile(HRawDocument rawDocument,
            File rawFile, String extension);

    void copyAndPersistRawDocument(HRawDocument fromDoc,
            HRawDocument toDoc);

    // TODO damason: parsing code only needs a file URI for this. Change to
    // return
    // uri when files are persisted to server.
    // Other implementations may need a way to specify that they are finished
    // with the
    // document resource and cleanup is possible, in case temp files were
    // generated.
    public InputStream getRawDocumentContentAsStream(HRawDocument document)
            throws RawDocumentContentAccessException;

    boolean hasPersistedDocument(GlobalDocumentId id);

}
