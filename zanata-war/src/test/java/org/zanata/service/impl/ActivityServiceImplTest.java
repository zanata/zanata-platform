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
package org.zanata.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ActivityType;
import org.zanata.dao.ActivityDAO;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocumentUploadedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.Activity;
import org.zanata.model.type.EntityType;
import org.zanata.seam.SeamAutowire;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Test(groups = { "business-tests" })
public class ActivityServiceImplTest extends ZanataDbunitJpaTest
{
   private SeamAutowire seam = SeamAutowire.instance();

   private Long personId = new Long(1);
   private Long projectVersionId = new Long(1);
   private Long documentId = new Long(1);
   private Long textFlowTargetId = new Long(1);
   
   private ActivityServiceImpl activityService;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void initializeSeam()
   {
      seam.reset()
            .use("activityDAO", new ActivityDAO(getSession()))
            .use("textFlowTargetDAO", new TextFlowTargetDAO(getSession()))
            .use("documentDAO", new DocumentDAO(getSession()))
            .ignoreNonResolvable();
      
      activityService = seam.autowire(ActivityServiceImpl.class);
   }

   @Test
   public void testNewReviewActivityInserted() throws Exception
   {
      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId, null, new LocaleId("as"), textFlowTargetId,
            ContentState.Approved));
      Activity activity = activityService.findActivity(personId, EntityType.HProjectIteration, projectVersionId, ActivityType.REVIEWED_TRANSLATION, new Date());
      assertThat(activity, not(nullValue()));
      assertThat(activity.getEventCount(), Matchers.equalTo(1));
   }

   @Test
   public void testNewReviewActivityUpdated() throws Exception
   {
      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId, null, new LocaleId("as"), textFlowTargetId,
            ContentState.Approved));
      
      List<Activity> activities = activityService.findLatestActivitiesForContext(personId, projectVersionId, 0, 10);
      assertThat(activities.size(), Matchers.equalTo(1));

      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId, null, new LocaleId("as"), textFlowTargetId,
            ContentState.Rejected));

      activities = activityService.findLatestActivitiesForContext(personId, projectVersionId, 0, 10);
      assertThat(activities.size(), Matchers.equalTo(1));
      
      Activity activity = activityService.findActivity(personId, EntityType.HProjectIteration, projectVersionId, ActivityType.REVIEWED_TRANSLATION, new Date());
      assertThat(activity.getEventCount(), Matchers.equalTo(2));
   }

   @Test
   public void testActivityInsertAndUpdate() throws Exception
   {
      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId, null, new LocaleId("as"), textFlowTargetId,
            ContentState.Translated));
      
      Activity activity = activityService.findActivity(personId, EntityType.HProjectIteration, projectVersionId, ActivityType.UPDATE_TRANSLATION, new Date());
      assertThat(activity, not(nullValue()));
      
      Long id = activity.getId();

      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId, null, new LocaleId("as"), textFlowTargetId,
            ContentState.NeedReview));
      
      activity = activityService.findActivity(personId, EntityType.HProjectIteration, projectVersionId, ActivityType.UPDATE_TRANSLATION, new Date());
      assertThat(activity.getId(), Matchers.equalTo(id));
   }
   
   @Test
   public void testActivityInsertMultipleTypeActivities() throws Exception
   {
      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId, null, new LocaleId("as"), textFlowTargetId,
            ContentState.Translated));

      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId, null, new LocaleId("as"), textFlowTargetId,
            ContentState.Approved));
      
      activityService.onDocumentUploaded(new DocumentUploadedEvent(documentId, false, new LocaleId("as")));
      
      List<Activity> activities = activityService.findLatestActivitiesForContext(personId, projectVersionId, 0, 10);
      assertThat(activities.size(), Matchers.equalTo(3));
   }

   @Test
   public void testGetAllPersonActivities() throws Exception
   {
      Long documentId2 = new Long(2);

      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId2, null, LocaleId.EN_US, new Long(5),
            ContentState.Translated));

      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId2, null, LocaleId.EN_US, new Long(5),
            ContentState.Approved));

      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId2, null, LocaleId.EN_US, new Long(6),
            ContentState.Rejected));

      activityService.logTextFlowStateUpdate(new TextFlowTargetStateEvent(documentId2, null, LocaleId.EN_US, new Long(6),
            ContentState.NeedReview));

      List<Activity> activities = activityService.findLatestActivitiesForContext(documentId2, projectVersionId, 0, 10);

      assertThat(activities.size(), Matchers.equalTo(2));
   }
}
