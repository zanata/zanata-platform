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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import javax.inject.Named;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;

@Named("localeDAO")

@javax.enterprise.context.Dependent
public class LocaleDAO extends AbstractDAOImpl<HLocale, Long> {

    public LocaleDAO() {
        super(HLocale.class);
    }

    public LocaleDAO(Session session) {
        super(HLocale.class, session);
    }

    public @Nullable
    HLocale findByLocaleId(@Nonnull LocaleId locale) {
        return (HLocale) getSession().byNaturalId(HLocale.class)
                .using("localeId", locale).load();
    }

    @SuppressWarnings("unchecked")
    public List<HLocale> findBySimilarLocaleId(LocaleId localeId) {
        return (List<HLocale>) getSession()
                .createQuery("from HLocale l where lower(l.localeId) = :id ")
                .setString("id", localeId.getId().toLowerCase())
                .setComment("LocaleDAO.findBySimilarLocaleId").list();
    }

    public List<HLocale> findAllActive() {
        return findByCriteria(Restrictions.eq("active", true));
    }

    public List<HLocale> findAllActiveAndEnabledByDefault() {
        return findByCriteria(Restrictions.eq("active", true),
                Restrictions.eq("enabledByDefault", true));
    }

    public List<HLocale> findAll() {
        return findByCriteria(); // Return all of them
    }
}
