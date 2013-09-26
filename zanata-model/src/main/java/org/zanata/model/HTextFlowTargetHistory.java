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

import javax.persistence.Access;
import javax.persistence.CascadeType;
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
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.zanata.common.ContentState;
import com.google.common.base.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Immutable
@NamedQueries({
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+1,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount " +
               		  "and contents[0] = :content0"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+2,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount " +
                       "and contents[0] = :content0 and contents[1] = :content1"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+3,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount " +
                       "and contents[0] = :content0 and contents[1] = :content1 and contents[2] = :content2"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+4,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount " +
                       "and contents[0] = :content0 and contents[1] = :content1 and contents[2] = :content2 and contents[3] = :content3"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+5,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount " +
                       "and contents[0] = :content0 and contents[1] = :content1 and contents[2] = :content2 and contents[3] = :content3 and contents[4] = :content4"),
   @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY+6,
               query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount " +
                       "and contents[0] = :content0 and contents[1] = :content1 and contents[2] = :content2 and contents[3] = :content3 and contents[4] = :content4 and contents[5] = :content5")
})
@NoArgsConstructor
@Access(javax.persistence.AccessType.FIELD)
@Getter
@Setter
public class HTextFlowTargetHistory extends HTextContainer implements Serializable, ITextFlowTargetHistory
{
   static final String QUERY_MATCHING_HISTORY = "HTextFlowTargetHistory.QUERY_MATCHING_HISTORY.";

   public static String getQueryNameMatchingHistory(int size)
   {
      return QUERY_MATCHING_HISTORY+size;
   }

   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   @Setter(AccessLevel.PROTECTED)
   private Long id;

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "target_id")
   private HTextFlowTarget textFlowTarget;

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   private Integer versionNum;

   @Type(type = "text")
   @AccessType("field")
   @ElementCollection(fetch = FetchType.EAGER)
   @JoinTable(name = "HTextFlowTargetContentHistory",
      joinColumns = @JoinColumn(name = "text_flow_target_history_id")
   )
   @IndexColumn(name = "pos", nullable = false)
   @Column(name = "content", nullable = false)
   private List<String> contents;

   private Date lastChanged;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   private HPerson lastModifiedBy;

   private ContentState state;

   @Column(name = "tf_revision")
   private Integer textFlowRevision;

   @ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
   @JoinColumn(name = "translated_by_id", nullable = true)
   @Setter(AccessLevel.PROTECTED)
   private HPerson translator;

   @ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
   @JoinColumn(name = "reviewed_by_id", nullable = true)
   @Setter(AccessLevel.PROTECTED)
   private HPerson reviewer;

   public HTextFlowTargetHistory(HTextFlowTarget target)
   {
      this.lastChanged = target.getLastChanged();
      this.lastModifiedBy = target.getLastModifiedBy();
      this.state = target.getState();
      this.textFlowRevision = target.getTextFlowRevision();
      this.textFlowTarget = target;
      this.versionNum = target.getVersionNum();
      translator = target.getTranslator();
      reviewer = target.getReviewer();
      this.setContents(target.getContents());
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
             || !Objects.equal(current.getTranslator(), this.translator)
             || !Objects.equal(current.getReviewer(), this.reviewer)
             || !Objects.equal(current.getState(), this.state)
             || !Objects.equal(current.getTextFlowRevision(), this.textFlowRevision)
             || !Objects.equal(current.getLastChanged(), this.lastChanged)
             || !Objects.equal(current.getTextFlow().getId(), this.textFlowTarget.getId())
             || !Objects.equal(current.getVersionNum(), this.versionNum);
   }
}
