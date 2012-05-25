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
package org.zanata.service.impl;

import java.util.List;

import org.hibernate.ScrollableResults;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.Transaction;
import org.zanata.common.ContentState;
import org.zanata.dao.DatabaseConstants;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.service.TranslatedDocResourceService;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;

//TODO unit test suite for this class

@Name("copyTransServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class CopyTransServiceImpl implements CopyTransService
{
   @In
   private LocaleService localeServiceImpl;
   
   @In
   private TextFlowTargetDAO textFlowTargetDAO;
   
   @In
   private DocumentDAO documentDAO;
   
   @Logger
   Log log;


   @Observer(TranslatedDocResourceService.EVENT_COPY_TRANS)
   public void execute(Long docId, String project, String iterationSlug)
   {
      HDocument document = documentDAO.findById(docId, true);
      log.info("copyTrans start: document \"{0}\"", document.getDocId());
      List<HLocale> localelist = localeServiceImpl.getSupportedLangugeByProjectIteration(project, iterationSlug);

      // TODO iterate over document's textflows, then call copyTransForTextFlow(textFlow, localeList)
      // refer patch from https://bugzilla.redhat.com/show_bug.cgi?id=746899
      for (HLocale locale : localelist)
      {
         copyTransForLocale(document, locale);
      }
      log.info("copyTrans finished: document \"{0}\"", document.getDocId());
   }

   private String createComment(HTextFlowTarget target)
   {
      String author;
      HDocument document = target.getTextFlow().getDocument();
      String projectname = document.getProjectIteration().getProject().getName();
      String version = document.getProjectIteration().getSlug();
      String documentid = document.getDocId();
      if (target.getLastModifiedBy() != null)
      {
         author = ", author " + target.getLastModifiedBy().getName();
      }
      else
      {
         author = "";
      }

      return "translation auto-copied from project " + projectname + ", version " + version + ", document " + documentid + author;
   }

   // TODO unit testing for this method. Reduce complexity(pass cyclomatic complexity analysis)
   @Override
   public void copyTransForLocale(HDocument document, HLocale locale)
   {
      ScrollableResults results = null;
      
      try
      {
         int copyCount = 0;
         int rowCount = 0;
         
         results = textFlowTargetDAO.findLatestEquivalentTranslations(document, locale);
         
         while (results.next())
         {
            rowCount++;
            
            final HTextFlowTarget oldTFT = (HTextFlowTarget)results.get(0);
            final HTextFlow textFlow = (HTextFlow)results.get(1);
            HTextFlowTarget hTarget = textFlow.getTargets().get( locale ); 
            
            if (hTarget != null && hTarget.getState() == ContentState.Approved)
               continue;
            
            if (hTarget == null)
            {
               hTarget = new HTextFlowTarget(textFlow, locale);
               hTarget.setVersionNum(1);
               textFlow.getTargets().put(locale, hTarget);
            }
            else
            {
               // DB trigger will copy old value to history table, if we
               // change the versionNum
               hTarget.setVersionNum(hTarget.getVersionNum() + 1);
            }
            // NB we don't touch creationDate
            hTarget.setTextFlowRevision(textFlow.getRevision());
            hTarget.setLastChanged(oldTFT.getLastChanged());
            hTarget.setLastModifiedBy(oldTFT.getLastModifiedBy());
            hTarget.setContents(oldTFT.getContents());
            hTarget.setState(oldTFT.getState());
            HSimpleComment hcomment = hTarget.getComment();
            if (hcomment == null)
            {
               hcomment = new HSimpleComment();
               hTarget.setComment(hcomment);
            }
            hcomment.setComment(createComment(oldTFT));
            textFlowTargetDAO.makePersistent(hTarget);
            ++copyCount;
            
            // manually flush
            if( rowCount % DatabaseConstants.BATCH_SIZE == 0 )
            {
               textFlowTargetDAO.flush();
               textFlowTargetDAO.clear();
            }
         }

         log.info("copyTrans: {0} {1} translations for document \"{2}{3}\" ", copyCount, locale.getLocaleId(), document.getPath(), document.getName());
      }
      catch (Exception e)
      {
         log.warn("exception during copy trans", e);
         try
         {
            Transaction.instance().rollback();
         }
         catch (Exception i)
         {
            log.warn("exception during transaction rollback", i);
         }
      }
      finally
      {
         if( results != null )
         {
            results.close();
         }
      }
   }
   
   @Override
   public void copyTransForDocument(HDocument document)
   {
      log.info("copyTrans start: document \"{0}\"", document.getDocId());
      List<HLocale> localeList =
            localeServiceImpl.getSupportedLangugeByProjectIteration(document.getProjectIteration().getProject().getSlug(),
                  document.getProjectIteration().getSlug());

      for (HLocale locale : localeList)
      {
         copyTransForLocale(document, locale);
      }
      log.info("copyTrans finished: document \"{0}\"", document.getDocId());
   }

}
