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
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.common.base.Function;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.action.helper.PagedDataHandler;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.EntityStatus;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.ActivityService;
import org.zanata.service.GravatarService;
import org.zanata.service.LanguageTeamService;
import org.zanata.util.DateUtil;
import org.zanata.util.StringUtil;
import org.zanata.util.UrlUtil;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private ProjectIterationDAO projectIterationDAO;

    @In
    private ProjectDAO projectDAO;

    @In
    private ZanataIdentity identity;

    @In
    private UrlUtil urlUtil;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @Getter
    private ProjectPageList projectList = new ProjectPageList();

    private final int USER_IMAGE_SIZE = 115;

    private final Comparator<HProject> projectCreationDateComparator =
            new Comparator<HProject>() {
                @Override
                public int compare(HProject o1, HProject o2) {
                    return o2.getCreationDate().after(o1.getCreationDate()) ? 1
                            : -1;
                }
            };

    public String getUserImageUrl() {
        return gravatarServiceImpl.getUserImageUrl(USER_IMAGE_SIZE);
    }

    public String getUsername() {
        return authenticatedAccount.getPerson().getAccount().getUsername();
    }

    public String getUserFullName() {
        return authenticatedAccount.getPerson().getName();
    }

    public String getUserLanguageTeams() {
        HAccount account = accountDAO.findById(authenticatedAccount.getId());
        return StringUtil.concat(account.getPerson().getLanguageMemberships(),
                ',', new Function<HLocale, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable HLocale locale) {
                        return locale.retrieveDisplayName();
                    }
                });
    }

    @CachedMethodResult
    public int getUserMaintainedProjectsCount() {
        return authenticatedAccount.getPerson().getMaintainerProjects().size();
    }

    @CachedMethodResult
    public List<HProject> getUserMaintainedProjects() {
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
        Collections.sort(sortedList, projectCreationDateComparator);

        return sortedList;
    }

    public String getLastUpdatedTimeLapseMessage(HProject project) {
        return DateUtil.getHowLongAgoDescription(project.getLastChanged());
    }

    public String getLastUpdatedTime(HProject project) {
        return DateUtil.formatShortDate(project.getLastChanged());
    }

    public long getProjectListPageStart() {
        return projectList.getCurrentPageFirstRecord() + 1;
    }

    public long getProjectListPageEnd() {
        return Math.min(projectList.getTotalRecords(),
                projectList.getCurrentPageFirstRecord()
                        + projectList.getCurrentPageData().size());
    }

    @CachedMethodResult
    public boolean canViewObsolete() {
        return identity != null
                && identity.hasPermission("HProject", "view-obsolete");
    }

    @CachedMethodResult
    public List<HProjectIteration> getProjectVersions(Long projectId) {
        List<HProjectIteration> result = new ArrayList<HProjectIteration>();
        if (canViewObsolete()) {
            result.addAll(projectIterationDAO.searchByProjectId(projectId));
        } else {
            result.addAll(projectIterationDAO.searchByProjectIdExcludingStatus(
                    projectId, EntityStatus.OBSOLETE));
        }
        return result;
    }

    @CachedMethodResult
    public boolean isUserMaintainer(Long projectId) {
        HProject project = getProject(projectId);
        return authenticatedAccount.getPerson().isMaintainer(project);
    }

    @CachedMethodResult
    public int getDocumentCount(Long versionId) {
        HProjectIteration version = getProjectVersion(versionId);
        return version.getDocuments().size();
    }

    public String getFormattedDate(Date date) {
        return DateUtil.formatShortDate(date);
    }

    @CachedMethodResult
    private HProjectIteration getProjectVersion(Long versionId) {
        return projectIterationDAO.findById(versionId, false);
    }

    @CachedMethodResult
    private HProject getProject(Long projectId) {
        return projectDAO.findById(projectId, false);
    }

    public String getCreateVersionUrl(String projectSlug) {
        return urlUtil.createNewVersionUrl(projectSlug);
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
        return languageTeamServiceImpl.isUserReviewer(authenticatedAccount.getPerson().getId());
    }

    private class ProjectPageList
            extends PagedDataHandler<HProject> {

        @Override
        protected List<HProject> fetchCurrentPage() {
            ProjectDAO projectDAO = 
                    (ProjectDAO)Component.getInstance(ProjectDAO.class);
            HAccount authenticatedAccount =
                    (HAccount) Component
                            .getInstance(JpaIdentityStore.AUTHENTICATED_USER);
            return projectDAO.getProjectsForMaintainer(
                            authenticatedAccount.getPerson(),
                            ((Long)getCurrentPageFirstRecord()).intValue(),
                            getPageSize());
        }

        @Override
        protected long fetchTotalRecords() {
            ProjectDAO projectDAO =
                    (ProjectDAO)Component.getInstance(ProjectDAO.class);
            HAccount authenticatedAccount =
                    (HAccount) Component
                            .getInstance(JpaIdentityStore.AUTHENTICATED_USER);
            return projectDAO.getMaintainedProjectCount(
                    authenticatedAccount.getPerson());
        }
    }
}
