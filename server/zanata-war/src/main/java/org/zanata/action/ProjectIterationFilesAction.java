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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;

@Name("projectIterationFilesAction")
@Scope(ScopeType.PAGE)
public class ProjectIterationFilesAction
{

   @RequestParameter("project")
   private String projectSlug;
   
   @RequestParameter("iteration")
   private String iterationSlug;
   
   @RequestParameter
   private String localeId;
   
   @In
   private DocumentDAO documentDAO;
   
   private List<HDocument> iterationDocuments;
   
   
   @Create
   public void initialize()
   {
      this.iterationDocuments = this.documentDAO.getAllByProjectIterationAndLocale(this.projectSlug, this.iterationSlug, 
            new LocaleId(this.localeId));
   }

   public List<HDocument> getIterationDocuments()
   {
      return iterationDocuments;
   }

   public void setIterationDocuments(List<HDocument> iterationDocuments)
   {
      this.iterationDocuments = iterationDocuments;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public String getIterationSlug()
   {
      return iterationSlug;
   }

   public void setIterationSlug(String iterationSlug)
   {
      this.iterationSlug = iterationSlug;
   }

   public String getLocaleId()
   {
      return localeId;
   }

   public void setLocaleId(String localeId)
   {
      this.localeId = localeId;
   }
   
}
