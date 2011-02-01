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
package net.openl10n.flies.model.po;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import net.openl10n.flies.model.HSimpleComment;
import net.openl10n.flies.model.HTextFlow;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;

/**
 * 
 * @author sflaniga@redhat.com
 * @see net.openl10n.flies.rest.dto.po.PotEntryData
 * @see net.openl10n.flies.rest.dto.extensions.gettext.PotEntryHeader
 */
@Entity
public class HPotEntryData implements Serializable
{

   private static final long serialVersionUID = 1L;

   private Long id;
   private HTextFlow textFlow;
   private String context;
   private HSimpleComment extractedComment;
   private String flags;
   private String references;

   public HPotEntryData()
   {
   }

   @Id
   @GeneratedValue
   public Long getId()
   {
      return id;
   }

   protected void setId(Long id)
   {
      this.id = id;
   }

   public void setTextFlow(HTextFlow textFlow)
   {
      this.textFlow = textFlow;
   }

   @OneToOne
   @JoinColumn(name = "tf_id", /* nullable=false, */unique = true)
   @NaturalId
   public HTextFlow getTextFlow()
   {
      return textFlow;
   }

   public void setContext(String context)
   {
      this.context = context;
   }

   public String getContext()
   {
      return context;
   }

   public void setExtractedComment(HSimpleComment extractedComment)
   {
      this.extractedComment = extractedComment;
   }

   @OneToOne(optional = true, cascade = CascadeType.ALL)
   @JoinColumn(name = "comment_id")
   public HSimpleComment getExtractedComment()
   {
      return extractedComment;
   }

   /**
    * Gettext message flags, delimited by ',' (comma)
    */
   public void setFlags(String flags)
   {
      this.flags = flags;
   }

   /**
    * Gettext message flags, delimited by ',' (comma)
    */
   public String getFlags()
   {
      return flags;
   }

   /**
    * Gettext message references, delimited by ',' (comma)
    */
   public void setReferences(String references)
   {
      this.references = references;
   }

   /**
    * Gettext message references, delimited by ',' (comma)
    */
   @Column(name = "refs")
   @Type(type = "text")
   public String getReferences()
   {
      return references;
   }

}
