/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import javax.inject.Named;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectLocaleMember;
import org.zanata.model.LocaleRole;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides methods to access data related to membership in a locale for a project.
 */
@Named("projectLocaleMemberDAO")

@javax.enterprise.context.Dependent
public class ProjectLocaleMemberDAO
        extends AbstractDAOImpl<HProjectLocaleMember, HProjectLocaleMember.HProjectLocaleMemberPK> {

    public ProjectLocaleMemberDAO() {
        super(HProjectLocaleMember.class);
    }

    public ProjectLocaleMemberDAO(Session session) {
        super(HProjectLocaleMember.class, session);
    }

    /**
     * Retrieve all of a person's roles in a locale for a project.
     */
    public Set<LocaleRole> getRolesInProjectLocale(HPerson person, HProject project, HLocale locale) {
        Query query = getSession().createQuery(
                "from HProjectLocaleMember as m where m.person = :person " +
                        "and m.project = :project " +
                        "and m.locale = :locale")
                .setParameter("person", person)
                .setParameter("project", project)
                .setParameter("locale", locale)
                .setComment("ProjectLocaleMemberDAO.getRolesInProjectLocale");
        return new HashSet<>(query.list());
    }

    /**
     * Check whether a person has a specified role in a locale for a project.
     *
     * @return true if the given person has the given role in the given project
     *              and locale.
     */
    public boolean hasProjectLocaleRole(HPerson person, HProject project, HLocale locale, LocaleRole role) {
        Query query = getSession().createQuery(
                "select count(m) from HProjectLocaleMember as m " +
                        "where m.person = :person " +
                        "and m.project = :project " +
                        "and m.locale = :locale " +
                        "and m.role = :role")
                .setParameter("person", person)
                .setParameter("project", project)
                .setParameter("locale", locale)
                .setParameter("role", role)
                .setComment("ProjectLocaleMemberDAO.hasProjectLocaleRole");
        return ((Long) query.uniqueResult()) > 0;
    }
}
