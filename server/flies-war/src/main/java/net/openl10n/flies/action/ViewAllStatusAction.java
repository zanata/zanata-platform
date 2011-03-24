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
package net.openl10n.flies.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.TransUnitWords;
import net.openl10n.flies.dao.ProjectIterationDAO;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("viewAllStatusAction")
@Scope(ScopeType.PAGE)
public class ViewAllStatusAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @Logger
   Log log;
   private String iterationSlug;
   private String projectSlug;
   @In
   ProjectIterationDAO projectIterationDAO;
   @In
   LocaleService localeServiceImpl;

   public static class Status
   {
      private String locale;
      private String nativeName;
      private TransUnitWords words;
      private int per;

      public Status(String locale, String nativeName, TransUnitWords words, int per)
      {
         this.locale = locale;
         this.nativeName = nativeName;
         this.words = words;
         this.per = per;
      }

      public String getLocale()
      {
         return locale;
      }

      public String getNativeName()
      {
         return nativeName;
      }

      public TransUnitWords getWords()
      {
         return words;
      }

      public double getPer()
      {
         return per;
      }

   }

   public void setProjectSlug(String slug)
   {
      this.projectSlug = slug;
   }

   public String getProjectSlug()
   {
      return this.projectSlug;
   }

   public void setIterationSlug(String slug)
   {
      this.iterationSlug = slug;
   }

   public String getIterationSlug()
   {
      return this.iterationSlug;
   }

   public List<Status> getAllStatus()
   {
      List<Status> result = new ArrayList<Status>();
      HProjectIteration iteration = projectIterationDAO.getBySlug(this.projectSlug, this.iterationSlug);
      Map<String, TransUnitWords> stats = projectIterationDAO.getAllWordStatsStatistics(iteration.getId());
      List<HLocale> locale = localeServiceImpl.getSupportedLangugeByProjectIteration(this.projectSlug, this.iterationSlug);
      Long total = projectIterationDAO.getTotalCountForIteration(iteration.getId());
      for (HLocale var : locale)
      {
         TransUnitWords words = stats.get(var.getLocaleId().getId());
         if (words == null)
         {
            words = new TransUnitWords();
            words.set(ContentState.New, total.intValue());

         }
         int per;
         if (total.intValue() == 0)
         {
            per = 0;
         }
         else
         {
            per = (int) Math.ceil(100 * words.getApproved() / words.getTotal());

         }
         Status op = new Status(var.getLocaleId().getId(), var.retrieveNativeName(), words, per);
         result.add(op);
      }
      return result;
   }


}
