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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.zanata.common.ActivityType;
import org.zanata.dao.ActivityDAO;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocumentUploadedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.Activity;
import org.zanata.model.HDocument;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HasEntityType;
import org.zanata.model.type.EntityType;
import org.zanata.service.ActivityService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("activityServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ActivityServiceImpl implements ActivityService
{
   @In
   private ActivityDAO activityDAO;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;

   @In
   private DocumentDAO documentDAO;
   
   @In
   private ProjectIterationDAO projectIterationDAO;
   
   @Override
   public List<Activity> getAllPersonActivities(Long personId, Long contextId, int offset, int count)
   {
      return activityDAO.findAllUserActivities(personId, contextId, offset, count);
   }
   
   @Override
   public Activity findActivity(Long actorId, EntityType contextType, Long contextId, ActivityType action, Date actionTime)
   {
      return activityDAO.findActivity(actorId, contextType, contextId, action, DateUtils.truncate(actionTime, Calendar.HOUR));
   }

   @Override
   public void logActivity(HPerson actor, HasEntityType context, HasEntityType target, ActivityType action, int wordCount)
   {
      if (actor != null && context != null && action != null)
      {
         Date currentActionTime = new Date();
         Activity activity = findActivity(actor.getId(), context.getEntityType(), context.getId(), action, currentActionTime);
         
         if (activity != null)
         {
            activity.updateActivity(currentActionTime, wordCount);
         }
         else
         {
            activity = new Activity(actor, context.getEntityType(), context.getId(), target.getEntityType(), target.getId(), action, wordCount);
         }
         activityDAO.makePersistent(activity);
         activityDAO.flush();
      }
   }
   
   @Override
   public Object getEntity(EntityType entityType, Long entityId) throws ZanataServiceException
   {
      Object result = null;
      
      if(entityType == EntityType.HDocument)
      {
         HDocument document = documentDAO.getById(entityId);
         result = (entityType.getEntityClass().cast(document));
      }
      else if(entityType == EntityType.HProjectIteration)
      {
         HProjectIteration projectVersion = projectIterationDAO.findById(entityId, false);
         result = (entityType.getEntityClass().cast(projectVersion));
      }
      else if(entityType == EntityType.HTexFlowTarget)
      {
         HTextFlowTarget target = textFlowTargetDAO.findById(entityId, false);
         result = (entityType.getEntityClass().cast(target));
      }
      else
      {
         throw new ZanataServiceException("Unsupported entity type");
      }
      return result;
   }
   
   /**
    * This method contains all logic to be run immediately after a Text Flow Target has
    * been successfully translated.
    */
   @Observer(TextFlowTargetStateEvent.EVENT_NAME)
   @Transactional
   public void textFlowStateUpdated(TextFlowTargetStateEvent event)
   {
      HTextFlowTarget target = textFlowTargetDAO.getById(event.getTextFlowTargetId());
      HDocument document = documentDAO.getById(event.getDocumentId());
      ActivityType actionType = event.getNewState().isReviewed() ? ActivityType.REVIEWED_TRANSLATION : ActivityType.UPDATE_TRANSLATION;

      logActivity(target.getLastModifiedBy(), document.getProjectIteration(), target, actionType, target.getTextFlow().getWordCount().intValue());
   }

   /**
    * This method contains all logic to be run immediately after a Source/Translation document has
    * been successfully uploaded.
    */
   @Observer(DocumentUploadedEvent.EVENT_NAME)
   @Transactional
   public void documentUploaded(DocumentUploadedEvent event)
   {
      HDocument document = documentDAO.getById(event.getDocumentId());

      ActivityType actionType = event.isSourceDocument() ? ActivityType.UPLOAD_SOURCE_DOCUMENT : ActivityType.UPLOAD_TRANSLATION_DOCUMENT;

      logActivity(document.getLastModifiedBy(), document.getProjectIteration(), document, actionType, getDocumentWordCount(document));
   }
   
   private int getDocumentWordCount(HDocument document)
   {
      int total = 0;
      
      for(HTextFlow textFlow: document.getTextFlows())
      {
         total += textFlow.getWordCount().intValue();
      }
      return total;
   }
}
