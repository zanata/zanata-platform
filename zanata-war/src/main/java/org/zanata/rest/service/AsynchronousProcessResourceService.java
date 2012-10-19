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

import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.lock.LockNotAcquiredException;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.process.MessagesProcessHandle;
import org.zanata.process.ProcessHandle;
import org.zanata.process.RunnableProcess;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.LocaleService;
import org.zanata.service.ProcessManagerService;
import org.zanata.service.TranslationService;
import org.zanata.service.impl.DocumentServiceImpl;
import org.zanata.service.impl.TranslationServiceImpl;

import lombok.extern.slf4j.Slf4j;

import static org.zanata.rest.dto.ProcessStatus.ProcessStatusCode;

/**
 * Default server-side implementation of the Asynchronous RunnableProcess Resource.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("asynchronousProcessResourceService")
@Path("/async")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Transactional
@Slf4j
public class AsynchronousProcessResourceService implements AsynchronousProcessResource
{
   @In
   private ProcessManagerService processManagerServiceImpl;

   @In
   private LocaleService localeServiceImpl;

   @In
   private DocumentDAO documentDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private ResourceUtils resourceUtils;

   @In
   private ZanataIdentity identity;


   @Override
   public ProcessStatus startSourceDocCreation(final @PathParam("id") String idNoSlash,
                                               final @PathParam("projectSlug") String projectSlug,
                                               final @PathParam("iterationSlug") String iterationSlug,
                                               final Resource resource,
                                               final @QueryParam("ext") Set<String> extensions,
                                               final @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans)
   {
      ProcessHandle handle = new ProcessHandle();
      String errorMessage = null;

      HProjectIteration hProjectIteration = retrieveAndCheckIteration(projectSlug, iterationSlug, true);

      resourceUtils.validateExtensions(extensions); //gettext, comment

      HDocument document = documentDAO.getByDocIdAndIteration(hProjectIteration, resource.getName());

      // already existing non-obsolete document.
      if (document != null)
      {
         if (!document.isObsolete())
         {
            // updates must happen through PUT on the actual resource
           errorMessage = "A document with name " + resource.getName() + " already exists.";
         }
      }

      if( errorMessage == null )
      {
         RunnableProcess<ProcessHandle> process =
            new RunnableProcess<ProcessHandle>()
            {
               @Override
               protected void run(ProcessHandle handle) throws Throwable
               {
                  DocumentService documentServiceImpl =
                        (DocumentService)Component.getInstance(DocumentServiceImpl.class);
                  documentServiceImpl.saveDocument(
                        projectSlug, iterationSlug, resource, extensions, copytrans, true, handle);
                  handle.setCurrentProgress( handle.getMaxProgress() ); // TODO This should update with real progress
               }
            }.withIdentity(identity);

         processManagerServiceImpl.startProcess(process,handle);

         //response.setStatus(Response.Status.ACCEPTED.getStatusCode());
         return this.getProcessStatus(handle.getId());
      }
      else
      {
         ProcessStatus status = new ProcessStatus();
         status.setStatusCode(ProcessStatusCode.Failed);
         status.getMessages().add(errorMessage);
         return status;
      }
   }

   @Override
   public ProcessStatus startSourceDocCreationOrUpdate(final @PathParam("id") String idNoSlash,
                                                       final @PathParam("projectSlug") String projectSlug,
                                                       final @PathParam("iterationSlug") String iterationSlug,
                                                       final Resource resource,
                                                       final @QueryParam("ext") Set<String> extensions,
                                                       final @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans)
   {
      ProcessHandle handle = new ProcessHandle();
      String errorMessage = null;

      HProjectIteration hProjectIteration = retrieveAndCheckIteration(projectSlug, iterationSlug, true);

      resourceUtils.validateExtensions(extensions); //gettext, comment

      if( errorMessage == null )
      {
         RunnableProcess<ProcessHandle> process =
               new RunnableProcess<ProcessHandle>()
               {
                  @Override
                  protected void prepare(ProcessHandle handle)
                  {
                     handle.setMaxProgress(resource.getTextFlows().size());
                  }

                  @Override
                  protected void run(ProcessHandle handle) throws Throwable
                  {
                     DocumentService documentServiceImpl =
                           (DocumentService)Component.getInstance(DocumentServiceImpl.class);
                     documentServiceImpl.saveDocument(
                           projectSlug, iterationSlug, resource, extensions, copytrans, true, handle);
                     handle.setCurrentProgress( handle.getMaxProgress() ); // TODO This should update with real progress
                  }

                  @Override
                  protected void handleThrowable(ProcessHandle handle, Throwable t)
                  {
                     // Ignore Lock exceptions
                     if( !(t instanceof LockNotAcquiredException) )
                     {
                        AsynchronousProcessResourceService.log.error("Error pushing source document", t);
                     }
                     super.handleThrowable(handle, t);    //To change body of overridden methods use File | Settings | File Templates.
                  }
               }.withIdentity(identity);

         processManagerServiceImpl.startProcess(process,handle);

         //response.setStatus(Response.Status.ACCEPTED.getStatusCode());
         return this.getProcessStatus(handle.getId());
      }
      else
      {
         ProcessStatus status = new ProcessStatus();
         status.setStatusCode(ProcessStatusCode.Failed);
         status.getMessages().add(errorMessage);
         return status;
      }
   }

   @Override
   public ProcessStatus startTranslatedDocCreationOrUpdate(final @PathParam("id") String idNoSlash,
                                                           final @PathParam("projectSlug") String projectSlug,
                                                           final @PathParam("iterationSlug") String iterationSlug,
                                                           final @PathParam("locale") LocaleId locale,
                                                           final TranslationsResource translatedDoc,
                                                           final @QueryParam("ext") Set<String> extensions,
                                                           final @QueryParam("merge") String merge)
   {
      // check security (cannot be on @Restrict as it refers to method parameters)
      identity.checkPermission("modify-translation", this.localeServiceImpl.getByLocaleId(locale),
            this.getSecuredIteration(projectSlug, iterationSlug).getProject());

      String errorMessage = null;
      MessagesProcessHandle handle = new MessagesProcessHandle();

      MergeType mergeType = null;
      try
      {
         mergeType = MergeType.valueOf(merge.toUpperCase());
      }
      catch (Exception e)
      {
         errorMessage = "bad merge type " + merge;
      }

      final String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      final MergeType finalMergeType = mergeType;
      final String userName = identity.getCredentials().getUsername();
      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

      if( errorMessage == null )
      {
         RunnableProcess<MessagesProcessHandle> process =
               new RunnableProcess<MessagesProcessHandle>()
               {
                  @Override
                  protected void run(MessagesProcessHandle handle) throws Throwable
                  {
                     TranslationService translationServiceImpl =
                           (TranslationService)Component.getInstance(TranslationServiceImpl.class);

                     // Translate
                     translationServiceImpl.translateAllInDoc(projectSlug, iterationSlug, id, locale, translatedDoc,
                           extensions, finalMergeType, true, userName, handle);
                  }

                  @Override
                  protected void handleThrowable(MessagesProcessHandle handle, Throwable t)
                  {
                     // Ignore Lock exceptions
                     if( !(t instanceof LockNotAcquiredException) )
                     {
                        AsynchronousProcessResourceService.log.error("Error pushing translations", t);
                     }
                     super.handleThrowable(handle, t);    //To change body of overridden methods use File | Settings | File Templates.
                  }
               }.withIdentity(identity);

         processManagerServiceImpl.startProcess(process,handle);

         return this.getProcessStatus(handle.getId());
      }
      else
      {
         ProcessStatus status = new ProcessStatus();
         status.setStatusCode(ProcessStatusCode.Failed);
         status.getMessages().add(errorMessage);
         return status;
      }
   }

   @Override
   public ProcessStatus getProcessStatus(@PathParam("processId") String processId)
   {
      ProcessHandle handle = processManagerServiceImpl.getProcessHandle(processId);

      if( handle == null )
      {
         throw new NotFoundException("A process was not found for id " + processId);
      }

      ProcessStatus status = new ProcessStatus();
      status.setStatusCode(handle.isInProgress() ? ProcessStatusCode.Running : ProcessStatusCode.Finished);
      int perComplete = 100;
      if(handle.getMaxProgress() > 0)
      {
         perComplete = (handle.getCurrentProgress() / handle.getMaxProgress()) * 100;
      }
      status.setPercentageComplete(perComplete);
      status.setUrl("" + processId);
      if( handle.getError() != null )
      {
         // Lock Exception, tell the client to keep waiting
         if( handle.getError() instanceof LockNotAcquiredException )
         {
            status.getMessages().add("Waiting to acquire lock.");
            status.setStatusCode(ProcessStatusCode.NotAccepted);
         }
         else
         {
            status.setStatusCode(ProcessStatusCode.Failed);
            status.getMessages().add(handle.getError().getMessage());
         }
      }

      if( handle instanceof MessagesProcessHandle )
      {
         MessagesProcessHandle messagesProcessHandle = (MessagesProcessHandle)handle;
         status.setMessages( messagesProcessHandle.getMessages() );
      }

      return status;
   }

   private HProjectIteration retrieveAndCheckIteration(String projectSlug, String iterationSlug, boolean writeOperation)
   {
      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      HProject hProject = hProjectIteration == null ? null : hProjectIteration.getProject();

      if (hProjectIteration == null)
      {
         throw new NoSuchEntityException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.");
      }
      else if (hProjectIteration.getStatus().equals(EntityStatus.OBSOLETE) || hProject.getStatus().equals(EntityStatus.OBSOLETE))
      {
         throw new NoSuchEntityException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.");
      }
      else if (writeOperation)
      {
         if (hProjectIteration.getStatus().equals(EntityStatus.READONLY) || hProject.getStatus().equals(EntityStatus.READONLY))
         {
            throw new ReadOnlyEntityException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' is read-only.");
         }
         else
         {
            return hProjectIteration;
         }
      }
      else
      {
         return hProjectIteration;
      }
   }

   public HProjectIteration getSecuredIteration(String projectSlug, String iterationSlug)
   {
      return retrieveAndCheckIteration(projectSlug, iterationSlug, false);
   }
}
