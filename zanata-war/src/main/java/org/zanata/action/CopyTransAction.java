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

import java.util.Map;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.scope.FlashScopeBean;

import static org.zanata.model.HCopyTransOptions.ConditionRuleAction;

/**
 * Copy Trans page action bean.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("copyTransAction")
public class CopyTransAction
{

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private CopyTransManager copyTransManager;

   @In
   private FlashScopeBean flash;

   @In
   private Map<String, String> messages;

   private String iterationSlug;

   private String projectSlug;

   private String contextMismatchAction = ConditionRuleAction.DOWNGRADE_TO_FUZZY.toString();

   private String documentIdMismatchAction = ConditionRuleAction.DOWNGRADE_TO_FUZZY.toString();

   private String projectMismatchAction = ConditionRuleAction.DOWNGRADE_TO_FUZZY.toString();

   private HProjectIteration projectIteration;


   public String getIterationSlug()
   {
      return iterationSlug;
   }

   public void setIterationSlug(String iterationSlug)
   {
      this.iterationSlug = iterationSlug;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public String getContextMismatchAction()
   {
      return contextMismatchAction;
   }

   public void setContextMismatchAction(String contextMismatchAction)
   {
      this.contextMismatchAction = contextMismatchAction;
   }

   public String getDocumentIdMismatchAction()
   {
      return documentIdMismatchAction;
   }

   public void setDocumentIdMismatchAction(String documentIdMismatchAction)
   {
      this.documentIdMismatchAction = documentIdMismatchAction;
   }

   public String getProjectMismatchAction()
   {
      return projectMismatchAction;
   }

   public void setProjectMismatchAction(String projectMismatchAction)
   {
      this.projectMismatchAction = projectMismatchAction;
   }

   public HProjectIteration getProjectIteration()
   {
      if( this.projectIteration == null )
      {
         this.projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      }
      return this.projectIteration;
   }

   public void initialize()
   {
      HProject project = this.getProjectIteration().getProject();
      if( project.getDefaultCopyTransOpts() != null )
      {
         this.contextMismatchAction = project.getDefaultCopyTransOpts().getContextMismatchAction().toString();
         this.documentIdMismatchAction = project.getDefaultCopyTransOpts().getDocIdMismatchAction().toString();
         this.projectMismatchAction = project.getDefaultCopyTransOpts().getProjectMismatchAction().toString();
      }
   }

   public boolean isCopyTransRunning()
   {
      return copyTransManager.isCopyTransRunning( getProjectIteration() );
   }

   @Restrict("#{s:hasPermission(copyTransAction.projectIteration, 'copy-trans')}")
   public void startCopyTrans()
   {
      if( isCopyTransRunning() )
      {
         flash.setAttribute("message", messages.get("jsf.iteration.CopyTrans.AlreadyStarted.flash"));
         return;
      }
      else if( getProjectIteration().getDocuments().size() <= 0 )
      {
         flash.setAttribute("message", messages.get("jsf.iteration.CopyTrans.NoDocuments"));
         return;
      }

      // Options
      HCopyTransOptions options = new HCopyTransOptions();
      options.setProjectMismatchAction( ConditionRuleAction.valueOf( this.projectMismatchAction ) );
      options.setContextMismatchAction( ConditionRuleAction.valueOf( this.contextMismatchAction ) );
      options.setDocIdMismatchAction( ConditionRuleAction.valueOf( this.documentIdMismatchAction ) );

      copyTransManager.startCopyTrans( getProjectIteration(), options );
      flash.setAttribute("message", messages.get("jsf.iteration.CopyTrans.Started"));
   }

   public void cancel()
   {
      // Simply navigate where needed
   }
}
