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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;
import org.zanata.common.ContentState;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.service.TranslationResourcesService;
import org.zanata.service.LocaleService;

@Name("copyTransService")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class CopyTransService
{
   @In
   private LocaleService localeServiceImpl;
   @In
   private TextFlowTargetDAO textFlowTargetDAO;
   @In
   private DocumentDAO documentDAO;
   @Logger
   Log log;


   @Observer(TranslationResourcesService.EVENT_COPY_TRANS)
   public void execute(Long docId, String project, String iterationSlug)
   {
      log.info("start copy trans for: " + docId);
      List<HLocale> localelist = localeServiceImpl.getSupportedLangugeByProjectIteration(project, iterationSlug);
      HDocument document = documentDAO.findById(docId, true);

      for (HLocale locale : localelist)
      {
         log.info("locale:" + locale.getLocaleId().getId());
         copyTransForLocale(document, locale);
      }
   }

   private String createComment(HTextFlowTarget target)
   {
      String authorname;
      HDocument document = target.getTextFlow().getDocument();
      String projectname = document.getProjectIteration().getProject().getName();
      String version = document.getProjectIteration().getSlug();
      String documentid = document.getDocId();
      if (target.getLastModifiedBy() != null)
      {
         authorname = target.getLastModifiedBy().getName();
      }
      else
      {
         authorname = "";
      }

      return "translation auto-copied from project " + projectname + ", version " + version + ", document " + documentid + ", author " + authorname;
   }

   private void copyTransForLocale(HDocument document, HLocale locale)
   {
      try
      {
         Transaction.instance().begin();
         int copyCount = 0;
         for (HTextFlow textFlow : document.getTextFlows())
         {
            HTextFlowTarget hTarget = textFlow.getTargets().get(locale);
            if (hTarget != null && hTarget.getState() == ContentState.Approved)
               continue;
            HTextFlowTarget oldTFT = textFlowTargetDAO.findLatestEquivalentTranslation(textFlow, locale);
            if (oldTFT != null)
            {
               log.info("find one:" + oldTFT.getContent());
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
               hTarget.setLastChanged(oldTFT.getLastChanged());
               hTarget.setLastModifiedBy(oldTFT.getLastModifiedBy());
               hTarget.setContent(oldTFT.getContent());
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
            }
         }
         textFlowTargetDAO.flush();
         Transaction.instance().commit();

         log.info("copied {0} existing translations for document \"{1}{2}\" ", copyCount, document.getPath(), document.getName());

      }
      catch (Exception e)
      {
         log.warn(e, e);
         try
         {
            Transaction.instance().rollback();
         }
         catch (Exception i)
         {
            log.warn(i, i);
         }
      }
   }

}
