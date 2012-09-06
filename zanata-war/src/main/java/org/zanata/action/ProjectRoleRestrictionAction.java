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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccountRole;
import org.zanata.model.HProject;

/**
 * This Action bean holds information about project restrictions by Role.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see {@link ProjectHome}
 * @see {@link LocaleListAction}
 */
@Name("projectRoleRestrictionAction")
@Scope(ScopeType.PAGE)
public class ProjectRoleRestrictionAction implements Serializable
{
   @In
   private AccountRoleDAO accountRoleDAO;

   @In(required = false)
   private ProjectHome projectHome;

   private Map<String, Boolean> roleRestrictions;


   @Out(required = false)
   public boolean getRestrictByRoles()
   {
      return projectHome.getInstance().isRestrictedByRoles();
   }

   public void setRestrictByRoles(boolean restrictByRoles)
   {
      projectHome.getInstance().setRestrictedByRoles(restrictByRoles);
   }

   public Map<String, Boolean> getRoleRestrictions()
   {
      if( roleRestrictions == null )
      {
         roleRestrictions = new HashMap<String, Boolean>();
         HProject project = projectHome.getInstance();

         for( HAccountRole role : project.getAllowedRoles() )
         {
            roleRestrictions.put( role.getName(), true );
         }
      }
      return roleRestrictions;
   }

   public List<HAccountRole> getAvailableRoles()
   {
      List<HAccountRole> allRoles = accountRoleDAO.findAll();

      Collections.sort( allRoles,
            new Comparator<HAccountRole>()
            {
               @Override
               public int compare(HAccountRole o1, HAccountRole o2)
               {
                  return o1.getName().compareTo( o2.getName() );
               }
            });

      return allRoles;
   }

   @Out(required = false)
   public Set<HAccountRole> getCustomizedProjectRoleRestrictions()
   {
      Set<HAccountRole> customizedRoleSet = new HashSet<HAccountRole>();
      for( String roleName : this.getRoleRestrictions().keySet() )
      {
         if( this.getRoleRestrictions().get( roleName ) )
         {
            customizedRoleSet.add( accountRoleDAO.findByName(roleName) );
         }
      }
      return customizedRoleSet;
   }


}
