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
package org.zanata.service;

import java.util.Date;
import java.util.List;

import org.zanata.common.ActivityType;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.Activity;
import org.zanata.model.IsEntityWithType;
import org.zanata.model.type.EntityType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface ActivityService {
    /**
     * Find activity with given person, context, action in the hour of given
     * action time
     *
     * @param actorId
     * @param contextType
     * @param contextId
     * @param activityType
     * @param actionTime
     * @return
     */
    Activity findActivity(long actorId, EntityType contextType, long contextId,
            ActivityType activityType, Date actionTime);

    /**
     * Get user activities with given contextId
     *
     * @param personId
     * @param contextId
     * @param offset
     * @param count
     * @return List<Activity>
     */
    List<Activity> findLatestActivitiesForContext(long personId,
            long contextId, int offset, int count);

    /**
     * Get user activities regardless of which context
     *
     * @param personId
     * @param offset
     * @param maxResults
     * @return List<Activity>
     */
    List<Activity> findLatestActivities(long personId, int offset,
            int maxResults);

    /**
     * Log activity, records roll up in hourly basis
     *
     * @param actorId
     * @param context
     * @param target
     * @param activityType
     * @param wordCount
     */
    void logActivity(long actorId, IsEntityWithType context,
            IsEntityWithType target, ActivityType activityType, int wordCount);

    /**
     * Get target or lastTarget entity in activity
     *
     * @param entityType
     * @param entityId
     * @return
     * @throws Exception
     */
    Object getEntity(EntityType entityType, long entityId)
            throws ZanataServiceException;

    /**
     * Get activity count of an actor
     *
     * @param personId
     * @return
     */
    int getActivityCountByActor(long personId);
}
