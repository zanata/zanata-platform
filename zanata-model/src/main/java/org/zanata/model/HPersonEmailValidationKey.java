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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.validator.Email;
import org.hibernate.validator.NotEmpty;

@Entity
@Setter
@ToString
@NoArgsConstructor
public class HPersonEmailValidationKey implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String keyHash;

   private HPerson person;

   private Date creationDate;

   private String email;

   public HPersonEmailValidationKey(HPerson person, String email, String keyHash, Date creationDate)
   {
      this.person = person;
      this.keyHash = keyHash;
      this.creationDate = creationDate;
      this.email = email;
   }

   @Column(unique = true, nullable = false)
   public String getKeyHash()
   {
      return keyHash;
   }

   @Temporal(TemporalType.TIMESTAMP)
   @Column(nullable = false)
   public Date getCreationDate()
   {
      return creationDate;
   }

   @Id
   @ManyToOne(optional = false)
   @JoinColumn(name = "personId", nullable = false)
   public HPerson getPerson()
   {
      return person;
   }

   @Email
   @NotEmpty
   public String getEmail()
   {
      return email;
   }

}
