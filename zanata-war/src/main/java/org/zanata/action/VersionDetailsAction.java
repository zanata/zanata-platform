/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.annotation.CachedMethods;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Name("versionDetailsAction")
@Scope(ScopeType.PAGE)
@CachedMethods
public class VersionDetailsAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In
   private LocaleService localeServiceImpl;

   @In
   private PersonDAO personDAO;

   @In
   private ZanataIdentity identity;

   private String projectSlug;

   @CachedMethodResult
   public List<HLocale> getSupportedLocaleList(String versionSlug)
   {
      List<HLocale> result = new ArrayList<HLocale>();
      List<HLocale> localeList = localeServiceImpl.getSupportedLangugeByProjectIteration(projectSlug, versionSlug);
      HPerson person = personDAO.findByUsername(identity.getCredentials().getUsername());
      for (HLocale locale : localeList)
      {
         if (isPersonInTeam(locale, person.getId()))
         {
            result.add(locale);
         }
      }
      return result;
   }


   private boolean isPersonInTeam(HLocale locale, final Long personId)
   {
      for (HLocaleMember lm : locale.getMembers())
      {
         if (lm.getPerson().getId().equals(personId))
         {
            return true;
         }
      }
      return false;
   }


   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public String editVersion()
   {
      return "/iteration/edit.xhtml";
   }

   public String sourceDocs()
   {
      return "/iteration/source_files.xhtml";
   }
}
