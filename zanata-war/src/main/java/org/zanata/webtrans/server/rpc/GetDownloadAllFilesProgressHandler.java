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

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.process.IterationZipFileBuildProcessHandle;
import org.zanata.service.ProcessManagerService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgress;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgressResult;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
@Name("webtrans.gwt.GetDownloadAllFilesProgressHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetDownloadAllFilesProgress.class)
public class GetDownloadAllFilesProgressHandler extends AbstractActionHandler<GetDownloadAllFilesProgress, GetDownloadAllFilesProgressResult>
{
   @In
   private ProcessManagerService processManagerServiceImpl;

   @Override
   public GetDownloadAllFilesProgressResult execute(GetDownloadAllFilesProgress action, ExecutionContext context) throws ActionException
   {
      int currentProgress = 0;
      int maxProgress = 0;
      String downloadId = "";
      
      IterationZipFileBuildProcessHandle processHandle = (IterationZipFileBuildProcessHandle) processManagerServiceImpl.getProcessHandle(action.getProcessId());
      if (processHandle != null)
      {
         downloadId = processHandle.getDownloadId();
         currentProgress = processHandle.getCurrentProgress();
         maxProgress = processHandle.getMaxProgress();
      }

      return new GetDownloadAllFilesProgressResult(currentProgress, maxProgress, downloadId);
   }

   @Override
   public void rollback(GetDownloadAllFilesProgress action, GetDownloadAllFilesProgressResult result, ExecutionContext context) throws ActionException
   {
   }
}
