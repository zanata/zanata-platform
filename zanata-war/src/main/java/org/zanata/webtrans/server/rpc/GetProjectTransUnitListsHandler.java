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
package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitLists;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitListsResult;
import com.ibm.icu.lang.UCharacter;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @see GetProjectTransUnitLists
 * @author David Mason, damason@redhat.com
 */
@Name("webtrans.gwt.GetProjectTransUnitListsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetProjectTransUnitLists.class)
public class GetProjectTransUnitListsHandler extends AbstractActionHandler<GetProjectTransUnitLists, GetProjectTransUnitListsResult>
{

   @Logger
   Log log;

   @In
   private LocaleService localeServiceImpl;

   @In
   private TextFlowSearchService textFlowSearchServiceImpl;

   @In
   private TransUnitTransformer transUnitTransformer;

   @Override
   public GetProjectTransUnitListsResult execute(GetProjectTransUnitLists action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();
      log.info("Searching all targets for workspace {0}", action.getWorkspaceId().toString());

      HashMap<Long, List<TransUnit>> matchingTUs = new HashMap<Long, List<TransUnit>>();
      HashMap<Long, String> docPaths = new HashMap<Long, String>();
      if ((action.getSearchString() == null || action.getSearchString().isEmpty()))
      {
         // TODO empty searches shouldn't be requested, consider replacing this
         // with an error, or making behaviour return all targets for the
         // project (consider performance).
         return new GetProjectTransUnitListsResult(docPaths, matchingTUs);
      }

      // TODO handle exception thrown by search service
      List<HTextFlowTarget> matchingFlows = textFlowSearchServiceImpl.findTextFlowTargets(action.getWorkspaceId(), FilterConstraints.filterBy(action.getSearchString()).ignoreSource().excludeNew().caseSensitive(action.isCaseSensitive()));
      log.info("Returned {0} results for search", matchingFlows.size());

      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e);
      }

      //FIXME remove when analyzer handles leading & trailing whitespace
      boolean needsWhitespaceCheck = !action.getSearchString().equals(action.getSearchString().trim());
      String searchString = action.getSearchString();
      if (!action.isCaseSensitive())
      {
         searchString = foldCase(searchString);
      }


      for (HTextFlowTarget htft : matchingFlows)
      {
         // FIXME temporary check for leading and trailing whitespace to compensate
         // for NGramAnalyzer trimming strings before tokenization. This should
         // be removed when updating to a lucene version with the whitespace
         // issue resolved.
         if (needsWhitespaceCheck)
         {
            boolean whitespaceMatch = false;
            for (String content : htft.getContents())
            {
               String contentStr = content;
               if (!action.isCaseSensitive())
               {
                  contentStr = foldCase(contentStr);
               }
               if (contentStr.contains(searchString))
               {
                  whitespaceMatch = true;
                  break;
               }
            }
            if (!whitespaceMatch)
            {
               continue;
            }
         }

         HTextFlow htf = htft.getTextFlow();
         List<TransUnit> listForDoc = matchingTUs.get(htf.getDocument().getId());
         if (listForDoc == null)
         {
            listForDoc = new ArrayList<TransUnit>();
         }

         TransUnit transUnit = transUnitTransformer.transform(htf, hLocale);
         listForDoc.add(transUnit);
         matchingTUs.put(htf.getDocument().getId(), listForDoc);
         docPaths.put(htf.getDocument().getId(), htf.getDocument().getDocId());
      }

      return new GetProjectTransUnitListsResult(docPaths, matchingTUs);
   }

   private String foldCase(String original)
   {
      char[] buffer = original.toCharArray();
      for (int i=0; i<buffer.length; i++)
      {
         buffer[i] = (char) UCharacter.foldCase(buffer[i], true);
      }
      return new String(buffer);
   }

   @Override
   public void rollback(GetProjectTransUnitLists action, GetProjectTransUnitListsResult result, ExecutionContext context) throws ActionException
   {
   }

}