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
package org.zanata.job;

import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.async.QuartzTriggerHandle;

/**
 * Responsible for scheduling all Zanata application level background jobs. 
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("zanataJobScheduler")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Startup
@Install(false)
public class ZanataJobScheduler
{

   @In(create=true)
   private DownloadFileCleanupJob downloadFileCleanupJob;
   
   private Set<QuartzTriggerHandle> triggerHandles = new HashSet<QuartzTriggerHandle>();
   
   @Create
   public void scheduleJobs()
   {
      this.triggerHandles.add( this.downloadFileCleanupJob.startJob("0 0 0 * * ?") );
   }
   
   @Destroy
   public void unscheduleAllJobs() throws Exception
   {
      for( QuartzTriggerHandle handle : this.triggerHandles )
      {
         handle.cancel();
      }
   }
}
