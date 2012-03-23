/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.search.FilterConstraints;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.util.TextFlowFilter;
import org.zanata.webtrans.shared.util.TextFlowFilterImpl;

/**
 * @author David Mason, damason@redhat.com
 */
@Name("textFlowSearchServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowSearchServiceImpl implements TextFlowSearchService
{


   @Logger
   Log log;

   @In
   TextFlowDAO textFlowDAO;
   
   @In
   private LocaleService localeServiceImpl;

   @In
   DocumentDAO documentDAO;
   
   
   @Override
   public List<HTextFlow> findTextFlows(WorkspaceId workspace, String searchTerm, FilterConstraints constraints)
   {
      LocaleId localeId = workspace.getLocaleId();
      String projectSlug = workspace.getProjectIterationId().getProjectSlug();
      String iterationSlug = workspace.getProjectIterationId().getIterationSlug();

      //TODO decide whether null or empty searchTerm is valid.
      //allowing it to get all may simplify jobs, but may be clearer
      //to use a different class for gets vs. filters.
      //On the other hand, this service could use a getter service
      //for its pre-filter retrieval, although it will likely be
      //doing its own querying eventually...

      //check that locale is valid for the workspace
      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(localeId, projectSlug, iterationSlug);
      }
      catch (ZanataServiceException e)
      {
         throw new ZanataServiceException("Failed to validate locale", e);
      }

      List<HDocument> documents = documentDAO.getAllByProjectIteration(projectSlug, iterationSlug);

      TextFlowFilter filter = new TextFlowFilterImpl(constraints);

      List<HTextFlow> allMatchingResults = new ArrayList<HTextFlow>();

      for (HDocument doc : documents)
      {
         log.warn("Inefficient fetch TransUnits:" + searchTerm);
         List<HTextFlow> result = textFlowDAO.getTransUnitList(doc.getId());

         for (HTextFlow textFlow : result)
         {
            if (!filter.isFilterOut(textFlow, hLocale))
            {
               allMatchingResults.add(textFlow);
            }
         }
      }

      return allMatchingResults;
   }

   
   @Override
   public List<HTextFlow> findTextFlows(WorkspaceId workspace, DocumentId doc, String searchTerm, FilterConstraints constraints)
   {
      // TODO Auto-generated method stub
      return null;
   }
}
