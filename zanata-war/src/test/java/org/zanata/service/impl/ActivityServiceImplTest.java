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
package org.zanata.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import com.google.common.base.Throwables;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.cdi.TestTransaction;
import org.zanata.common.ActivityType;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.events.DocumentUploadedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.Activity;
import org.zanata.model.type.EntityType;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.IServiceLocator;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
public class ActivityServiceImplTest extends ZanataDbunitJpaTest {

    private Long personId = new Long(1L);
    private Long versionId = new Long(1L);
    private Long projectVersionId = new Long(1L);
    private Long documentId = new Long(1L);
    private Long textFlowTargetId = new Long(1L);

    @Inject
    private ActivityServiceImpl activityService;

    @Produces @Mock IServiceLocator serviceLocator;

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void initializeSeam() {
        try {
            when(serviceLocator.getJndiComponent("java:jboss/UserTransaction",
                    UserTransaction.class)).thenReturn(new TestTransaction(getEm()));
        } catch (NamingException e) {
            // this should not happen
            Throwables.propagate(e);
        }
    }

    @Test
    @InRequestScope
    public void testNewReviewActivityInserted() throws Exception {
        TextFlowTargetStateEvent event =
            buildEvent(personId, versionId, documentId, null,
                new LocaleId("as"), textFlowTargetId, ContentState.Approved,
                ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event);
        Activity activity =
                activityService.findActivity(personId,
                        EntityType.HProjectIteration, projectVersionId,
                        ActivityType.REVIEWED_TRANSLATION, new Date());
        assertThat(activity, not(nullValue()));
        assertThat(activity.getEventCount(), equalTo(1));
    }

    @Test
    @InRequestScope
    public void testNewReviewActivityUpdated() throws Exception {
        TextFlowTargetStateEvent event =
            buildEvent(personId, versionId, documentId, null,
                new LocaleId("as"),
                textFlowTargetId, ContentState.Approved,
                ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event);
        List<Activity> activities =
                activityService.findLatestActivitiesForContext(personId,
                        projectVersionId, 0, 10);
        assertThat(activities.size(), equalTo(1));

        TextFlowTargetStateEvent event2 =
            buildEvent(personId, versionId, documentId, null,
                new LocaleId("as"),
                textFlowTargetId, ContentState.Rejected,
                ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event2);

        activities =
                activityService.findLatestActivitiesForContext(personId,
                        projectVersionId, 0, 10);
        assertThat(activities.size(), equalTo(1));

        Activity activity =
                activityService.findActivity(personId,
                        EntityType.HProjectIteration, projectVersionId,
                        ActivityType.REVIEWED_TRANSLATION, new Date());
        assertThat(activity.getEventCount(), equalTo(2));
    }

    @Test
    @InRequestScope
    public void testActivityInsertAndUpdate() throws Exception {
        TextFlowTargetStateEvent event =
            buildEvent(personId, versionId, documentId, null,
                new LocaleId("as"),
                textFlowTargetId, ContentState.Translated,
                ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event);

        Activity activity =
                activityService.findActivity(personId,
                        EntityType.HProjectIteration, projectVersionId,
                        ActivityType.UPDATE_TRANSLATION, new Date());
        assertThat(activity, not(nullValue()));

        Long id = activity.getId();

        TextFlowTargetStateEvent event2 =
            buildEvent(personId, versionId, documentId, null,
                new LocaleId("as"), textFlowTargetId, ContentState.NeedReview,
                ContentState.New);
        activityService.logTextFlowStateUpdate(event2);
        activity =
                activityService.findActivity(personId,
                        EntityType.HProjectIteration, projectVersionId,
                        ActivityType.UPDATE_TRANSLATION, new Date());
        assertThat(activity.getId(), equalTo(id));
    }

    @Test
    @InRequestScope
    public void testActivityInsertMultipleTypeActivities() throws Exception {
        TextFlowTargetStateEvent event =
            buildEvent(personId, versionId, documentId, null,
                new LocaleId("as"),
                textFlowTargetId, ContentState.Translated,
                ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event);

        TextFlowTargetStateEvent event2 =
            buildEvent(personId, versionId, documentId, null,
                new LocaleId("as"),
                textFlowTargetId, ContentState.Approved,
                ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event2);

        activityService.onDocumentUploaded(new DocumentUploadedEvent(personId,
                documentId, false, new LocaleId("as")));

        List<Activity> activities =
                activityService.findLatestActivitiesForContext(personId,
                        projectVersionId, 0, 5);
        assertThat(activities.size(), equalTo(3));
    }

    @Test
    @InRequestScope
    public void testGetAllPersonActivities() throws Exception {
        Long documentId2 = new Long(2L);

        TextFlowTargetStateEvent event =
            buildEvent(personId, versionId, documentId2, null, LocaleId.EN_US,
                new Long(5), ContentState.Translated, ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event);

        TextFlowTargetStateEvent event2 =
            buildEvent(personId, versionId, documentId2, null, LocaleId.EN_US,
                new Long(5), ContentState.Approved, ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event2);

        TextFlowTargetStateEvent event3 =
            buildEvent(personId, versionId, documentId2, null, LocaleId.EN_US,
                new Long(5), ContentState.Approved, ContentState.NeedReview);
        activityService.logTextFlowStateUpdate(event3);

        TextFlowTargetStateEvent event4 =
            buildEvent(personId, versionId, documentId2, null, LocaleId.EN_US,
                new Long(6), ContentState.Rejected, ContentState.Translated);
        activityService.logTextFlowStateUpdate(event4);

        TextFlowTargetStateEvent event5 =
            buildEvent(personId, versionId, documentId2, null, LocaleId.EN_US,
                new Long(6), ContentState.NeedReview, ContentState.New);
        activityService.logTextFlowStateUpdate(event5);

        List<Activity> activities =
            activityService.findLatestActivitiesForContext(personId,
                projectVersionId, 0, 10);

        assertThat(activities.size(), equalTo(2));
    }

    private TextFlowTargetStateEvent buildEvent(Long personId, Long versionId,
        Long documentId, Long tfIf, LocaleId localeId,
        Long tftId, ContentState newState, ContentState oldState) {

        DocumentLocaleKey key = new DocumentLocaleKey(documentId, localeId);

        TextFlowTargetStateEvent.TextFlowTargetState state =
            new TextFlowTargetStateEvent.TextFlowTargetState(tfIf,
                tftId, newState, oldState);

        return new TextFlowTargetStateEvent(key, versionId, personId, state);
    }
}
