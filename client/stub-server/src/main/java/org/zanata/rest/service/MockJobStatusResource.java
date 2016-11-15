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

import org.zanata.rest.dto.JobStatus;

import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class MockJobStatusResource implements JobStatusResource {
    @Override
    public Response getJobStatus(long jobId) {
        String time = ZonedDateTime.now(ZoneId.systemDefault()).format(
                DateTimeFormatter.ISO_INSTANT);
        JobStatus jobStatus = new JobStatus(1L,
                "Job complete for jobId: " + jobId);
        jobStatus.setStartTime(time);
        jobStatus.setStatusTime(time);
        jobStatus.setPercentCompleted(100);
        jobStatus.getMessages().add(new JobStatus.JobStatusMessage(
                time, "INFO", "job complete"));
        return Response.status(Response.Status.ACCEPTED).entity(jobStatus).build();
    }
}
