/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

import java.util.Set;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path(AsynchronousProcessResource.SERVICE_PATH)
public class MockAsynchronousProcessResource implements
        AsynchronousProcessResource {
    @Override
    public ProcessStatus startSourceDocCreation(String idNoSlash,
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, @DefaultValue("true") boolean copytrans) {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public ProcessStatus startSourceDocCreationOrUpdate(String idNoSlash,
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, @DefaultValue("true") boolean copytrans) {
        ProcessStatus processStatus = new ProcessStatus();
        processStatus.setStatusCode(ProcessStatus.ProcessStatusCode.Running);
        processStatus.setPercentageComplete(50);
        processStatus.setUrl("process1");
        return processStatus;
    }

    @Override
    public ProcessStatus startTranslatedDocCreationOrUpdate(String idNoSlash,
            String projectSlug, String iterationSlug, LocaleId locale,
            TranslationsResource translatedDoc, Set<String> extensions,
            String merge, @DefaultValue("false") boolean myTrans) {
        ProcessStatus processStatus = new ProcessStatus();
        processStatus.setStatusCode(ProcessStatus.ProcessStatusCode.Running);
        processStatus.setPercentageComplete(50);
        processStatus.setUrl("process2");
        return processStatus;
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) {
        ProcessStatus processStatus = new ProcessStatus();
        processStatus.setStatusCode(ProcessStatus.ProcessStatusCode.Finished);
        processStatus.setPercentageComplete(100);
        processStatus.setUrl(processId);
        return processStatus;
    }
}

