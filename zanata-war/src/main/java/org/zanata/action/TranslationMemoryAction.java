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
package org.zanata.action;

import java.util.List;

import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityHome;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.model.tm.TransMemory;
import org.zanata.service.SlugEntityService;

import lombok.Getter;
import lombok.Setter;

/**
 * Controller class for the Translation Memory UI.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("translationMemoryAction")
public class TranslationMemoryAction extends EntityHome<TransMemory>
{
   @In
   private TransMemoryDAO transMemoryDAO;

   @In
   private SlugEntityService slugEntityServiceImpl;

   private List<TransMemory> transMemoryList;

   @Getter @Setter
   private String slug;

   public List<TransMemory> getAllTranslationMemories()
   {
      if( transMemoryList == null )
      {
         transMemoryList = transMemoryDAO.findAll();
      }
      return transMemoryList;
   }

   public boolean verifySlugAvailable()
   {
      return slugEntityServiceImpl.isSlugAvailable(slug, TransMemory.class);
   }

   public void cancel()
   {
      // Navigation logic in pages.xml
   }
}
