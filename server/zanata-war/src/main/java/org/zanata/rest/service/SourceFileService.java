/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.service;

import java.io.InputStream;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RequestScoped
@Slf4j
@Transactional
public class SourceFileService implements SourceFileResource {

    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private FilePersistService filePersistService;
    @Inject
    private LegacyFileMapper legacyFileMapper;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private SourceDocumentUpload sourceUploader;

    @Override
    public Response uploadSourceFile(
            String projectSlug,
            String iterationSlug,
            String docId,
            InputStream fileStream,
            long size,
            @Nullable ProjectType projectType) {
        String suffix = legacyFileMapper.getFilenameSuffix(projectSlug, iterationSlug, projectType, true);
        String actualDocId = StringUtils.removeEnd(docId, suffix);
//        String actualDocId = legacyFileMapper.getServerDocId(projectSlug, iterationSlug, docId, projectType);
        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, actualDocId);
        // FIXME
        return null;
//        return sourceUploader.tryUploadSourceFile(id, fileStream);
    }


    @Override
    public Response downloadSourceFile(
            String projectSlug,
            String iterationSlug,
            String fileType,
            String docId,
            String projectType) {
        // FIXME
        return null;
    }

}
