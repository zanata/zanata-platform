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

import javax.validation.constraints.NotNull;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityHome;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.RoleAssignmentRuleDAO;
import org.zanata.model.HRoleAssignmentRule;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("roleAssignmentRuleAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasRole('admin')}")
public class RoleAssignmentRuleAction extends EntityHome<HRoleAssignmentRule>
{
   @In
   private RoleAssignmentRuleDAO roleAssignmentRuleDAO;

   @In
   private AccountRoleDAO accountRoleDAO;


   public List<HRoleAssignmentRule> getAllRules()
   {
      return roleAssignmentRuleDAO.findAll();
   }

   public void edit( HRoleAssignmentRule rule )
   {
      this.setInstance(rule);
   }

   public void remove( HRoleAssignmentRule rule )
   {
      roleAssignmentRuleDAO.makeTransient(rule);
   }

   public void setRoleToAssign( String roleName )
   {
      this.getInstance().setRoleToAssign( accountRoleDAO.findByName(roleName) );
   }

   @NotNull
   public String getRoleToAssign()
   {
      if( this.getInstance() == null || this.getInstance().getRoleToAssign() == null )
      {
         return null;
      }
      else
      {
         return this.getInstance().getRoleToAssign().getName();
      }
   }

}
