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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionGroupService;

import com.google.common.collect.Lists;

@Name("versionGroupJoinAction")
@Scope(ScopeType.PAGE)
public class VersionGroupJoinAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private VersionGroupService versionGroupServiceImpl;

    @In
    private VersionGroupDAO versionGroupDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In(create = true)
    private SendEmailAction sendEmail;

    @Getter
    private HProjectIteration selectedVersion;

    @Getter
    @Setter
    private String slug;

    @Getter
    @Setter
    private String iterationSlug;

    @Getter
    @Setter
    private String projectSlug;

    public String getGroupName() {
        return versionGroupDAO.getBySlug(slug).getName();
    }

    public void searchProjectVersion() {
        if (StringUtils.isNotEmpty(iterationSlug)
                && StringUtils.isNotEmpty(projectSlug)) {
            selectedVersion =
                    projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        }
    }

    public boolean isVersionInGroup(Long projectIterationId) {
        return versionGroupServiceImpl.isVersionInGroup(slug,
                projectIterationId);
    }

    public void cancel() {
        sendEmail.cancel();
    }

    public String send() {
        if (selectedVersion != null
                && !isVersionInGroup(selectedVersion.getId())) {
            List<HPerson> maintainers = new ArrayList<HPerson>();
            for (HPerson maintainer : versionGroupServiceImpl
                    .getMaintainersBySlug(slug)) {
                maintainers.add(maintainer);
            }
            return sendEmail.sendToVersionGroupMaintainer(maintainers);
        } else {
            FacesMessages.instance().add(
                    "#{messages['jsf.NoProjectVersionSelected']}");
            return "success";
        }

    }
}
