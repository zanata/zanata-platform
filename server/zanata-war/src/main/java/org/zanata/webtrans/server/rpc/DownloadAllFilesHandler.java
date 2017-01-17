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
package org.zanata.webtrans.server.rpc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationArchiveService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesAction;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesResult;

import java.io.Serializable;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Named("webtrans.gwt.DownloadAllFilesHandler")
@RequestScoped
@ActionHandlerFor(DownloadAllFilesAction.class)
public class DownloadAllFilesHandler extends
        AbstractActionHandler<DownloadAllFilesAction, DownloadAllFilesResult>
        implements Serializable {

    @Inject
    private ZanataIdentity identity;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private TranslationArchiveService translationArchiveServiceImpl;

    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "CDI proxies are Serializable")
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @Override
    public DownloadAllFilesResult execute(DownloadAllFilesAction action,
            ExecutionContext context) throws ActionException {
        HProjectIteration version =
                projectIterationDAO.getBySlug(action.getProjectSlug(),
                        action.getVersionSlug());
        if (identity.hasPermission(version, "download-all")) {
            AsyncTaskHandle<String> handle = new AsyncTaskHandle<String>();
            Serializable taskKey =
                    asyncTaskHandleManager.registerTaskHandle(handle);
            // TODO This should be in a service and share code with the JSF
            // pages that do the same thing
            try {
                translationArchiveServiceImpl.startBuildingTranslationFileArchive(
                        action.getProjectSlug(), action.getVersionSlug(),
                        action.getLocaleId(), ZanataIdentity.instance().getCredentials()
                                .getUsername(), handle);
            } catch (Exception e) {
                throw new ActionException(e);
            }

            // NB Keys are currently strings, but this is tied to the
            // implementation
            return new DownloadAllFilesResult(true, taskKey.toString());
        }

        return new DownloadAllFilesResult(false, null);

    }

    @Override
    public void rollback(DownloadAllFilesAction action,
            DownloadAllFilesResult result, ExecutionContext context)
            throws ActionException {
    }

}
