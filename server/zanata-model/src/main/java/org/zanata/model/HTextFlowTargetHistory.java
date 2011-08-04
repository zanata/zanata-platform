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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.zanata.common.ContentState;

@Entity
@org.hibernate.annotations.Entity(mutable = false)
public class HTextFlowTargetHistory implements Serializable, ITextFlowTargetHistory
{

   private static final long serialVersionUID = 1L;

   private Long id;

   private HTextFlowTarget textFlowTarget;

   private Integer versionNum;

   private String content;

   private Date lastChanged;

   private HPerson lastModifiedBy;

   private ContentState state;

   private Integer textFlowRevision;

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

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "target_id")
   public HTextFlowTarget getTextFlowTarget()
   {
      return textFlowTarget;
   }

   public void setTextFlowTarget(HTextFlowTarget textFlowTarget)
   {
      this.textFlowTarget = textFlowTarget;
   }

   @Override
   @NaturalId
   public Integer getVersionNum()
   {
      return versionNum;
   }

   public void setVersionNum(Integer versionNum)
   {
      this.versionNum = versionNum;
   }

   @Override
   @Type(type = "text")
   public String getContent()
   {
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   public Date getLastChanged()
   {
      return lastChanged;
   };

   public void setLastChanged(Date lastChanged)
   {
      this.lastChanged = lastChanged;
   }

   @ManyToOne
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   @Override
   public HPerson getLastModifiedBy()
   {
      return lastModifiedBy;
   }

   public void setLastModifiedBy(HPerson lastModifiedBy)
   {
      this.lastModifiedBy = lastModifiedBy;
   }

   @Override
   public ContentState getState()
   {
      return state;
   }

   public void setState(ContentState state)
   {
      this.state = state;
   }

   @Override
   @Column(name = "tf_revision")
   public Integer getTextFlowRevision()
   {
      return textFlowRevision;
   }

   public void setTextFlowRevision(Integer textFlowRevision)
   {
      this.textFlowRevision = textFlowRevision;
   }
}
