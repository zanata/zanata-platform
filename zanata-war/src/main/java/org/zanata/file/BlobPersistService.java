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

import java.io.InputStream;
import java.sql.Blob;

import org.hibernate.Session;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.model.HDocumentUploadPart;

// TODO damason: replace with file system implementation, named something like "filePartPersistService"
@Named("blobPersistService")
@javax.enterprise.context.Dependent

public class BlobPersistService implements UploadPartPersistService {

    @Inject
    private Session session;

    public HDocumentUploadPart newUploadPartFromStream(
            InputStream partContentStream, int contentLength) {
        HDocumentUploadPart newPart = new HDocumentUploadPart();
        Blob partContent =
                session.getLobHelper().createBlob(partContentStream,
                        contentLength);
        newPart.setContent(partContent);
        return newPart;
    }

}
