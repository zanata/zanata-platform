/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.model.tm;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;

/**
 * Adapts TMX metadata to the generic translation memory objects.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TMXMetadataHelper
{
   private static final DateTimeFormatter ISO8601Z = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z").withZoneUTC();
   private static final ObjectMapper jsonMapper = new ObjectMapper();

   // TMX attributes which we store as fields (*not* in the generic metadata map):
   private static final String _creationdate = "creationdate";
   private static final String _changedate = "changedate";
   private static final String _srclang = "srclang";
   private static final String _tuid = "tuid";

   @SuppressWarnings("null")
   private static @Nonnull Map<String, String> getGenericMetadata(HasTMMetadata fromEntity)
   {
      String metadataString = fromEntity.getMetadata().get(TMMetadataType.TMX14);
      try
      {
         return jsonMapper.readValue(metadataString, Map.class);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private static @Nonnull ImmutableMap<String, String> getSharedMetadata(HasTMMetadata entity)
   {
      Builder<String, String> m = ImmutableMap.builder();
      m.putAll(getGenericMetadata(entity));
      m.put(_creationdate, toString(entity.getCreationDate()));
      m.put(_changedate, toString(entity.getLastChanged()));
      return m.build();
   }

   /**
    * Gets all the metadata in a single Map.
    * @param tu
    * @return
    */
   public static @Nonnull ImmutableMap<String, String> getMetadata(TMTranslationUnit tu)
   {
      Builder<String, String> m = ImmutableMap.builder();
      m.putAll(getSharedMetadata(tu));
      String tuid = tu.getTransUnitId();
      if (tuid != null)
      {
         m.put(_tuid, tuid);
      }
      String srclang = tu.getSourceLanguage();
      if (srclang != null)
      {
         m.put(_srclang, srclang);
      }
      return m.build();
   }

   /**
    * Sets all the TU's metadata, taken from a Map
    * @param toTransUnit
    * @param fromMetadata
    */
   public static void setMetadata(TMTranslationUnit toTransUnit, @Nonnull Map<String, String> fromMetadata)
   {
      Map<String, String> metadata = Maps.newHashMap(fromMetadata);
      String tuid = metadata.remove(_tuid);
      if (tuid != null)
      {
         toTransUnit.setTransUnitId(tuid);
      }
      String srclang = metadata.remove(_srclang);
      if (srclang != null)
      {
         toTransUnit.setSourceLanguage(srclang);
      }
      setSharedMetadata(toTransUnit, metadata);
   }

   private static void setSharedMetadata(HasTMMetadata toEntity, Map<String, String> metadata)
   {
      String creationdate = metadata.remove(_creationdate);
      if (creationdate != null)
      {
         toEntity.setCreationDate(toDate(creationdate));
      }
      String changedate = metadata.remove(_changedate);
      if (changedate != null)
      {
         toEntity.setLastChanged(toDate(changedate));
      }
      setGenericMetadata(toEntity, metadata);
   }

   private static void setGenericMetadata(HasTMMetadata toEntity, Map<String, String> metadata)
   {
      try
      {
         String json = jsonMapper.writeValueAsString(metadata);
         toEntity.getMetadata().put(TMMetadataType.TMX14, json);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("null")
   public static @Nonnull Date toDate(String dateString)
   {
      return ISO8601Z.parseDateTime(dateString).toDate();
   }

   @SuppressWarnings("null")
   public static @Nonnull String toString(Date date)
   {
      return ISO8601Z.print(date.getTime());
   }

}
