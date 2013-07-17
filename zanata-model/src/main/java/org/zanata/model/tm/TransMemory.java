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
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.MapKeyClass;
import javax.persistence.OneToMany;

import org.zanata.model.SlugEntityBase;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * A translation Memory representation.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@EqualsAndHashCode(callSuper = true, of = {"name"})
@ToString(exclude = "translationUnits")
@Data
@Access(AccessType.FIELD)
public class TransMemory extends SlugEntityBase implements HasTMMetadata
{
   private static final long serialVersionUID = 1L;

   private String description;

   @Column(name = "source_language", nullable = true)
   private String sourceLanguage;

   @Setter(AccessLevel.PROTECTED)
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "translationMemory")
   private Set<TMTranslationUnit> translationUnits = Sets.newHashSet();

   /**
    * Map values are Json strings containing metadata for the particular type of translation memory
    */
   @ElementCollection
   @MapKeyClass(TMMetadataType.class)
   @JoinTable(name = "TransMemory_Metadata")
   @Lob
   private Map<TMMetadataType, String> metadata = Maps.newHashMap();

}
