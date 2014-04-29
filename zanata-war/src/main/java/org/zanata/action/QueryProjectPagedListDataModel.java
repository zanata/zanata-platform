/*
 *
 *  * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;

import lombok.Getter;
import lombok.Setter;

/**
 * @see org.zanata.action.EntityPagedListDataModel
 */
public class QueryProjectPagedListDataModel extends
        PagedListDataModel<HProject> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Setter
    private boolean includeObsolete;

    @Setter
    @Getter
    private String query;

    public QueryProjectPagedListDataModel(int pageSize) {
        setPageSize(pageSize);
    }

    @Override
    public DataPage<HProject> fetchPage(int startRow, int pageSize) {
        ProjectDAO projectDAO =
                (ProjectDAO) Component.getInstance(ProjectDAO.class,
                        ScopeType.STATELESS);

        try {
            List<HProject> proj =
                    projectDAO.searchProjects(query, pageSize, startRow,
                            includeObsolete);

            int projectSize =
                    projectDAO.getQueryProjectSize(query, includeObsolete);

            return new DataPage<HProject>(projectSize, startRow, proj);

        } catch (ParseException e) {
            return null;
        }
    }
}
