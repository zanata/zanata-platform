/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.ActivityService;
import org.zanata.service.GravatarService;
import org.zanata.ui.AbstractListFilter;
import org.zanata.util.ComparatorUtil;
import org.zanata.service.LanguageTeamService;
import org.zanata.util.DateUtil;
import org.zanata.util.ServiceLocator;

import javax.annotation.Nullable;

import lombok.Getter;

@Name("dashboardAction")
@Scope(ScopeType.PAGE)
@Restrict("#{identity.loggedIn}")
public class DashboardAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private GravatarService gravatarServiceImpl;

    @In
    private ActivityService activityServiceImpl;

    @In
    private LanguageTeamService languageTeamServiceImpl;

    @In
    private AccountDAO accountDAO;

    @In
    private ProjectDAO projectDAO;

    @In
    private ZanataIdentity identity;

    @In
    private Messages msgs;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @Getter
    private ProjectFilter projectList = new ProjectFilter();

    @Getter(lazy = true)
    private final int userMaintainedProjectsCount =
            countUserMaintainedProjects();

    @Getter(lazy = true)
    private final List<HProject> userMaintainedProjects =
            fetchUserMaintainedProjects();

    public String getUserImageUrl() {
        return gravatarServiceImpl.getUserImageUrl(
                GravatarService.USER_IMAGE_SIZE);
    }

    public String getUsername() {
        return authenticatedAccount.getPerson().getAccount().getUsername();
    }

    public String getUserFullName() {
        return authenticatedAccount.getPerson().getName();
    }

    public String getUserLanguageTeams() {
        HAccount account = accountDAO.findById(authenticatedAccount.getId());
        return StringUtils.join(
                Collections2.transform(account.getPerson()
                .getLanguageMemberships(),
                 new Function<HLocale, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@NonNull HLocale locale) {
                        return locale.retrieveDisplayName();
                    }
                }),
            ", ");
    }

    private int countUserMaintainedProjects() {
        return projectDAO.getMaintainedProjectCount(
                authenticatedAccount.getPerson(), null);
    }

    private List<HProject> fetchUserMaintainedProjects() {
        List<HProject> sortedList = new ArrayList<HProject>();

        if (canViewObsolete()) {
            sortedList.addAll(authenticatedAccount.getPerson()
                    .getMaintainerProjects());
        } else {
            for (HProject project : authenticatedAccount.getPerson()
                    .getMaintainerProjects()) {
                if (project.getStatus() != EntityStatus.OBSOLETE) {
                    sortedList.add(project);
                }
            }
        }
        Collections.sort(sortedList,
                ComparatorUtil.PROJECT_CREATION_DATE_COMPARATOR);

        return sortedList;
    }

    public String getLastTranslatedTimeLapseMessage(HProject project) {
        Date lastTranslatedDate = projectDAO.getLastTranslatedDate(project);
        // TODO i18n needed
        return lastTranslatedDate == null ? "never" :
                DateUtil.getHowLongAgoDescription(lastTranslatedDate);
    }

    public String getLastTranslatedTime(HProject project) {
        return DateUtil.formatShortDate(project.getLastChanged());
    }

    public boolean canViewObsolete() {
        return identity != null
                && identity.hasPermission("HProject", "view-obsolete");
    }

    public DashboardUserStats getTodayStats() {
        Date now = new Date();
        return activityServiceImpl.getDashboardUserStatistic(
                authenticatedAccount.getPerson().getId(),
                DateUtil.getStartOfDay(now), DateUtil.getEndOfTheDay(now));
    }

    public DashboardUserStats getWeekStats() {
        Date now = new Date();
        return activityServiceImpl.getDashboardUserStatistic(
                authenticatedAccount.getPerson().getId(),
                DateUtil.getStartOfWeek(now), DateUtil.getEndOfTheWeek(now));
    }

    public DashboardUserStats getMonthStats() {
        Date now = new Date();
        return activityServiceImpl.getDashboardUserStatistic(
                authenticatedAccount.getPerson().getId(),
                DateUtil.getStartOfMonth(now), DateUtil.getEndOfTheMonth(now));
    }

    public boolean isUserReviewer() {
        return languageTeamServiceImpl.isUserReviewer(
                authenticatedAccount.getPerson().getId());
    }

    public String getLastTranslatorMessage(HProject project) {
        HPerson lastTrans = projectDAO.getLastTranslator(project);
        if (lastTrans != null) {
            String username = lastTrans.getName();
            if (username == null || username.trim().isEmpty()) {
                if (lastTrans.getAccount() != null) {
                    username = lastTrans.getAccount().getUsername();
                }
            } else {
                username = lastTrans.getName();
            }
            return msgs
                    .format("jsf.dashboard.activity.lastTranslatedBy.message",
                            username);
        }
        return "";
    }

    /**
     * Project list filter. Pages its elements directly from the database.
     */
    public class ProjectFilter
            extends AbstractListFilter<HProject> {

        @Override
        protected List<HProject> fetchRecords(int start, int max,
                String filter) {
            ServiceLocator serviceLocator = ServiceLocator.instance();
            ProjectDAO projectDAO =
                    serviceLocator.getInstance(ProjectDAO.class);
            HAccount authenticatedAccount =
                    serviceLocator
                            .getInstance(JpaIdentityStore.AUTHENTICATED_USER,
                                    HAccount.class);
            return projectDAO.getProjectsForMaintainer(
                    authenticatedAccount.getPerson(), filter, start, max);
        }

        @Override
        protected long fetchTotalRecords(String filter) {
            ServiceLocator serviceLocator = ServiceLocator.instance();
            ProjectDAO projectDAO =
                    serviceLocator.getInstance(ProjectDAO.class);
            HAccount authenticatedAccount =
                    serviceLocator
                            .getInstance(JpaIdentityStore.AUTHENTICATED_USER,
                                    HAccount.class);
            return projectDAO.getMaintainedProjectCount(
                    authenticatedAccount.getPerson(), filter);
        }
    }
}
