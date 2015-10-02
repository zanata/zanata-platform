/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.action.CopyTransManager;
import org.zanata.async.handle.CopyTransTaskHandle;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.security.ZanataIdentity;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("copyTransResourceService")
@Path(CopyTransResource.SERVICE_PATH)
public class CopyTransResourceService implements CopyTransResource {
    @Inject
    private ZanataIdentity identity;

    @Inject
    private CopyTransManager copyTransManager;

    @Inject
    private DocumentDAO documentDAO;

    @Override
    public CopyTransStatus startCopyTrans(String projectSlug,
            String iterationSlug, String docId) {
        HDocument document =
                documentDAO.getByProjectIterationAndDocId(projectSlug,
                        iterationSlug, docId);
        if (document == null) {
            throw new NoSuchEntityException("Could not find document: "
                    + projectSlug + "/" + iterationSlug + "/" + docId);
        }

        // NB: Permission check happens in the Copy Trans service itself.

        copyTransManager.startCopyTrans(document, null); // TODO allow options
                                                         // from the Rest
                                                         // endpoint
        return this.getCopyTransStatus(projectSlug, iterationSlug, docId);
    }

    @Override
    public CopyTransStatus getCopyTransStatus(String projectSlug,
            String iterationSlug, String docId) {

        HDocument document =
                documentDAO.getByProjectIterationAndDocId(projectSlug,
                        iterationSlug, docId);
        if (document == null) {
            throw new NoSuchEntityException("Could not find document: "
                    + projectSlug + "/" + iterationSlug + "/" + docId);
        }

        identity.checkPermission("copy-trans", document.getProjectIteration());

        CopyTransTaskHandle processHandle =
                copyTransManager.getCopyTransProcessHandle(document);

        if (processHandle == null) {
            throw new NoSuchEntityException(
                    "There are no current or recently finished Copy Trans processes for this document.");
        }

        CopyTransStatus status = new CopyTransStatus();
        status.setInProgress(!processHandle.isDone());
        float percent =
                ((float) processHandle.getCurrentProgress() / processHandle
                        .getMaxProgress()) * 100;
        status.setPercentageComplete((int) percent);
        return status;
    }
}
