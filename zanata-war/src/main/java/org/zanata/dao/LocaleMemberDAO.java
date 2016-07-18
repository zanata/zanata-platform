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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;

import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HLocaleMember.HLocaleMemberPk;
import org.zanata.model.HPerson;

@RequestScoped
public class LocaleMemberDAO extends
        AbstractDAOImpl<HLocaleMember, HLocaleMemberPk> {

    public LocaleMemberDAO() {
        super(HLocaleMember.class);
    }

    public LocaleMemberDAO(Session session) {
        super(HLocaleMember.class, session);
    }

    @SuppressWarnings("unchecked")
    public List<HLocaleMember> findAllByLocale(LocaleId localeId) {
        Query query =
                getSession()
                        .createQuery(
                                "from HLocaleMember as m where m.id.supportedLanguage.localeId = :localeId order by lower(m.id.person.name)");
        query.setParameter("localeId", localeId);
        query.setComment("LocaleMemberDAO.findAllByLocale");
        return query.list();
    }

    public List<HLocaleMember> findActiveMembers(LocaleId localeId,
        @Nullable HPerson excludePerson) {
        StringBuilder sb = new StringBuilder();
        sb.append("from HLocaleMember ")
            .append("where id.supportedLanguage.localeId = :localeId ");
        if (excludePerson != null) {
            sb.append("and id.person.id <> :excludePerson ");
        }
        sb.append("and (isTranslator = true ")
            .append("or isReviewer = true ")
            .append("or isCoordinator = true) ")
            .append("order by lower(id.person.name)");
        Query query = getSession().createQuery(sb.toString());
        query.setParameter("localeId", localeId);
        if (excludePerson != null) {
            query.setParameter("excludePerson", excludePerson.getId());
        }
        query.setCacheable(true);
        query.setComment("LocaleMemberDAO.findActiveMembers");
        return query.list();
    }

    public List<HLocaleMember> findAllActiveMembers(LocaleId localeId) {
        return findActiveMembers(localeId, null);
    }

    /**
     * Check if person is a reviewer of any language
     *
     * @param personId
     * @return
     */
    public List<HLocaleMember> findByPersonWithReviewerRole(Long personId) {
        Query query =
                getSession()
                        .createQuery(
                                "from HLocaleMember where id.person.id= :personId and isReviewer = :isReviewer");
        query.setParameter("personId", personId);
        query.setParameter("isReviewer", true);
        query.setComment("LocaleMemberDAO.findByPersonWithReviewerRole");
        return query.list();
    }

    public boolean isLocaleCoordinator(Long personId, LocaleId localeId) {
        Query query =
                getSession()
                        .createQuery(
                                "from HLocaleMember as m where m.id.supportedLanguage.localeId = :localeId "
                                        + "and m.id.person.id = :personId and m.coordinator = true");
        query.setParameter("localeId", localeId).setParameter("personId",
                personId);
        query.setComment("LocaleMemberDAO.isLocaleCoordinator");
        return query.list().size() > 0;
    }

    public boolean isLocaleMember(Long personId, LocaleId localeId) {
        Query query =
                getSession().createQuery(
                        "from HLocaleMember as m where m.id.supportedLanguage.localeId = :localeId "
                                + "and m.id.person.id = :personId");
        query.setParameter("localeId", localeId).setParameter("personId",
                personId);
        query.setComment("LocaleMemberDAO.isLocaleMember");
        return query.list().size() > 0;
    }

    /*
     * NB: Override the base class method because it is not generating a
     * 'delete' statement. By having an HQL statement, this is guaranteed. THis
     * could be because of the entity in question having a composite primary
     * key. Need to try this in a later version of Hibernate.
     */
    @Override
    public void makeTransient(HLocaleMember entity) {
        HLocale locale = entity.getSupportedLanguage();
        getSession()
                .createQuery(
                        "delete HLocaleMember as m where m.id.supportedLanguage = :locale "
                                + "and m.id.person = :person")
                .setParameter("locale", locale)
                .setParameter("person", entity.getPerson()).executeUpdate();

        // We need to evict the HLocale to refresh member list within
        getSession().getSessionFactory().getCache()
                .evictEntity(HLocale.class, locale.getId());
    }

    public HLocaleMember
            findByPersonAndLocale(Long personId, LocaleId localeId) {
        Query query =
                getSession().createQuery(
                        "from HLocaleMember as m where m.id.supportedLanguage.localeId = :localeId "
                                + "and m.id.person.id = :personId");
        query.setParameter("localeId", localeId).setParameter("personId",
                personId);
        query.setComment("LocaleMemberDAO.findByPersonAndLocale");
        return (HLocaleMember) query.uniqueResult();
    }
}
