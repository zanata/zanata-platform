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
package net.openl10n.flies.dao;

import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HLocale;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("localeDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LocaleDAO extends AbstractDAOImpl<HLocale, Long>
{

   public LocaleDAO()
   {
      super(HLocale.class);
   }

   public LocaleDAO(Session session)
   {
      super(HLocale.class, session);
   }

   public HLocale findByLocaleId(LocaleId locale)
   {
      return (HLocale) getSession().createCriteria(HLocale.class).add(Restrictions.naturalId().set("localeId", locale)).setCacheable(true).uniqueResult();
   }

   public List<HLocale> findAllActive()
   {
      return findByCriteria(Restrictions.eq("active", true));
   }

}
