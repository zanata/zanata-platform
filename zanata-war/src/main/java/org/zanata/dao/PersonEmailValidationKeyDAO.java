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
package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import javax.inject.Named;
import org.zanata.model.HPersonEmailValidationKey;

@Named("personEmailValidationKeyDAO")

@javax.enterprise.context.Dependent
public class PersonEmailValidationKeyDAO extends
        AbstractDAOImpl<HPersonEmailValidationKey, Long> {

    public PersonEmailValidationKeyDAO() {
        super(HPersonEmailValidationKey.class);
    }

    public PersonEmailValidationKeyDAO(Session session) {
        super(HPersonEmailValidationKey.class, session);
    }

    public HPersonEmailValidationKey findByKeyHash(String keyHash) {
        Query query =
                getSession()
                        .createQuery(
                                "from HPersonEmailValidationKey as key where key.keyHash= :keyHash");
        query.setParameter("keyHash", keyHash);
        query.setCacheable(false);
        query.setComment("PersonEmailValidationKeyDAO.findByKeyHash");
        return (HPersonEmailValidationKey) query.uniqueResult();
    }

    public HPersonEmailValidationKey findByPersonId(Long personId) {
        Query query =
                getSession()
                        .createQuery(
                                "from HPersonEmailValidationKey as key where key.person.id= :personId");
        query.setLong("personId", personId);
        query.setCacheable(false);
        query.setComment("PersonEmailValidationKeyDAO.findByPersonId");
        return (HPersonEmailValidationKey) query.uniqueResult();
    }
}
