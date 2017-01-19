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
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.dao.VersionGroupDAO;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.model.HIterationGroup;
import org.zanata.seam.security.IdentityManager;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.CheckLoggedIn;
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
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;
import javax.annotation.Nullable;

@Named("dashboardAction")
@ViewScoped
@CheckLoggedIn
@Model
@Transactional
public class DashboardAction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    private GravatarService gravatarServiceImpl;
    @Inject
    private ActivityService activityServiceImpl;
    @Inject
    private LanguageTeamService languageTeamServiceImpl;
    @Inject
    private AccountDAO accountDAO;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private Messages msgs;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private ProjectFilter projectList;
    @Inject
    private GroupFilter groupList;
    private final java.util.concurrent.atomic.AtomicReference<Object> userMaintainedProjectsCount =
            new java.util.concurrent.atomic.AtomicReference<Object>();
    private final java.util.concurrent.atomic.AtomicReference<Object> userMaintainedProjects =
            new java.util.concurrent.atomic.AtomicReference<Object>();
    @Inject
    private IdentityManager identityManager;


    public String getUserImageUrl() {
        return gravatarServiceImpl
                .getUserImageUrl(GravatarService.USER_IMAGE_SIZE);
    }

    public String getUsername() {
        return authenticatedAccount.getPerson().getAccount().getUsername();
    }

    public String getUserFullName() {
        return authenticatedAccount.getPerson().getName();
    }

    public String getUserLanguageTeams() {
        HAccount account = accountDAO.findById(authenticatedAccount.getId());
        return StringUtils.join(Collections2.transform(
                account.getPerson().getLanguageMemberships(),
                new Function<HLocale, Object>() {

                    @Nullable
                    @Override
                    public Object apply(@Nonnull HLocale locale) {
                        if (locale == null) {
                            throw new NullPointerException("locale");
                        }
                        return locale.retrieveDisplayName();
                    }
                }), ", ");
    }

    public String getUserRoles() {
        HAccount account = accountDAO.findById(authenticatedAccount.getId());
        List<String> roles =
            identityManager.getGrantedRoles(account.getUsername());
        return StringUtils.join(roles, ", ");
    }

    private int countUserMaintainedProjects() {
        return projectDAO.getMaintainedProjectCount(
                authenticatedAccount.getPerson(), null);
    }

    private List<HProject> fetchUserMaintainedProjects() {
        List<HProject> sortedList = new ArrayList<HProject>();
        if (canViewObsolete()) {
            sortedList.addAll(
                    authenticatedAccount.getPerson().getMaintainerProjects());
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
        return lastTranslatedDate == null ? "never"
                : DateUtil.getHowLongAgoDescription(lastTranslatedDate);
    }

    public String getShortTime(Date date) {
        return DateUtil.formatShortDate(date);
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
        return languageTeamServiceImpl
                .isUserReviewer(authenticatedAccount.getPerson().getId());
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
            return msgs.format(
                    "jsf.dashboard.activity.lastTranslatedBy.message",
                    username);
        }
        return "";
    }

    public boolean canCreateProject() {
        return identity.hasPermission(new HProject(), "insert");
    }

    public boolean canCreateGroup() {
        return identity.hasPermission(new HIterationGroup(), "insert");
    }

    /**
     * Project list filter. Pages its elements directly from the database.
     */
    public static class ProjectFilter extends AbstractListFilter<HProject>
            implements Serializable {

        @Inject
        @Authenticated
        private HAccount authenticatedAccount;
        @Inject
        private ProjectDAO projectDAO;

        @Override
        protected List<HProject> fetchRecords(int start, int max,
                String filter) {
            return projectDAO.getProjectsForMaintainer(
                    authenticatedAccount.getPerson(), filter, start, max);
        }

        @Override
        protected long fetchTotalRecords(String filter) {
            return projectDAO.getMaintainedProjectCount(
                    authenticatedAccount.getPerson(), filter);
        }
    }

    /**
     * Group list filter. Pages its elements directly from the database.
     */
    public static class GroupFilter extends AbstractListFilter<HIterationGroup>
            implements Serializable {

        @Inject
        @Authenticated
        private HAccount authenticatedAccount;
        @Inject
        private VersionGroupDAO versionGroupDAO;

        @Override
        protected List<HIterationGroup> fetchRecords(int start, int max,
                String filter) {
            return versionGroupDAO.getGroupsByMaintainer(
                    authenticatedAccount.getPerson(), filter, start, max);
        }

        @Override
        protected long fetchTotalRecords(String filter) {
            return versionGroupDAO.getMaintainedGroupCount(
                    authenticatedAccount.getPerson(), filter);
        }
    }

    public ProjectFilter getProjectList() {
        return this.projectList;
    }

    public GroupFilter getGroupList() {
        return this.groupList;
    }

    public int getUserMaintainedProjectsCount() {
        Object value = this.userMaintainedProjectsCount.get();
        if (value == null) {
            synchronized (this.userMaintainedProjectsCount) {
                value = this.userMaintainedProjectsCount.get();
                if (value == null) {
                    final int actualValue = countUserMaintainedProjects();
                    value = actualValue;
                    this.userMaintainedProjectsCount.set(value);
                }
            }
        }
        return (Integer) value;
    }

    public List<HProject> getUserMaintainedProjects() {
        Object value = this.userMaintainedProjects.get();
        if (value == null) {
            synchronized (this.userMaintainedProjects) {
                value = this.userMaintainedProjects.get();
                if (value == null) {
                    final List<HProject> actualValue =
                            fetchUserMaintainedProjects();
                    value = actualValue == null ? this.userMaintainedProjects
                            : actualValue;
                    this.userMaintainedProjects.set(value);
                }
            }
        }
        return (List<HProject>) (value == this.userMaintainedProjects ? null
                : value);
    }
}
