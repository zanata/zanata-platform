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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
@Access(AccessType.FIELD)
public class HPersonEmailValidationKey implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private Long id;

   @Column(nullable = false, unique = true)
   private String keyHash;

   @ManyToOne(optional = false)
   @JoinColumn(name = "personId", nullable = false, unique = true)
   private HPerson person;

   @Temporal(TemporalType.TIMESTAMP)
   @Column(nullable = false)
   private Date creationDate;

   @Email
   @NotEmpty
   private String email;

   public HPersonEmailValidationKey(HPerson person, String email, String keyHash)
   {
      this.person = person;
      this.keyHash = keyHash;
      this.email = email;
   }

   @SuppressWarnings("unused")
   @PrePersist
   private void onPersist()
   {
      creationDate = new Date();
   }

}
