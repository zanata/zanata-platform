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
package org.zanata.model.po;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author sflaniga@redhat.com
 * @see org.zanata.rest.dto.po.PotEntryData
 * @see org.zanata.rest.dto.extensions.gettext.PotEntryHeader
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@BatchSize(size = 20)
@Setter
@Getter
@Access(AccessType.FIELD)
public class HPotEntryData implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   @Setter(AccessLevel.PROTECTED)
   private Long id;

   @OneToOne
   @JoinColumn(name = "tf_id", /* nullable=false, */unique = true)
   @NaturalId
   private HTextFlow textFlow;

   private String context;

   @Deprecated // use HTextFlow.comment
   @OneToOne(optional = true, cascade = CascadeType.ALL)
   @JoinColumn(name = "comment_id")
   private HSimpleComment extractedComment;

   /**
    * Gettext message flags, delimited by ',' (comma)
    */
   private String flags;

   /**
    * Gettext message references, delimited by ',' (comma)
    */
   @Column(name = "refs")
   @Type(type = "text")
   private String references;
}
