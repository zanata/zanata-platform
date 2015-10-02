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
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.service.VersionGroupService;

import com.google.common.collect.Lists;

@Named("versionGroupAction")
@javax.faces.bean.ViewScoped
public class VersionGroupAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private VersionGroupService versionGroupServiceImpl;

    @Inject /* TODO [CDI] check this: migrated from @In(required = false, value = ZanataJpaIdentityStore.AUTHENTICATED_USER) */
    private HAccount authenticatedAccount;

    @Getter
    @Setter
    private String groupNameFilter;

    @Getter
    @Setter
    private boolean showObsoleteGroups = false;

    public List<HIterationGroup> getAllVersionGroups() {
        List<EntityStatus> statusList = Lists.newArrayList(EntityStatus.ACTIVE);
        if (authenticatedAccount != null && isShowObsoleteGroups()) {
            statusList.add(EntityStatus.OBSOLETE);
        }
        EntityStatus[] statuses =
                statusList.toArray(new EntityStatus[statusList.size()]);

        return versionGroupServiceImpl.getAllGroups(statuses);
    }
}
