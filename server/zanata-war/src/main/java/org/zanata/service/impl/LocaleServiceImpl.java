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
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.LocaleService;

import com.ibm.icu.util.ULocale;

/**
 * This implementation provides all the business logic related to Locale.
 * 
 */
@Name("localeServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LocaleServiceImpl implements LocaleService
{
   private LocaleDAO localeDAO;
   
   private ProjectDAO projectDAO;

   private ProjectIterationDAO projectIterationDAO;

   private PersonDAO personDAO;
   @Logger
   Log log;

   @In
   public void setLocaleDAO(LocaleDAO localeDAO)
   {
      this.localeDAO= localeDAO;
   }

   @In
   public void setProjectDAO(ProjectDAO projectDAO)
   {
      this.projectDAO = projectDAO;
   }

   @In
   public void setProjectIterationDAO(ProjectIterationDAO projectIterationDAO)
   {
      this.projectIterationDAO = projectIterationDAO;
   }

   @In
   public void setPersonDAO(PersonDAO personDAO)
   {
      this.personDAO = personDAO;
   }

   public List<HLocale> getAllLocales()
   {
      List<HLocale> hSupportedLanguages = localeDAO.findAll();
      if (hSupportedLanguages == null)
         return new ArrayList<HLocale>();
      return hSupportedLanguages;
   }

   public void save(LocaleId localeId)
   {
      if (localeExists(localeId))
         return;
      HLocale entity = new HLocale();
      entity.setLocaleId(localeId);
      entity.setActive(true);
      localeDAO.makePersistent(entity);
      localeDAO.flush();
   }

   public void disable(LocaleId localeId)
   {
      HLocale entity = localeDAO.findByLocaleId(localeId);
      if (entity != null)
      {
         entity.setActive(false);
         localeDAO.makePersistent(entity);
         localeDAO.flush();
      }
   }

   public List<LocaleId> getAllJavaLanguages()
   {
      ULocale[] locales = ULocale.getAvailableLocales();
      List<LocaleId> addedLocales = new ArrayList<LocaleId>();
      for (ULocale locale : locales)
      {
         String id = locale.toLanguageTag();
         LocaleId localeId = new LocaleId(id);
         addedLocales.add(localeId);
      }
      return addedLocales;
   }

   public void enable(LocaleId localeId)
   {
      HLocale entity = localeDAO.findByLocaleId(localeId);
      if (entity != null)
      {
         entity.setActive(true);
         localeDAO.makePersistent(entity);
         localeDAO.flush();
      }
   }
   
   public boolean localeExists(LocaleId locale)
   {
      HLocale entity = localeDAO.findByLocaleId(locale);
      return entity != null;
   }
   
   public List<HLocale> getSupportedLocales()
   {
      return localeDAO.findAllActive();
   }

   public boolean localeSupported(LocaleId locale)
   {
      HLocale entity = localeDAO.findByLocaleId(locale);
      return entity != null && entity.isActive();
   }

   @Override
   public HLocale validateLocaleByProjectIteration(LocaleId locale, String project, String iterationSlug) throws ZanataServiceException
   {
      List<HLocale> allList = getSupportedLangugeByProjectIteration(project, iterationSlug);
      HLocale hLocale = localeDAO.findByLocaleId(locale);
      if (hLocale == null || !hLocale.isActive())
      {
         throw new ZanataServiceException("Locale " + locale.getId() + " is not enabled on this server. Please contact admin.");
      }
      if (!allList.contains(hLocale))
      {
         throw new ZanataServiceException("Locale " + locale.getId() + " is not allowed for project " + project + " and version " + iterationSlug + ". Please contact project maintainer.");
      }
      return hLocale;
   }

   @Override
   public HLocale validateSourceLocale(LocaleId locale) throws ZanataServiceException
   {
      HLocale hLocale = getByLocaleId(locale);
      if (hLocale == null || !hLocale.isActive())
      {
         throw new ZanataServiceException("Locale " + locale.getId() + " is not enabled on this server. Please contact admin.");
      }
      return hLocale;
   }

   @Override
   public HLocale getByLocaleId(LocaleId locale)
   {
      return localeDAO.findByLocaleId(locale);
   }

   @Override
   public List<HLocale> getSupportedLangugeByProjectIteration(String project, String iterationSlug)
   {
      HProjectIteration iteration = projectIterationDAO.getBySlug(project, iterationSlug);
      if (iteration.getOverrideLocales())
      {
         return new ArrayList<HLocale>(iteration.getCustomizedLocales());
      }
      HProject proj = projectDAO.getBySlug(project);
      if (proj.getOverrideLocales())
      {
         return new ArrayList<HLocale>(proj.getCustomizedLocales());
      }
      return localeDAO.findAllActive();
   }
   
   @Override
   public List<HLocale> getTranslation(String project, String iterationSlug, String username)
   {
      List<HLocale> allList = getSupportedLangugeByProjectIteration(project, iterationSlug);

      List<HLocale> member = personDAO.getLanguageMembershipByUsername(username);
      member.retainAll(allList);
      return member;
   }

   private String getDescript(HLocale op)
   {
      return op.retrieveDisplayName() + " [" + op.getLocaleId().getId() + "] " + op.retrieveNativeName();
   }

   @Override
   public Map<String, String> getGlobalLocaleItems()
   {
      Map<String, String> globalItems = new TreeMap<String, String>();
      List<HLocale> locale = getSupportedLocales();
      for (HLocale op : locale)
      {
         String name = getDescript(op);
         globalItems.put(name, name);
      }
      return globalItems;
   }

   @Override
   public Map<String, String> getIterationGlobalLocaleItems(String projectSlug)
   {
      log.info("start getIterationGlobalLocaleItems for:" + projectSlug);
      HProject project = projectDAO.getBySlug(projectSlug);
      return project.getOverrideLocales() ? getCustomizedLocalesItems(projectSlug) : getGlobalLocaleItems();
   }

   @Override
   public Map<String, String> getCustomizedLocalesItems(String projectSlug)
   {
      Map<String, String> customizedItems = new TreeMap<String, String>();
      HProject project = projectDAO.getBySlug(projectSlug);
      if (project != null && project.getOverrideLocales())
      {
         Set<HLocale> locales = project.getCustomizedLocales();
         for (HLocale op : locales)
         {
            String name = getDescript(op);
            customizedItems.put(name, name);
         }
      }
      return customizedItems;
   }

   @Override
   public Map<String, String> getIterationCustomizedLocalesItems(String projectSlug, String iterationSlug)
   {
      Map<String, String> customizedItems = new TreeMap<String, String>();
      HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      if (iteration != null && iteration.getOverrideLocales())
      {
         Set<HLocale> locales = iteration.getCustomizedLocales();
         for (HLocale op : locales)
         {
            String name = getDescript(op);
            customizedItems.put(name, name);
         }
      }
      return customizedItems;
   }

   @Override
   public Set<HLocale> convertCustomizedLocale(Map<String, String> var)
   {
      Set<HLocale> result = new HashSet<HLocale>();
      for (String op : var.keySet())
      {
         String[] list = op.split("\\[");
         String seVar = list[1].split("\\]")[0];
         HLocale entity = localeDAO.findByLocaleId(new LocaleId(seVar));
         result.add(entity);
      }
      return result;
   }

   public HLocale getSourceLocale(String projectSlug, String iterationSlug)
   {
      return localeDAO.findByLocaleId(new LocaleId("en-US"));
   }
}
