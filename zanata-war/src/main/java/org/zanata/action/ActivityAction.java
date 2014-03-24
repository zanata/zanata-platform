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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.model.Activity;
import org.zanata.model.HAccount;
import org.zanata.service.ActivityService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("activityAction")
@Scope(ScopeType.PAGE)
@Restrict("#{identity.loggedIn}")
public class ActivityAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private ActivityService activityServiceImpl;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    private final int ACTIVITY_COUNT_PER_LOAD = 5;

    private final int MAX_ACTIVITIES_COUNT_PER_PAGE = 20;

    private int activityPageIndex = 0;

    public List<Activity> getActivities() {
        List<Activity> activities = new ArrayList<Activity>();

        if (authenticatedAccount != null) {
            int count = (activityPageIndex + 1) * ACTIVITY_COUNT_PER_LOAD;
            activities =
                    activityServiceImpl.findLatestActivities(
                            authenticatedAccount.getPerson().getId(), 0, count);
        }
        return activities;
    }

    public void loadNextActivity() {
        activityPageIndex++;
    }

    public boolean hasMoreActivities() {
        int loadedActivitiesCount =
                (activityPageIndex + 1) * ACTIVITY_COUNT_PER_LOAD;
        int totalActivitiesCount =
                activityServiceImpl
                        .getActivityCountByActor(authenticatedAccount
                                .getPerson().getId());

        if ((loadedActivitiesCount < totalActivitiesCount)
                && (loadedActivitiesCount < MAX_ACTIVITIES_COUNT_PER_PAGE)) {
            return true;
        }
        return false;
    }
}
