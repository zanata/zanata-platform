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
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.model.Activity;
import org.zanata.model.HAccount;
import org.zanata.security.annotations.ZanataSecured;
import org.zanata.service.ActivityService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("activityAction")
@javax.faces.bean.ViewScoped
@ZanataSecured
@CheckLoggedIn
public class ActivityAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private ActivityService activityServiceImpl;

    @Inject /* TODO [CDI] check this: migrated from @In(required = false, value = ZanataJpaIdentityStore.AUTHENTICATED_USER) */
    private HAccount authenticatedAccount;

    private final int ACTIVITY_COUNT_PER_LOAD = 5;
    private final int MAX_ACTIVITIES_COUNT_PER_PAGE = 20;

    private int activityPageIndex = 0;

    public List<Activity> getActivities() {
        if (authenticatedAccount != null) {
            int count = (activityPageIndex + 1) * ACTIVITY_COUNT_PER_LOAD;
            return activityServiceImpl.findLatestActivities(
                            authenticatedAccount.getPerson().getId(), 0, count);
        }
        return Collections.emptyList();
    }

    public void loadNextActivity() {
        activityPageIndex++;
    }

    public boolean hasMoreActivities() {
        int loadedActivitiesCount =
                (activityPageIndex + 1) * ACTIVITY_COUNT_PER_LOAD;
        int totalActivitiesCount = activityServiceImpl
                        .getActivityCountByActor(authenticatedAccount
                                .getPerson().getId());

        return ((loadedActivitiesCount < totalActivitiesCount)
                && (loadedActivitiesCount < MAX_ACTIVITIES_COUNT_PER_PAGE));
    }
}
