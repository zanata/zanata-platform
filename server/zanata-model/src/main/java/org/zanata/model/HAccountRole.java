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
package org.zanata.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.jboss.seam.annotations.security.management.RoleConditional;
import org.jboss.seam.annotations.security.management.RoleGroups;
import org.jboss.seam.annotations.security.management.RoleName;

@Entity
public class HAccountRole implements Serializable
{
   private static final long serialVersionUID = 9177366120789064801L;

   private Integer id;
   private String name;
   private boolean conditional;

   private Set<HAccountRole> groups;

   @Id
   @GeneratedValue
   public Integer getId()
   {
      return id;
   }

   public void setId(Integer id)
   {
      this.id = id;
   }

   @RoleName
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @RoleGroups
   @ManyToMany(targetEntity = HAccountRole.class)
   @JoinTable(name = "HAccountRoleGroup", joinColumns = @JoinColumn(name = "roleId"), inverseJoinColumns = @JoinColumn(name = "memberOf"))
   public Set<HAccountRole> getGroups()
   {
      return groups;
   }

   public void setGroups(Set<HAccountRole> groups)
   {
      this.groups = groups;
   }

   @RoleConditional
   public boolean isConditional()
   {
      return conditional;
   }

   public void setConditional(boolean conditional)
   {
      this.conditional = conditional;
   }
}
