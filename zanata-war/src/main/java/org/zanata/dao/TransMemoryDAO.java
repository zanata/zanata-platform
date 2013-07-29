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

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.exception.EntityMissingException;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemory;

import com.google.common.base.Optional;

/**
 * Data Access Object for Translation Memory and related entities.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("transMemoryDAO")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class TransMemoryDAO extends AbstractDAOImpl<TransMemory, Long>
{
   public TransMemoryDAO()
   {
      super(TransMemory.class);
   }

   public TransMemoryDAO(Session session)
   {
      super(TransMemory.class, session);
   }

   public Optional<TransMemory> getBySlug(@Nonnull String slug)
   {
      if(!StringUtils.isEmpty(slug))
      {
         TransMemory tm = (TransMemory) getSession().byNaturalId(TransMemory.class).using("slug", slug).load();
         return Optional.fromNullable(tm);
      }
      return Optional.absent();
   }

   public void deleteTransMemoryContents(@Nonnull String slug)
   {
      Optional<TransMemory> tm = getBySlug(slug);
      if (!tm.isPresent())
      {
         throw new EntityMissingException("Translation memory " + slug + " was not found.");
      }
      Iterator it = tm.get().getTranslationUnits().iterator();
      while(it.hasNext())
      {
         getSession().delete(it.next());
         it.remove();
      }
      makePersistent(tm.get());
   }

   public @Nullable
   TransMemoryUnit findTranslationUnit(@Nonnull String tmSlug, @Nonnull String uniqueId)
   {

      List results = getSession()
            .createQuery("from TransMemoryUnit tu where tu.uniqueId = :uniqueId and tu.translationMemory.slug = :tmSlug")
            .setString("uniqueId", uniqueId)
            .setString("tmSlug", tmSlug)
            .list();
      if( results.size() > 0 )
      {
         return (TransMemoryUnit)results.get(0);
      }
      return null;
   }
}
