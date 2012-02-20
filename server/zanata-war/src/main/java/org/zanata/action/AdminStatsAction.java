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
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HLocale;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Name("adminStatsAction")
@Scope(ScopeType.PAGE)
public class AdminStatsAction implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Logger
   Log log;

   @In
   ProjectDAO projectDAO;

   @In
   ProjectIterationDAO projectIterationDAO;

   @In
   PersonDAO personDAO;

   @In
   TextFlowDAO textFlowDAO;

   @In
   TextFlowTargetDAO textFlowTargetDAO;

   public int getTotalProjectCount()
   {
      return projectDAO.getTotalProjectCount();
   }

   public int getTotalActiveProjectCount()
   {
      return projectDAO.getTotalActiveProjectCount();
   }

   public int getTotalReadOnlyProjectCount()
   {
      return projectDAO.getTotalReadOnlyProjectCount();
   }

   public int getTotalObsoleteProjectCount()
   {
      return projectDAO.getTotalObsoleteProjectCount();
   }

   public int getTotalProjectIterCount()
   {
      return projectIterationDAO.getTotalProjectIterCount();
   }

   public int getTotalActiveProjectIterCount()
   {
      return projectIterationDAO.getTotalActiveProjectIterCount();
   }

   public int getTotalReadOnlyProjectIterCount()
   {
      return projectIterationDAO.getTotalReadOnlyProjectIterCount();
   }

   public int getTotalObsoleteProjectIterCount()
   {
      return projectIterationDAO.getTotalObsoleteProjectIterCount();
   }

   public int getTotalTranslator()
   {
      return personDAO.getTotalTranslator();
   }

   public int getTotalWords()
   {
      return textFlowDAO.getTotalWords();
   }

   public int getTotalApprovedWords()
   {
      return textFlowTargetDAO.getTotalApprovedWords();
   }

   public int getTotalNeedReviewWords()
   {
      return textFlowTargetDAO.getTotalNeedReviewWords();
   }

   public int getTotalUntranslatedWords()
   {
      return getTotalWords() - (getTotalNeedReviewWords() + getTotalApprovedWords());
   }
}


 