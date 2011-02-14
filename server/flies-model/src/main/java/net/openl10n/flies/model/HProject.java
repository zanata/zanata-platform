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
package net.openl10n.flies.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import net.openl10n.flies.rest.dto.Project;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @see Project
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "projecttype", discriminatorType = DiscriminatorType.STRING)
@Restrict
public abstract class HProject extends AbstractSlugEntity implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String name;
   private String description;
   private String homeContent;

   private Set<HPerson> maintainers;

   @Length(max = 80)
   @NotEmpty
   @Field(index = Index.TOKENIZED)
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @Length(max = 100)
   @Field(index = Index.TOKENIZED)
   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   @Type(type = "text")
   public String getHomeContent()
   {
      return homeContent;
   }

   public void setHomeContent(String homeContent)
   {
      this.homeContent = homeContent;
   }

   @ManyToMany
   @JoinTable(name = "HProject_Maintainer", joinColumns = @JoinColumn(name = "projectId"), inverseJoinColumns = @JoinColumn(name = "personId"))
   public Set<HPerson> getMaintainers()
   {
      if (maintainers == null)
         maintainers = new HashSet<HPerson>();
      return maintainers;
   }

   public void setMaintainers(Set<HPerson> maintainers)
   {
      this.maintainers = maintainers;
   }

   @Override
   public String toString()
   {
      return super.toString() + "[name=" + name + "]";
   }

}
