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
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang.time.DateUtils;
import org.zanata.common.UserActionType;
import org.zanata.model.type.EntityType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@NoArgsConstructor
@Access(AccessType.FIELD)
public class Activity extends ModelEntityBase implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Getter
   @NotNull
   @JoinColumn(name = "person_id", nullable = false)
   @ManyToOne
   private HPerson acter;
   
   @Getter
   @Setter
   @Temporal(TemporalType.TIMESTAMP)
   @NotNull
   private Date startOffset;
   
   @Getter
   @Setter
   @Temporal(TemporalType.TIMESTAMP)
   @NotNull
   private Date endOffset;
   
   @Getter
   @Temporal(TemporalType.TIMESTAMP)
   @NotNull
   private Date roundOffDate;
   
   @Getter
   @NotNull
   @Enumerated(EnumType.STRING)
   private EntityType contextType;
   
   @Getter
   @Setter
   @NotNull
   @Column(name = "context_id")
   private Long contextId;
   
   @Getter
   @Enumerated(EnumType.STRING)
   private EntityType lastTargetType;
   
   @Getter
   @Setter
   @NotNull
   @Column(name = "last_target_id")
   private Long lastTargetId;
   
   @Getter
   @Enumerated(EnumType.STRING)
   private UserActionType action;
   
   @Getter
   private int counter = 1;
   
   @Getter
   private int wordCount;
   
   public Activity(HPerson acter, EntityType contextType, Long contextId, EntityType lastTargetType, Long lastTargetId, UserActionType action, int wordCount)
   {
      this.acter = acter;
      this.contextType = contextType;
      this.contextId = contextId;
      this.lastTargetType = lastTargetType;
      this.lastTargetId = lastTargetId;
      this.action = action;
      this.wordCount = wordCount;
   }

   @SuppressWarnings("unused")
   @PrePersist
   private void onPersist()
   {
      creationDate = new Date();
      lastChanged = creationDate;
      
      startOffset = new Date();
      endOffset = startOffset;
      roundOffDate = DateUtils.truncate(startOffset, Calendar.HOUR);
   }
   
   @SuppressWarnings("unused")
   @PreUpdate
   private void onUpdate()
   {
      lastChanged = new Date();
      
      counter++;
   }
}