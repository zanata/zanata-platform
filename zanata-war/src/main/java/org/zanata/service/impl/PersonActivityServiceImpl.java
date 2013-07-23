/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.UserActionType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.PersonActivityDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocumentUploadedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.HDocument;
import org.zanata.model.HPerson;
import org.zanata.model.HPersonActivity;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.PersonActivityService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("personActivityServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class PersonActivityServiceImpl implements PersonActivityService
{
   @In
   private PersonActivityDAO personActivityDAO;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;
   
   @In
   private DocumentDAO documentDAO;

   @Override
   public HPersonActivity getPersonActivity(Long personId, Long versionId, UserActionType action)
   {
      return personActivityDAO.findUserActivity(personId, versionId, action);
   }

   @Override
   public List<HPersonActivity> getAllPersonActivities(Long personId, Long versionId, int offset, int count)
   {
      return personActivityDAO.findAllUserActivities(personId, versionId, offset, count);
   }

   @Override
   public void insertOrUpdateActivity(HPerson person, HProjectIteration projectIteration, UserActionType action)
   {
      if(person != null && projectIteration != null && action != null)
      {
         HPersonActivity activity = personActivityDAO.findUserActivity(person.getId(), projectIteration.getId(), action);

         if (activity != null)
         {
            activity.updateLastChanged();
         }
         else
         {
            activity = new HPersonActivity(person, projectIteration, action);
         }
         personActivityDAO.makePersistent(activity);
         personActivityDAO.flush();
      }
   }

   /**
    * This method contains all logic to be run immediately after a Text Flow Target has
    * been successfully translated.
    */
   @Observer(TextFlowTargetStateEvent.EVENT_NAME)
   @Override
   public void textFlowStateUpdated(TextFlowTargetStateEvent event)
   {
      HTextFlowTarget target = textFlowTargetDAO.getById(event.getTextFlowTargetId());
      HDocument document = documentDAO.getById(event.getDocumentId());
      UserActionType actionType = event.getNewState().isReviewed() ? UserActionType.REVIEWED_TRANSLATION : UserActionType.UPDATE_TRANSLATION;

      insertOrUpdateActivity(target.getLastModifiedBy(), document.getProjectIteration(), actionType);
   }

   @Observer(DocumentUploadedEvent.EVENT_NAME)
   @Override
   public void documentUploaded(DocumentUploadedEvent event)
   {
      HDocument document = documentDAO.getById(event.getDocumentId());
      
      UserActionType actionType = event.isSourceDocument() ? UserActionType.UPLOAD_SOURCE_DOCUMENT : UserActionType.UPLOAD_TRANSLATION_DOCUMENT;
      
      insertOrUpdateActivity(document.getLastModifiedBy(), document.getProjectIteration(), actionType);
   }
}
