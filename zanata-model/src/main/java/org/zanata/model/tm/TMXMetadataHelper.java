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
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.zanata.common.LocaleId;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * Adapts TMX metadata to the generic translation memory objects.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TMXMetadataHelper
{
   private static final String TMX_ELEMENT_CHILDREN = "__TMX_ELEMENT_CHILDREN__";

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

   private static @Nonnull ImmutableMap<String, String> getSharedMetadata(HasTMMetadata fromEntity)
   {
      Builder<String, String> m = ImmutableMap.builder();
      m.putAll(getGenericMetadata(fromEntity));
      m.put(_creationdate, toString(fromEntity.getCreationDate()));
      m.put(_changedate, toString(fromEntity.getLastChanged()));
      return m.build();
   }

   /**
    * Gets all the metadata in a single Map.
    * @param tu
    * @return
    */
   public static @Nonnull ImmutableMap<String, String> getMetadata(TransMemoryUnit tu)
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
    * @param tuElem
    */
   public static void setMetadata(TransMemoryUnit toTransUnit, @Nonnull Element tuElem)
   {
      Map<String, String> metadata = buildMetadata(tuElem);
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

   private static void setSharedMetadata(HasTMMetadata toEntity, Map<String, String> fromMetadata)
   {
      String creationdate = fromMetadata.remove(_creationdate);
      if (creationdate != null)
      {
         toEntity.setCreationDate(toDate(creationdate));
      }
      String changedate = fromMetadata.remove(_changedate);
      if (changedate != null)
      {
         toEntity.setLastChanged(toDate(changedate));
      }
      setGenericMetadata(toEntity, fromMetadata);
   }

   private static void setGenericMetadata(HasTMMetadata toEntity, Map<String, String> fromMetadata)
   {
      try
      {
         String json = jsonMapper.writeValueAsString(fromMetadata);
         toEntity.getMetadata().put(TMMetadataType.TMX14, json);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Sets all the Translation Memory's metadata, taken from a map.
    * @param toTransMemory
    * @param headerElem
    */
   public static void setMetadata(TransMemory toTransMemory, @Nonnull Element headerElem)
   {
      Map<String, String> metadata = buildMetadata(headerElem);
      String srclang = metadata.remove(_srclang);
      if (srclang != null)
      {
         toTransMemory.setSourceLanguage(new LocaleId(srclang).getId()); // This will fail if the language is not accepted
      }
      setSharedMetadata(toTransMemory, metadata);
   }


   public static void setMetadata(TransMemoryUnitVariant tuv, Element tuvElem)
   {

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

   private static Map<String, String> buildMetadata(Element elem)
   {
      Map<String, String> metadata = Maps.newHashMap();
      for (int i = 0; i < elem.getAttributeCount(); i++)
      {
         Attribute attr = elem.getAttribute(i);
         String name = attr.getQualifiedName();
         String value = attr.getValue();
         metadata.put(name, value);
      }
      // Header might also have sub nodes (save them as pure xml)
      List<String> childrenXml = Lists.newArrayList();
      Elements childElements = elem.getChildElements();
      for (int i = 0; i < childElements.size(); i++)
      {
         Element child = childElements.get(i);
         if (child.getLocalName().equals("prop") || child.getLocalName().equals("note"))
         {
            childrenXml.add(child.toXML());
         }
      }
      metadata.put(TMXMetadataHelper.TMX_ELEMENT_CHILDREN, childrenXml.toString());
      return metadata;
   }
}
