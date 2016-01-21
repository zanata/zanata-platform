/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

/**
 * Repository for the HLocale entity.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Repository
public abstract class LocaleRepository
        extends AbstractEntityRepository<HLocale, Long>
        implements Serializable {

    public abstract List<HLocale> findByActiveEqual(boolean active);

    public abstract HLocale findOptionalByLocaleId(LocaleId localeId);

    public List<HLocale> findByActive() {
        return findByActiveEqual(true);
    }

    public HLocale findByLocaleId(LocaleId localeId) {
        return findOptionalByLocaleId(localeId);
    }

    public List<HLocale> findAllActiveAndEnabledByDefault() {
        // TODO To get rid of some of this boilerplate code and/or use
        // DeltaSpike's criteria API, we might need to activate JPA metamodel
        // generation
        CriteriaQuery<HLocale> criteria = criteriaQuery();
        CriteriaBuilder cb =
                entityManager().getEntityManagerFactory()
                        .getCriteriaBuilder();
        Root<HLocale> root = criteria.from(entityClass());
        criteria.where(
                cb.and(
                        cb.equal(root.get("active"), true),
                        cb.equal(root.get("enabledByDefault"), true)
                )
        );

        return entityManager().createQuery(criteria).getResultList();
    }
}
