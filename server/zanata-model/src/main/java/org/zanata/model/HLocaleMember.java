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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author camunoz@redhat.com
 *
 */
@Entity
@Table(name="HLocale_Member")
@Setter
@NoArgsConstructor
public class HLocaleMember implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private HLocaleMemberPk id = new HLocaleMemberPk();
   
   private boolean isCoordinator;
   
   public HLocaleMember( HPerson person, HLocale supportedLanguage, boolean isCoordinator )
   {
      id.setPerson(person);
      id.setSupportedLanguage(supportedLanguage);
      setCoordinator(isCoordinator);
   }
   
   @EmbeddedId
   protected HLocaleMemberPk getId()
   {
      return id;
   }
   
   protected void setId(HLocaleMemberPk id)
   {
      this.id = id;
   }

   @Column(name="isCoordinator")
   public boolean isCoordinator()
   {
      return isCoordinator;
   }

   @Transient
   public HPerson getPerson()
   {
      return id.getPerson();
   }

   @Transient
   public HLocale getSupportedLanguage()
   {
      return id.getSupportedLanguage();
   }

   @Embeddable
   @Setter
   @AllArgsConstructor
   @NoArgsConstructor
   public static class HLocaleMemberPk implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private HPerson person;
      private HLocale supportedLanguage;

      @ManyToOne(fetch=FetchType.EAGER, optional=false)
      @JoinColumn(name="personId", nullable=false)
      public HPerson getPerson()
      {
         return person;
      }

      @ManyToOne(fetch=FetchType.EAGER, optional=false)
      @JoinColumn(name="supportedLanguageId")
      public HLocale getSupportedLanguage()
      {
         return supportedLanguage;
      }

   }
}
