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
import javax.faces.event.ValueChangeEvent;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.model.tm.TransMemory;
import org.zanata.service.SlugEntityService;

/**
 * Controller class for the Translation Memory UI.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("translationMemoryAction")
@Restrict("#{s:hasRole('admin')}")
public class TranslationMemoryAction extends EntityHome<TransMemory>
{
   @In
   private TransMemoryDAO transMemoryDAO;

   @In
   private SlugEntityService slugEntityServiceImpl;

   private List<TransMemory> transMemoryList;

   public List<TransMemory> getAllTranslationMemories()
   {
      if( transMemoryList == null )
      {
         transMemoryList = transMemoryDAO.findAll();
      }
      return transMemoryList;
   }

   public void verifySlugAvailable(ValueChangeEvent e)
   {
      String slug = (String) e.getNewValue();
      validateSlug(slug, e.getComponent().getId());
   }

   public boolean validateSlug(String slug, String componentId)
   {
      if (!slugEntityServiceImpl.isSlugAvailable(slug, TransMemory.class))
      {
         FacesMessages.instance().addToControl(componentId, "This Id is not available");
         return false;
      }
      return true;
   }

   public void clearTransMemory(String transMemorySlug)
   {
      transMemoryDAO.deleteTransMemoryContents(transMemorySlug);
      transMemoryList = null; // Force refresh next time list is requested
   }

   public String cancel()
   {
      // Navigation logic in pages.xml
      return "cancel";
   }
}
