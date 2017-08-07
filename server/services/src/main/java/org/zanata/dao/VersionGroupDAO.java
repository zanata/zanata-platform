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
package org.zanata.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.zanata.common.EntityStatus;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("versionGroupDAO")
@RequestScoped
public class VersionGroupDAO extends AbstractDAOImpl<HIterationGroup, Long> {
    public VersionGroupDAO() {
        super(HIterationGroup.class);
    }

    public VersionGroupDAO(Session session) {
        super(HIterationGroup.class, session);
    }

    public int getAllGroupsCount(EntityStatus... statuses) {
        return getAllGroups(-1, 0, statuses).size();
    }

    public List<HIterationGroup> getAllGroups(int maxResult, int firstResult,
        EntityStatus... statuses) {
        StringBuilder sb = new StringBuilder();
        sb.append("from HIterationGroup ");
        if (statuses != null && statuses.length >= 1) {
            sb.append("where status in :statuses ");
        }

        sb.append("order by name asc");
        Query query = getSession().createQuery(sb.toString());

        if (statuses != null && statuses.length >= 1) {
            query.setParameterList("statuses", Lists.newArrayList(statuses));
        }

        query.setFirstResult(firstResult);
        if(maxResult != -1) {
            query.setMaxResults(maxResult);
        }
        query.setComment("VersionGroupDAO.getAllGroups");
        query.setCacheable(true);
        return query.list();
    }

    public HIterationGroup getBySlug(@Nonnull String slug) {
        if (!StringUtils.isEmpty(slug)) {
            return (HIterationGroup) getSession()
                    .byNaturalId(HIterationGroup.class).using("slug", slug)
                    .load();
        }
        return null;
    }

    public List<HPerson> getMaintainersBySlug(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "select g.maintainers from HIterationGroup as g where g.slug = :slug");
        q.setParameter("slug", slug);
        q.setComment("VersionGroupDAO.getMaintainersBySlug");
        return q.list();
    }

    public List<HIterationGroup> getGroupsByMaintainer(HPerson maintainer,
        String filter, int firstResult, int maxResults) {
        final String sqlFilter = filter == null ? "" : filter;
        Query q = getSession().createQuery(
            "from HIterationGroup g " +
                "where :maintainer in elements(g.maintainers) " +
                "and g.status <> :obsolete " +
                "and (g.name like :filter " +
                "or g.slug like :filter) " +
                "order by g.name")
            .setParameter("maintainer", maintainer)
            .setParameter("obsolete", EntityStatus.OBSOLETE)
            .setParameter("filter", "%" + sqlFilter + "%")
            .setFirstResult(firstResult)
            .setMaxResults(maxResults);
        return q.list();
    }

    public int getMaintainedGroupCount(HPerson maintainer, String filter) {
        final String sqlFilter = filter == null ? "" : filter;
        Query q = getSession().createQuery(
            "select count(g) from HIterationGroup g " +
                "where :maintainer in elements(g.maintainers) " +
                "and g.status <> :obsolete " +
                "and (g.name like :filter " +
                "or g.slug like :filter) " +
                "order by g.name")
            .setParameter("maintainer", maintainer)
            .setParameter("obsolete", EntityStatus.OBSOLETE)
            .setParameter("filter", "%" + sqlFilter + "%");
        return ((Long) q.uniqueResult()).intValue();
    }

    public List<HIterationGroup> searchGroupBySlugAndName(String searchTerm,
            int maxResult, int firstResult, EntityStatus... statuses) {
        if (StringUtils.isEmpty(searchTerm)) {
            return new ArrayList<HIterationGroup>();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("from HIterationGroup ")
                .append("where (lower(slug) LIKE :searchTerm OR lower(name) LIKE :searchTerm) ");
        if (statuses != null && statuses.length >= 1) {
            sb.append("AND status in :statuses ");
        }
        sb.append("order by name asc");
        Query query = getSession().createQuery(sb.toString());
        query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        query.setFirstResult(firstResult);
        if(maxResult != -1) {
            query.setMaxResults(maxResult);
        }
        if (statuses != null && statuses.length >= 1) {
            query.setParameterList("statuses", Lists.newArrayList(statuses));
        }
        query.setComment("VersionGroupDAO.searchGroupBySlugAndName");
        query.setCacheable(true);
        return query.list();
    }

    public int searchGroupBySlugAndNameCount(String searchTerm, EntityStatus... statuses) {
        return searchGroupBySlugAndName(searchTerm, -1, 0, statuses).size();
    }
}
