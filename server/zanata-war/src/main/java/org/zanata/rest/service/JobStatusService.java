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

import static com.google.common.collect.Lists.newArrayList;
import static org.zanata.util.ISO8601Util.toInstant;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.rest.dto.JobStatus;
import org.zanata.rest.dto.JobStatus.JobStatusCode;
import org.zanata.rest.dto.JobStatus.JobStatusMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RequestScoped
@Slf4j
@Transactional
// FIXME see also AsynchronousProcessResourceService and ProcessStatus
public class JobStatusService implements JobStatusResource {

    @Inject
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @Override
    public JobStatus getJobStatus(String jobId) {
        AsyncTaskHandle handle =
                asyncTaskHandleManager.getHandleByKey(jobId);

        if (handle == null) {
            throw new NotFoundException("A job was not found for id "
                    + jobId);
        }

        // FIXME check username

        JobStatus status = new JobStatus(jobId);
        status.setStatusCode(handle.isDone() ? JobStatusCode.Finished
                : JobStatusCode.Running);
        int perComplete = 100;
        if (handle.getMaxProgress() > 0) {
            perComplete =
                    (handle.getCurrentProgress() * 100 / handle
                            .getMaxProgress());
        }
        status.setPercentCompleted(perComplete);
        status.setStartTime(toInstant(handle.getStartTime()));
        // FIXME set username, time, current/total items

        if (handle.isDone()) {
            Object result = null;
            try {
                result = handle.getResult();
            } catch (InterruptedException e) {
                log.debug("async task interrupted", e);
                // The process was forcefully cancelled
                status.setStatusCode(JobStatusCode.Failed);
                status.setMessages(newArrayList(
                        new JobStatusMessage(toInstant(handle.getFinishTime()), "ERROR", e.getMessage())));
            } catch (ExecutionException e) {
                log.debug("async task failed", e);
                // Exception thrown while running the task
                status.setStatusCode(JobStatusCode.Failed);
                status.setMessages(newArrayList(
                        new JobStatusMessage(toInstant(handle.getFinishTime()), "ERROR", e.getCause().getMessage())));
            }

            // TODO Need to find a generic way of returning all object types.
            // Since the only current
            // scenario involves lists of strings, hardcoding to that
            if (result != null && result instanceof List) {
                status.getMessages().addAll((List) result);
            }
        }

        return status;
    }


}
