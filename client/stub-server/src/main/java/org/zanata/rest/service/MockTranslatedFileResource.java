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
import static org.zanata.rest.service.MockFileResource.sampleTransResource;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.rest.dto.JobStatus;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class MockTranslatedFileResource implements TranslatedFileResource {

    @Override
    public Response uploadTranslationFile(String projectSlug,
            String iterationSlug, String localeId, String docId, String merge,
            InputStream fileStream, long size, String projectType) {
        try {
            long actual = IOUtils.copyLarge(fileStream, new NullOutputStream());
            String time = ZonedDateTime.now(ZoneId.systemDefault()).format(
                    DateTimeFormatter.ISO_INSTANT);
            JobStatus jobStatus = new JobStatus(1L,
                    "Upload of translation document successful (" +
                            actual + "/" + size + " bytes): " +
                            projectType + ":" + projectSlug + "/" +
                            iterationSlug + "/" + docId + ":" + localeId);
            jobStatus.setStartTime(time);
            jobStatus.setStatusTime(time);
            jobStatus.getMessages().add(new JobStatus.JobStatusMessage(
                    time, "INFO", "upload has been scheduled"));
            return Response.status(Response.Status.ACCEPTED).entity(jobStatus).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response downloadTranslationFile(String projectSlug,
            String versionSlug, String locale,
            final String docId, String projectType) {
        StreamingOutput output = output1 -> {
            PoWriter2 writer = new PoWriter2(false, false);
            writer.writePo(output1, "UTF-8", sampleResource(docId),
                    sampleTransResource());
        };
        return Response.ok()
                .header("Content-Disposition",
                        "attachment; filename=\""
                                + docId + ".po\"").type(MediaType.TEXT_PLAIN)
                .entity(output).build();
    }

}

