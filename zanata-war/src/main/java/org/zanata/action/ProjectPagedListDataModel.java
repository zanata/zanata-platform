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
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;

public class ProjectPagedListDataModel extends PagedListDataModel<HProject>
        implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean filterActive;
    private boolean filterReadOnly;
    private boolean filterObsolete;

    public ProjectPagedListDataModel(boolean filterActive,
            boolean filterReadOnly, boolean filterObsolete) {
        this.filterActive = filterActive;
        this.filterReadOnly = filterReadOnly;
        this.filterObsolete = filterObsolete;
    }

    @Override
    public DataPage<HProject> fetchPage(int startRow, int pageSize) {
        ProjectDAO projectDAO =
                (ProjectDAO) Component.getInstance(ProjectDAO.class,
                        ScopeType.STATELESS);
        List<HProject> proj =
                projectDAO.getOffsetListOrderByName(startRow, pageSize,
                        filterActive, filterReadOnly, filterObsolete);

        int projectSize =
                projectDAO.getFilterProjectSize(filterActive, filterReadOnly,
                        filterObsolete);

        return new DataPage<HProject>(projectSize, startRow, proj);
    }

    public void setFilterObsolete(boolean filterObsolete) {
        this.filterObsolete = filterObsolete;
        refresh();
    }

    public void setFilterActive(boolean filterActive) {
        this.filterActive = filterActive;
        refresh();
    }

    public void setFilterReadOnly(boolean filterReadOnly) {
        this.filterReadOnly = filterReadOnly;
        refresh();
    }

}
