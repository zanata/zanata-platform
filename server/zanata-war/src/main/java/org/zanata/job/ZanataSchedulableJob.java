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

import java.util.Date;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

/**
 * Base class for all Zanata jobs. Provides minor set pieces for jobs to be ran in 
 * the background.
 * 
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class ZanataSchedulableJob
{

   @Logger
   private Log log;
   
   public String getName()
   {
      return this.getClass().getName();
   }
   
   @Asynchronous
   public QuartzTriggerHandle startJob( @IntervalCron String cron )
   {
      final Date startTime = new Date();
      log.info("Running Job {0} ({1})", this.getName(), startTime);
      
      try
      {
         this.execute();
      }
      catch (Exception e)
      {
         log.error("Unexpected error while running Job {0}", e, this.getName());
      }
      
      final Date endTime = new Date();
      final long durationInSecs = ( startTime.getTime() - endTime.getTime() ) / 1000;
      log.info("Finished running Job {0} ({1}). Total Duration {2} seconds", this.getName(), new Date(), durationInSecs);
      
      return null;
   }
   
   protected abstract void execute() throws Exception;
   
}
