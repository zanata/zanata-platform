/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HLocale;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Name("glossaryStatsAction")
@Scope(ScopeType.PAGE)
public class GlossaryStatsAction implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Logger
   Log log;

   @In
   GlossaryDAO glossaryDAO;

   public List<Status> getAllStatus()
   {
      List<Status> result = new ArrayList<Status>();
      Map<String, Integer> statsMap = new HashMap<String, Integer>();

      List<HGlossaryEntry> entries = glossaryDAO.getEntries();
      for (HGlossaryEntry entry : entries)
      {
         for (HLocale localeKey : entry.getGlossaryTerms().keySet())
         {
            int count = 0;
            if (statsMap.containsKey(localeKey.getLocaleId().getId()))
            {
               count = statsMap.get(localeKey.getLocaleId().getId());
            }
            statsMap.put(localeKey.getLocaleId().getId(), count + 1);
         }
      }
      
      for (Entry<String, Integer> entry : statsMap.entrySet())
      {
         result.add(new Status(entry.getKey(), entry.getValue()));
      }

      Collections.sort(result);
      return result;
   }
   
   public static class Status implements Comparable<Status>
   {
      private String locale;
      private int entryCount;

      public Status(String locale, int entryCount)
      {
         this.locale = locale;
         this.entryCount = entryCount;
      }

      public String getLocale()
      {
         return locale;
      }

      public int getEntryCount()
      {
         return entryCount;
      }

      @Override
      public int compareTo(Status o)
      {
         if (o.getEntryCount() == this.getEntryCount())
         {
            return 0;
         }
         return o.getEntryCount() > this.getEntryCount() ? 1 : -1;
      }
   }
}


 