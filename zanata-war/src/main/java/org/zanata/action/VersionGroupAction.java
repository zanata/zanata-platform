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

import lombok.Getter;
import lombok.Setter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.service.VersionGroupService;

@Name("versionGroupAction")
@Scope(ScopeType.PAGE)
public class VersionGroupAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private VersionGroupService versionGroupServiceImpl;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @Setter
    private List<HIterationGroup> allVersionGroups;

    @Getter
    @Setter
    private String groupNameFilter;

    @Getter
    @Setter
    private boolean showActiveGroups = true;

    @Getter
    @Setter
    private boolean showObsoleteGroups = false;

    public void loadAllActiveGroupsOrIsMaintainer() {
        if (authenticatedAccount != null) {
            allVersionGroups =
                    versionGroupServiceImpl
                            .getAllActiveAndMaintainedGroups(authenticatedAccount
                                    .getPerson());
        } else {
            allVersionGroups = versionGroupServiceImpl.getAllActiveGroups();
        }
    }

    private boolean filterGroupByStatus(HIterationGroup group) {
        if (isShowActiveGroups() && isShowObsoleteGroups()) {
            return true;
        }

        if (group.getStatus() == EntityStatus.OBSOLETE) {
            return isShowObsoleteGroups();
        } else if (group.getStatus() == EntityStatus.ACTIVE) {
            return isShowActiveGroups();
        }
        return false;
    }

    public List<HIterationGroup> getAllVersionGroups() {
        List<HIterationGroup> result = new ArrayList<HIterationGroup>();
        for (HIterationGroup group : allVersionGroups) {
            if (filterGroupByStatus(group)) {
                result.add(group);
            }
        }
        return result;
    }
}
