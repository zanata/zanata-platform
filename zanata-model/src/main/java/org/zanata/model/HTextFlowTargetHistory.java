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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.zanata.common.ContentState;
import com.google.common.base.Objects;

@Entity
@org.hibernate.annotations.Entity(mutable = false)
@NamedQueries({
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+1,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = ?1 and size(t.contents) = ?2 " +
               		  "and contents[0] = ?3"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+2,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = ?1 and size(t.contents) = ?2 " +
                       "and contents[0] = ?3 and contents[1] = ?4"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+3,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = ?1 and size(t.contents) = ?2 " +
                       "and contents[0] = ?3 and contents[1] = ?4 and contents[2] = ?5"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+4,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = ?1 and size(t.contents) = ?2 " +
                       "and contents[0] = ?3 and contents[1] = ?4 and contents[2] = ?5 and contents[3] = ?6"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+5,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = ?1 and size(t.contents) = ?2 " +
                       "and contents[0] = ?3 and contents[1] = ?4 and contents[2] = ?5 and contents[3] = ?6 and contents[4] = ?7"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+6,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = ?1 and size(t.contents) = ?2 " +
                       "and contents[0] = ?3 and contents[1] = ?4 and contents[2] = ?5 and contents[3] = ?6 and contents[4] = ?7 and contents[5] = ?8")
})
public class HTextFlowTargetHistory extends HTextContainer implements Serializable, ITextFlowTargetHistory
{
   static final String QUERY_MATCHING_HISTORY = "HTextFlowTargetHistory.QUERY_MATCHING_HISTORY.";

   public static String getQueryNameMatchingHistory(int size)
   {
      return QUERY_MATCHING_HISTORY+size;
   }

   private static final long serialVersionUID = 1L;

   private Long id;

   private HTextFlowTarget textFlowTarget;

   private Integer versionNum;

   private List<String> contents;

   private Date lastChanged;

   private HPerson lastModifiedBy;

   private ContentState state;

   private Integer textFlowRevision;

   public HTextFlowTargetHistory()
   {
   }

   public HTextFlowTargetHistory(HTextFlowTarget target)
   {
      this.lastChanged = target.getLastChanged();
      this.lastModifiedBy = target.getLastModifiedBy();
      this.state = target.getState();
      this.textFlowRevision = target.getTextFlowRevision();
      this.textFlowTarget = target;
      this.versionNum = target.getVersionNum();
      this.setContents(target.getContents());
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

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @ManyToOne(fetch = FetchType.LAZY)
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
   // TODO PERF @NaturalId(mutable=false) for better criteria caching
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
   @AccessType("field")
   @ElementCollection(fetch = FetchType.EAGER)
   @JoinTable(name = "HTextFlowTargetContentHistory", 
      joinColumns = @JoinColumn(name = "text_flow_target_history_id")
   )
   @IndexColumn(name = "pos", nullable = false)
   @Column(name = "content", nullable = false)
   public List<String> getContents()
   {
      return contents;
   }

   public void setContents(List<String> contents)
   {
      this.contents = new ArrayList<String>(contents);
   }

   public Date getLastChanged()
   {
      return lastChanged;
   };

   public void setLastChanged(Date lastChanged)
   {
      this.lastChanged = lastChanged;
   }

   @ManyToOne(fetch = FetchType.LAZY)
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
   
   /**
    * Determines whether a Text Flow Target has changed when compared to this
    * history object.
    * 
    * @param current The current Text Flow Target state. 
    * @return True, if any of the Text Flow Target fields have changed from the
    * state recorded in this History object. False, otherwise.
    */
   public boolean hasChanged(HTextFlowTarget current)
   {
      return    !Objects.equal(current.getContents(), this.contents)
             || !Objects.equal(current.getLastChanged(), this.lastChanged)
             || !Objects.equal(current.getLastModifiedBy(), this.lastModifiedBy)
             || !Objects.equal(current.getState(), this.state)
             || !Objects.equal(current.getTextFlowRevision(), this.textFlowRevision)
             || !Objects.equal(current.getLastChanged(), this.lastChanged)
             || !Objects.equal(current.getTextFlow().getId(), this.textFlowTarget.getId())
             || !Objects.equal(current.getVersionNum(), this.versionNum);
   }
}
