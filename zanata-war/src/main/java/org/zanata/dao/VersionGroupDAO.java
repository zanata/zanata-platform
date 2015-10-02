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
import javax.inject.Named;
import org.zanata.common.EntityStatus;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("versionGroupDAO")

@javax.enterprise.context.Dependent
public class VersionGroupDAO extends AbstractDAOImpl<HIterationGroup, Long> {
    public VersionGroupDAO() {
        super(HIterationGroup.class);
    }

    public VersionGroupDAO(Session session) {
        super(HIterationGroup.class, session);
    }

    public List<HIterationGroup> getAllGroups(EntityStatus... statuses) {
        StringBuilder sb = new StringBuilder();
        sb.append("from HIterationGroup g ");
        if (statuses != null && statuses.length >= 1) {
            sb.append("where g.status in :statuses");
        }
        Query query = getSession().createQuery(sb.toString());

        if (statuses != null && statuses.length >= 1) {
            query.setParameterList("statuses", Lists.newArrayList(statuses));
        }

        query.setComment("VersionGroupDAO.getAllGroups");
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

    public List<HIterationGroup> searchGroupBySlugAndName(String searchTerm) {
        if (StringUtils.isEmpty(searchTerm)) {
            return new ArrayList<HIterationGroup>();
        }
        Query query =
                getSession()
                        .createQuery(
                                "from HIterationGroup g where (lower(g.slug) LIKE :searchTerm OR lower(g.name) LIKE :searchTerm) AND g.status = :status");
        query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        query.setParameter("status", EntityStatus.ACTIVE);
        query.setComment("VersionGroupDAO.searchGroupBySlugAndName");
        return query.list();
    }
}
