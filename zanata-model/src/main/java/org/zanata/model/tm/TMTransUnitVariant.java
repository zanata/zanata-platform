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
package org.zanata.model.tm;

import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.MapKeyClass;

import org.zanata.util.OkapiUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.zanata.model.ModelEntityBase;
import org.zanata.util.HashUtil;

import com.google.common.collect.Maps;

/**
 * A translation unit variant.
 * This is the equivalent of a translated string.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@EqualsAndHashCode(callSuper=true, exclude = {"content"})
@ToString(exclude = {"contentHash", "metadata"})
@NoArgsConstructor
@Data
@Access(AccessType.FIELD)
public class TMTransUnitVariant extends ModelEntityBase implements HasTMMetadata
{
   private static final long serialVersionUID = 1L;

   @Column(nullable = false)
   private String language;

   @Column(name = "tagged_segment", nullable = false)
   @Lob
   private String taggedSegment;

   @Setter(AccessLevel.NONE)
   @Column(name = "plain_text_segment", nullable = true)
   @Lob
   private String plainTextSegment;

   @Setter(AccessLevel.PROTECTED)
   @Column(name ="plain_text_segment_hash", nullable = false)
   private String plainTextSegmentHash;

   /**
    * Map values are Json strings containing metadata for the particular type of translation memory
    */
   @ElementCollection
   @MapKeyClass(TMMetadataType.class)
   @JoinTable(name = "TMTransUnitVariant_Metadata")
   @Lob
   private Map<TMMetadataType, String> metadata = Maps.newHashMap();

   public TMTransUnitVariant(String language, String content)
   {
      this.language = language;
      this.setTaggedSegment(content);
   }

   public void setTaggedSegment(String taggedSegment)
   {
      this.taggedSegment = taggedSegment;
      updatePlainTextSegment();
   }

   private void updatePlainTextSegmentHash()
   {
      this.plainTextSegmentHash = HashUtil.generateHash(this.plainTextSegment);
   }

   private void updatePlainTextSegment()
   {
      this.plainTextSegment = OkapiUtil.removeFormattingMarkup(taggedSegment);
      updatePlainTextSegmentHash();
   }


}
