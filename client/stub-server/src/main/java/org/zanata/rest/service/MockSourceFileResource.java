/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.service;

import static org.zanata.rest.service.MockFileResource.sampleResource;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ProjectType;
import org.zanata.rest.dto.JobStatus;
import org.zanata.rest.dto.JobStatus.JobStatusCode;
import org.zanata.rest.dto.resource.Resource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class MockSourceFileResource implements SourceFileResource {

    @Override
    public Response uploadSourceFile(String projectSlug, String versionSlug,
            String docId, InputStream fileStream, long size, ProjectType projectType) {
        try {
            long actual = IOUtils
                    .copyLarge(fileStream, new NullOutputStream());
            Instant time = Instant.now();
            JobStatus jobStatus = new JobStatus("1");
            jobStatus.setStatusCode(JobStatusCode.Waiting);
            jobStatus.setCurrentItem(0);
            jobStatus.setTotalItems(100);
            jobStatus.setEstimatedCompletionTime(time.plusSeconds(1));
            jobStatus.getMessages().add("upload has been scheduled");
            jobStatus.getMessages().add(
                    "Uploading new source document (" +
                            actual + "/" + size + " bytes): " +
                            projectType + ":" + projectSlug + "/" +
                            versionSlug + "/" + docId);
            return Response.status(Response.Status.ACCEPTED).entity(jobStatus).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response downloadSourceFile(String projectSlug, String versionSlug,
            final String docId, String projectType) {
        StreamingOutput output = output1 -> {
            PoWriter2 writer = new PoWriter2(false, false);
            Resource doc = sampleResource(docId);
            writer.writePot(output1, "UTF-8", doc);
        };
        return Response
                .ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + docId
                                + ".pot\"").type(MediaType.TEXT_PLAIN)
                .entity(output).build();
    }

}

