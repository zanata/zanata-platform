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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("versionGroupDAO")
@RequestScoped
public class VersionGroupDAO extends AbstractDAOImpl<HIterationGroup, Long> {
    private static final long serialVersionUID = -3405353569468388836L;

    public VersionGroupDAO() {
        super(HIterationGroup.class);
    }

    public VersionGroupDAO(Session session) {
        super(HIterationGroup.class, session);
    }

    public int getAllGroupsCount() {
        return getAllGroups(-1, 0).size();
    }

    public List<HIterationGroup> getAllGroups(int maxResult, int firstResult) {
        Query query = getSession()
                .createQuery("from HIterationGroup order by name asc");

        query.setFirstResult(firstResult);
        if(maxResult != -1) {
            query.setMaxResults(maxResult);
        }
        query.setComment("VersionGroupDAO.getAllGroups");
        query.setCacheable(true);
        @SuppressWarnings("unchecked")
        List<HIterationGroup> list = query.list();
        return list;
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
        @SuppressWarnings("unchecked")
        List<HPerson> list = q.list();
        return list;
    }

    public List<HIterationGroup> getGroupsByMaintainer(HPerson maintainer,
        String filter, int firstResult, int maxResults) {
        final String sqlFilter = filter == null ? "" : filter;
        Query q = getSession().createQuery(
            "from HIterationGroup g " +
                "where :maintainer in elements(g.maintainers) " +
                "and (g.name like :filter " +
                "or g.slug like :filter) " +
                "order by g.name")
            .setParameter("maintainer", maintainer)
            .setParameter("filter", "%" + sqlFilter + "%")
            .setFirstResult(firstResult)
            .setMaxResults(maxResults);
        @SuppressWarnings("unchecked")
        List<HIterationGroup> list = q.list();
        return list;
    }

    public int getMaintainedGroupCount(HPerson maintainer, String filter) {
        final String sqlFilter = filter == null ? "" : filter;
        Query q = getSession().createQuery(
            "select count(g) from HIterationGroup g " +
                "where :maintainer in elements(g.maintainers) " +
                "and (g.name like :filter " +
                "or g.slug like :filter) " +
                "order by g.name")
            .setParameter("maintainer", maintainer)
            .setParameter("filter", "%" + sqlFilter + "%");
        return ((Long) q.uniqueResult()).intValue();
    }

    public List<HIterationGroup> searchGroupBySlugAndName(String searchTerm,
            int maxResult, int firstResult) {
        if (StringUtils.isEmpty(searchTerm)) {
            return new ArrayList<>();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("from HIterationGroup ")
                .append("where (lower(slug) LIKE :searchTerm escape '!' ")
                .append("OR lower(name) LIKE :searchTerm escape '!') ")
                .append("order by name asc");
        Query query = getSession().createQuery(sb.toString());
        String escapeSearchTerm =
                StringUtils.lowerCase(escapeQuery(searchTerm));
        query.setParameter("searchTerm", "%" + escapeSearchTerm + "%");
        query.setFirstResult(firstResult);
        if(maxResult != -1) {
            query.setMaxResults(maxResult);
        }
        query.setComment("VersionGroupDAO.searchGroupBySlugAndName");
        query.setCacheable(true);
        @SuppressWarnings("unchecked")
        List<HIterationGroup> list = query.list();
        return list;
    }

    public int searchGroupBySlugAndNameCount(String searchTerm) {
        return searchGroupBySlugAndName(searchTerm, -1, 0).size();
    }
}
