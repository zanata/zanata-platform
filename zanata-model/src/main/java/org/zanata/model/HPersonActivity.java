/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.zanata.common.UserActionType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NoArgsConstructor
@Access(AccessType.FIELD)
public class HPersonActivity implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   @Getter
   protected Long id;
   
   @Getter
   @Temporal(TemporalType.TIMESTAMP)
   @NotNull
   private Date lastChanged;
   
   @Getter
   @NotNull
   @JoinColumn(name = "person_id", nullable = false)
   @ManyToOne
   private HPerson person;
   
   @Getter
   @NotNull
   @JoinColumn(name = "project_iteration_id", nullable = false)
   @ManyToOne
   private HProjectIteration projectIteration;
   
   @Getter
   @Enumerated(EnumType.STRING)
   private UserActionType action;

   public HPersonActivity(HPerson person, HProjectIteration projectIteration, UserActionType action)
   {
      this.person = person;
      this.projectIteration = projectIteration;
      this.action = action;
   }
   
   public void updateLastChanged()
   {
      lastChanged = new Date();
   }
   
   @SuppressWarnings("unused")
   @PrePersist
   private void onPersist()
   {
      updateLastChanged();
   }
   
   @SuppressWarnings("unused")
   @PreUpdate
   private void onUpdate()
   {
      updateLastChanged();
   }
}