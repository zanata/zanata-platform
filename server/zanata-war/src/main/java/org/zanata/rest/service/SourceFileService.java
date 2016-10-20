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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.util.GenericType;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ContentState;
import org.zanata.common.DocumentType;
import org.zanata.common.FileTypeInfo;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.RawDocumentContentAccessException;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.file.TranslationDocumentUpload;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.service.FileSystemService;
import org.zanata.service.FileSystemService.DownloadDescriptorProperties;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

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
    private FileService fileService;

    @Override
    Response uploadSourceFile(
            String projectSlug,
            String iterationSlug,
            String docId,
            DocumentFileUploadForm uploadForm,
            String projectType) {
        String actualDocId = fileService.getDocId(projectSlug, iterationSlug, docId, projectType);
        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, actualDocId);
        return sourceUploader.tryUploadSourceFile(id, uploadForm);

    }


    @Override
    Response downloadSourceFile(
            String projectSlug,
            String iterationSlug,
            String fileType,
            String docId,
            String projectType) {

    }
}
