/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.async.tasks;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.security.Identity;
import org.zanata.async.AsyncTask;
import org.zanata.async.TimedAsyncHandle;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.CopyTransServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * Asynchronous Task that runs copy trans.
 * This task can run against a document or against a project iteration depending
 * on which constructor is used.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CopyTransTask implements AsyncTask<Void, CopyTransTask.CopyTransTaskHandle>
{
   private HDocument document;

   private HProjectIteration projectIteration;

   private HCopyTransOptions copyTransOptions;

   private final CopyTransTaskHandle handle = new CopyTransTaskHandle();

   public CopyTransTask(HDocument document, HCopyTransOptions copyTransOptions)
   {
      this.document = document;
      this.copyTransOptions = copyTransOptions;
   }

   public CopyTransTask(HProjectIteration projectIteration, HCopyTransOptions copyTransOptions)
   {
      this.projectIteration = projectIteration;
      this.copyTransOptions = copyTransOptions;
   }

   @Override
   public CopyTransTaskHandle getHandle()
   {
      return handle;
   }

   @Override
   public Void call() throws Exception
   {
      getHandle().startTiming();
      getHandle().setTriggeredBy(Identity.instance().getPrincipal().getName());
      calculateMaxProgress();

      CopyTransService copyTransServiceImpl =
            (CopyTransService) Component.getInstance(CopyTransServiceImpl.class);

      if( projectIteration != null )
      {
         copyTransServiceImpl.copyTransForIteration( projectIteration, copyTransOptions );
      }
      else
      {
         copyTransServiceImpl.copyTransForDocument( document );
      }

      getHandle().finishTiming();
      return null;
   }

   private void calculateMaxProgress()
   {
      LocaleService localeService = (LocaleService)Component.getInstance(LocaleServiceImpl.class);
      if( projectIteration != null )
      {
         List<HLocale> localeList =
               localeService.getSupportedLangugeByProjectIteration(projectIteration.getProject().getSlug(),
                     projectIteration.getSlug());

         getHandle().setMaxProgress( projectIteration.getDocuments().size() * localeList.size() );
      }
      else
      {
         // Set the max progress only if it hasn't been set yet
         List<HLocale> localeList =
               localeService.getSupportedLangugeByProjectIteration(document.getProjectIteration().getProject().getSlug(),
                     document.getProjectIteration().getSlug());

         getHandle().setMaxProgress(localeList.size());
      }

   }

   public static class CopyTransTaskHandle extends TimedAsyncHandle<Void>
   {
      @Getter
      @Setter
      private int documentsProcessed;

      @Getter
      @Setter
      private String cancelledBy;

      @Getter
      @Setter
      private long cancelledTime;

      @Getter
      @Setter
      private String triggeredBy;

   }
}
