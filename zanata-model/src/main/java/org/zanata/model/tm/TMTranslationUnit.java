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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyClass;

import org.zanata.model.ModelEntityBase;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A single translation memory unit belonging to a Translation Memory.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@EqualsAndHashCode(callSuper = true, of = {"transUnitId", "sourceLanguage", "translationMemory"})
@ToString(exclude = "translationMemory")
@Data
@NoArgsConstructor
@Access(AccessType.FIELD)
public class TMTranslationUnit extends ModelEntityBase implements HasTMMetadata
{
   private static final long serialVersionUID = 1L;

   @Column(name = "trans_unit_id", nullable = true)
   private String transUnitId;

   @Column(name = "source_language", nullable = true)
   private String sourceLanguage;

   @ManyToOne(optional = false, fetch = FetchType.LAZY)
   @JoinColumn(name = "tm_id", nullable = false)
   private TransMemory translationMemory;

   @Column(name = "unique_id", nullable = false)
   private String uniqueId;

   @Column(nullable = true)
   private Integer position;

   @ManyToMany(cascade = CascadeType.ALL)
   @JoinTable(name = "TMTranslationUnit_TransUnitVariant",
              joinColumns = @JoinColumn(name = "trans_unit_id"),
              inverseJoinColumns = @JoinColumn(name = "trans_unit_variant_id"))
   @MapKey(name = "language")
   private Map<String, TMTransUnitVariant> transUnitVariants = Maps.newHashMap();

   /**
    * Map values are Json strings containing metadata for the particular type of translation memory
    */
   @ElementCollection
   @MapKeyClass(TMMetadataType.class)
   @JoinTable(name = "TMTransUnit_Metadata")
   @Lob
   private Map<TMMetadataType, String> metadata = Maps.newHashMap();

   public TMTranslationUnit(String uniqueId)
   {
      this.uniqueId = uniqueId;
   }
}
