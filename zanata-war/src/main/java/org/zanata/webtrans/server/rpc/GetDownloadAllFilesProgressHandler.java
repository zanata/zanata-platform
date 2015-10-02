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

import java.util.concurrent.ExecutionException;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgress;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgressResult;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Named("webtrans.gwt.GetDownloadAllFilesProgressHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(GetDownloadAllFilesProgress.class)
public class GetDownloadAllFilesProgressHandler
        extends
        AbstractActionHandler<GetDownloadAllFilesProgress, GetDownloadAllFilesProgressResult> {
    @Inject
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @Override
    public GetDownloadAllFilesProgressResult execute(
            GetDownloadAllFilesProgress action, ExecutionContext context)
            throws ActionException {
        int currentProgress = 0;
        int maxProgress = 0;
        String downloadId = "";

        AsyncTaskHandle<String> handle =
                asyncTaskHandleManager.getHandleByKey(action.getProcessId());
        if (handle != null) {
            if (handle.isDone()) {
                try {
                    downloadId = handle.getResult();
                } catch (InterruptedException e) {
                    throw new ActionException(
                            "Zip file preparation was interrupted", e);
                } catch (ExecutionException e) {
                    throw new ActionException(
                            "Error preparing zip file for download", e);
                }
            }
            currentProgress = handle.getCurrentProgress();
            maxProgress = handle.getMaxProgress();
        }

        return new GetDownloadAllFilesProgressResult(currentProgress,
                maxProgress, downloadId);
    }

    @Override
    public void rollback(GetDownloadAllFilesProgress action,
            GetDownloadAllFilesProgressResult result, ExecutionContext context)
            throws ActionException {
    }
}
