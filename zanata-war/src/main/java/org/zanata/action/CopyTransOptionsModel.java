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

import java.io.Serializable;
import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HCopyTransOptions;

/**
 * Holds a {@link org.zanata.model.HCopyTransOptions} model object.
 * This component is intended for use within other components that need to keep a
 * copy of a CopyTransOptions entity, although it may be accessed directly as well.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("copyTransOptionsModel")
@Scope(ScopeType.PAGE)
@AutoCreate
public class CopyTransOptionsModel implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In
   private EntityManager entityManager;

   private HCopyTransOptions instance;


   public HCopyTransOptions getInstance()
   {
      if( instance == null )
      {
         instance = new HCopyTransOptions();
      }
      return instance;
   }

   public void setInstance(HCopyTransOptions instance)
   {
      this.instance = instance;
   }

   public String getProjectMismatchAction()
   {
      return getInstance().getProjectMismatchAction().toString();
   }

   public void setProjectMismatchAction(String projectMismatchAction)
   {
      getInstance().setProjectMismatchAction(HCopyTransOptions.ConditionRuleAction.valueOf(projectMismatchAction));
   }

   public String getDocIdMismatchAction()
   {
      return getInstance().getDocIdMismatchAction().toString();
   }

   public void setDocIdMismatchAction(String docIdMismatchAction)
   {
      getInstance().setDocIdMismatchAction(HCopyTransOptions.ConditionRuleAction.valueOf(docIdMismatchAction));
   }

   public String getContextMismatchAction()
   {
      return getInstance().getContextMismatchAction().toString();
   }

   public void setContextMismatchAction(String contextMismatchAction)
   {
      getInstance().setContextMismatchAction(HCopyTransOptions.ConditionRuleAction.valueOf(contextMismatchAction));
   }

   public void save()
   {
      this.setInstance( entityManager.merge(this.getInstance()) );
   }
}
