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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.persistence.EntityManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.time.DateUtils;
import javax.inject.Inject;
import javax.inject.Named;

import org.zanata.action.DashboardUserStats;
import org.zanata.async.Async;
import org.zanata.common.ActivityType;
import org.zanata.dao.ActivityDAO;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocumentUploadedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.Activity;
import org.zanata.model.HDocument;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.IsEntityWithType;
import org.zanata.model.type.EntityType;
import org.zanata.service.ActivityService;
import org.zanata.transaction.TransactionUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("activityServiceImpl")
@RequestScoped
public class ActivityServiceImpl implements ActivityService {
    private static final long serialVersionUID = -6696241684824218697L;
    @Inject
    private ActivityDAO activityDAO;

    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;

    @Inject
    private DocumentDAO documentDAO;

    @Inject
    private PersonDAO personDAO;

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Inject
    private EntityManager entityManager;

    @Inject
    private ActivityLockManager activityLockManager;

    @Inject
    private TransactionUtil transactionUtil;

    @Override
    public Activity findActivity(long actorId, EntityType contextType,
            long contextId, ActivityType activityType, Date actionTime) {
        return activityDAO.findActivity(actorId, contextType, contextId,
                activityType, getRoundedTime(actionTime));
    }

    private Date getRoundedTime(Date actionTime) {
        return DateUtils.truncate(actionTime, Calendar.HOUR);
    }

    @Override
    public List<Activity> findLatestVersionActivitiesByUser(long personId,
            List<Long> versionIds, int offset, int maxResults) {
        if (versionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return activityDAO.findLatestVersionActivitiesByUser(personId,
                versionIds, offset, maxResults);
    }

    @Override
    public List<Activity> findLatestVersionActivities(Long versionId,
            int offset, int maxResults) {

        return activityDAO.findLatestVersionActivities(versionId, offset,
                maxResults);
    }

    @Override
    public List<Activity> findLatestActivitiesForContext(long personId,
            long contextId, int offset, int maxResults) {
        return activityDAO.findLatestActivitiesForContext(personId, contextId,
                offset, maxResults);
    }

    @Override
    public List<Activity> findLatestActivities(long personId, int offset,
            int maxResults) {
        return activityDAO.findLatestActivities(personId, offset, maxResults);
    }

    @Override
    public void logActivity(long actorId, IsEntityWithType context,
            IsEntityWithType target, ActivityType activityType, int wordCount) {
        Lock lock = activityLockManager.getLock(actorId);
        lock.lock();
        try {
            // TODO wrap this in a transaction (between lock/unlock), or
            // perhaps remove this method
            logActivityAlreadyLocked(actorId, context, target, activityType,
                    wordCount);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Precondition: current thread must hold a lock for the person 'actorId'.
     *
     * @param actorId
     * @param context
     * @param target
     * @param activityType
     * @param wordCount
     */
    private void logActivityAlreadyLocked(long actorId,
            IsEntityWithType context, IsEntityWithType target,
            ActivityType activityType, int wordCount) {
        if (context != null && activityType != null) {
            Date currentActionTime = new Date();
            Activity activity =
                    findActivity(actorId, context.getEntityType(),
                            context.getId(), activityType, currentActionTime);
            if (activity != null) {
                activity.updateActivity(currentActionTime, target, wordCount);
            } else {
                HPerson actor = personDAO.findById(actorId);
                activity =
                        new Activity(actor, context, target, activityType,
                                wordCount);
            }
            activityDAO.makePersistent(activity);
            activityDAO.flush();
        }
    }

    @Override
    public Object getEntity(EntityType entityType, long entityId) {
        return entityManager.find(entityType.getEntityClass(), entityId);
    }

    /**
     * Logs each text flow target translation immediately after successful
     * translation.
     */
    // uses Async to ensure transaction environment is reset, because
    // this is triggered during transaction.commit
    @Async
    public void logTextFlowStateUpdate(@Observes(during = TransactionPhase.AFTER_SUCCESS) TextFlowTargetStateEvent event_) {
        // workaround for https://issues.jboss.org/browse/WELD-2019
        final TextFlowTargetStateEvent event = event_;

        Long actorId = event.getActorId();

        if (actorId != null) {
            Lock lock = activityLockManager.getLock(actorId);
            lock.lock();
            try {
                transactionUtil.run(() -> {
                    HDocument document =
                        documentDAO.getById(event.getKey().getDocumentId());

                    HTextFlowTarget lastReviewedTarget = null;
                    HTextFlowTarget lastTranslatedTarget = null;

                    int totalReviewedWords = 0;
                    int totalTranslatedWords = 0;

                    for (TextFlowTargetStateEvent.TextFlowTargetStateChange state : event
                        .getStates()) {
                        HTextFlowTarget target =
                            textFlowTargetDAO.findById(
                                state.getTextFlowTargetId(), false);
                        if (state.getNewState().isReviewed()) {
                            lastReviewedTarget = target;
                            totalReviewedWords +=
                                target.getTextFlow().getWordCount()
                                    .intValue();
                        } else {
                            lastTranslatedTarget = target;
                            totalTranslatedWords +=
                                target.getTextFlow().getWordCount()
                                    .intValue();
                        }
                    }
                    if (lastReviewedTarget != null) {
                        logActivityAlreadyLocked(actorId,
                            document.getProjectIteration(), lastReviewedTarget,
                            ActivityType.REVIEWED_TRANSLATION,
                            totalReviewedWords);
                    }

                    if (lastTranslatedTarget != null) {
                        logActivityAlreadyLocked(actorId,
                            document.getProjectIteration(),
                            lastTranslatedTarget,
                            ActivityType.UPDATE_TRANSLATION,
                            totalTranslatedWords);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Logs document upload immediately after successful upload.
     */
    // uses Async to ensure transaction environment is reset, because
    // this is triggered during transaction.commit
    @Async
    public void onDocumentUploaded(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentUploadedEvent event_)
            throws Exception {
        // workaround for https://issues.jboss.org/browse/WELD-2019
        final DocumentUploadedEvent event = event_;
        Lock lock = activityLockManager.getLock(event.getActorId());
        lock.lock();
        try {
            transactionUtil.run(() -> {
                HDocument document = documentDAO.getById(event.getDocumentId());
                ActivityType activityType =
                        event.isSourceDocument() ?
                                ActivityType.UPLOAD_SOURCE_DOCUMENT
                                : ActivityType.UPLOAD_TRANSLATION_DOCUMENT;
                HPerson actor = personDAO.findById(event.getActorId());
                logActivityAlreadyLocked(actor.getId(),
                        document.getProjectIteration(), document, activityType,
                        getDocumentWordCount(document));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private int getDocumentWordCount(HDocument document) {
        int total = 0;

        for (HTextFlow textFlow : document.getTextFlows()) {
            total += textFlow.getWordCount().intValue();
        }
        return total;
    }

    @Override
    public int getActivityCountByActor(long personId) {
        return activityDAO.getActivityCountByActor(personId);
    }

    @Override
    public DashboardUserStats getDashboardUserStatistic(Long personId,
            Date startDate, Date endDate) {
        DashboardUserStats stats = new DashboardUserStats();
        int[] result =
                activityDAO.getTranslatedStats(personId, startDate, endDate);

        stats.setWordsTranslated(result[0]);
        stats.setMessagesTranslated(result[1]);
        stats.setDocumentsTranslated(result[2]);

        result = activityDAO.getReviewedStats(personId, startDate, endDate);
        stats.setWordsReviewed(result[0]);
        stats.setMessagesReviewed(result[1]);
        stats.setDocumentsReviewed(result[2]);

        return stats;
    }
}
