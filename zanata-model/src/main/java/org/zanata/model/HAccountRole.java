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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import javax.validation.constraints.NotNull;
import org.jboss.seam.annotations.security.management.RoleConditional;
import org.jboss.seam.annotations.security.management.RoleGroups;
import org.jboss.seam.annotations.security.management.RoleName;
import org.zanata.model.type.RoleTypeType;

import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Access(AccessType.FIELD)
@TypeDef(name = "roleType", typeClass = RoleTypeType.class)
public class HAccountRole implements Serializable
{
   private static final long serialVersionUID = 9177366120789064801L;

   @Id
   @GeneratedValue
   private Integer id;

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @RoleName
   private String name;

   @RoleConditional
   private boolean conditional;

   @Type(type = "roleType")
   @NotNull
   private RoleType roleType = RoleType.MANUAL;

   @RoleGroups
   @ManyToMany(targetEntity = HAccountRole.class)
   @JoinTable(name = "HAccountRoleGroup", joinColumns = @JoinColumn(name = "roleId"), inverseJoinColumns = @JoinColumn(name = "memberOf"))
   private Set<HAccountRole> groups = Sets.newHashSet();

   public enum RoleType
   {
      AUTO,
      MANUAL;

      public char getInitial()
      {
         return name().charAt(0);
      }

      public static RoleType valueOf(char initial)
      {
         switch (initial)
         {
            case 'A':
               return AUTO;
            case 'M':
               return MANUAL;
            default:
               throw new IllegalArgumentException(String.valueOf(initial));
         }
      }
   }

}
