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
package org.zanata.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import javax.enterprise.context.RequestScoped;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;

@RequestScoped
public class PersonDAO extends AbstractDAOImpl<HPerson, Long> {

    private static final long serialVersionUID = -7523118638634663058L;

    public PersonDAO() {
        super(HPerson.class);
    }

    public PersonDAO(Session session) {
        super(HPerson.class, session);
    }

    public HPerson findByEmail(String email) {
        return (HPerson) getSession().byNaturalId(HPerson.class)
                .using("email", email).load();
    }

    @SuppressWarnings("unchecked")
    public List<HLocale> getLanguageMembershipByUsername(String userName) {
        Query query =
                getSession()
                        .createQuery(
                                "select m.id.supportedLanguage from HLocaleMember as m where m.id.person.account.username = :username");
        query.setParameter("username", userName);
        query.setCacheable(false);
        query.setComment("PersonDAO.getLanguageMembershipByUsername");
        List<HLocale> re = new ArrayList<HLocale>();
        List<HLocale> su = query.list();
        for (HLocale lan : su) {
            if (lan.isActive()) {
                re.add(lan);
            }
        }
        return re;
    }

    public HPerson findByUsername(String username) {
        Query query =
                getSession()
                        .createQuery(
                                "from HPerson as p where p.account.username = :username");
        query.setParameter("username", username);
        query.setCacheable(false);
        query.setComment("PersonDAO.findByUsername");
        return (HPerson) query.uniqueResult();
    }

    public String findEmail(String username) {
        Query query =
                getSession()
                        .createQuery(
                                "select p.email from HPerson as p where p.account.username = :username");
        query.setParameter("username", username);
        query.setCacheable(false);
        query.setComment("PersonDAO.findEmail");
        return (String) query.uniqueResult();

    }

    public int findAllEnabledContainingNameSize(String name) {
        return findAllEnabledContainingName(name).size();
    }

    public List<HPerson> findAllEnabledContainingName(String name) {
        return findAllEnabledContainingName(name, -1, 0);
    }

    public List<HPerson> findAllEnabledContainingName(String name, int maxResult,
        int firstResult) {
        if (StringUtils.isEmpty(name)) {
            return new ArrayList<>();
        }
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("from HPerson as p ")
                .append("where p.account.enabled=true ")
                .append("and (lower(p.account.username) like :name ")
                .append("or lower(p.name) like lower(:name) escape '!')");
        Query query = getSession().createQuery(queryBuilder.toString());
        String escapeName = escapeQuery(name);
        query.setParameter("name", "%" + escapeName + "%");
        query.setCacheable(false);
        query.setFirstResult(firstResult);
        if(maxResult != -1) {
            query.setMaxResults(maxResult);
        }
        query.setComment("PersonDAO.findAllEnabledContainingName");
        @SuppressWarnings("unchecked")
        List<HPerson> results = query.list();
        return results;
    }

    public int getTotalTranslator() {
        Query query =
                getSession()
                        .createQuery(
                                "select count(distinct id.person) from HLocaleMember where isTranslator = :isTranslator");
        query.setParameter("isTranslator", true);

        Long totalCount = (Long) query.uniqueResult();
        query.setCacheable(true).setComment("PersonDAO.getTotalTranslator");
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalReviewer() {
        Query query =
                getSession()
                        .createQuery(
                                "select count(distinct id.person) from HLocaleMember where isReviewer = :isReviewer");
        query.setParameter("isReviewer", true);

        Long totalCount = (Long) query.uniqueResult();
        query.setCacheable(true).setComment("PersonDAO.getTotalReviewer");
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    /**
     * Indicates if a Person is a member of a language team with selected roles.
     *
     * @param person
     * @param language
     * @param isTranslator
     * @param isReviewer
     * @param isCoordinator
     * @return True if person is a member of the language team with selected
     *         roles.
     */
    public boolean isUserInLanguageTeamWithRoles(HPerson person,
            HLocale language, Boolean isTranslator, Boolean isReviewer,
            Boolean isCoordinator) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from HLocaleMember where ");
        sb.append("id.person = :person ");
        sb.append("and id.supportedLanguage = :language ");

        if (isTranslator != null) {
            sb.append("and isTranslator = :isTranslator ");
        }
        if (isReviewer != null) {
            sb.append("and isReviewer = :isReviewer ");
        }
        if (isCoordinator != null) {
            sb.append("and isCoordinator = :isCoordinator ");
        }

        Query q =
                getSession().createQuery(sb.toString().trim())
                        .setParameter("person", person)
                        .setParameter("language", language);

        if (isTranslator != null) {
            q.setParameter("isTranslator", isTranslator);
        }
        if (isReviewer != null) {
            q.setParameter("isReviewer", isReviewer);
        }
        if (isCoordinator != null) {
            q.setParameter("isCoordinator", isCoordinator);
        }

        q.setCacheable(false).setComment(
                "PersonDAO.isUserInLanguageTeamWithRoles");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount > 0L;
    }

    public List<HLocaleMember> getAllLanguageTeamMemberships(HPerson person) {
        Query q =
                getSession().createQuery(
                        "from HLocaleMember where id.person = :person")
                        .setParameter("person", person);
        q.setCacheable(false).setComment(
                "PersonDAO.getAllLanguageTeamMemberships");
        @SuppressWarnings("unchecked")
        List<HLocaleMember> list = q.list();
        return list;
    }

}
