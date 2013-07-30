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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.zanata.common.LocaleId;
import org.zanata.util.TMXUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.okapi.common.resource.Property;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;

/**
 * Adapts TMX metadata to the generic translation memory objects.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TMXMetadataHelper
{
   private static final String EMPTY_NAMESPACE = XMLConstants.NULL_NS_URI;

   private static final String TMX_ELEMENT_CHILDREN = "__TMX_ELEMENT_CHILDREN__";

   private static final DateTimeFormatter ISO8601Z = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z").withZoneUTC();
   private static final ObjectMapper jsonMapper = new ObjectMapper();

   // TMX attributes which we store as fields (*not* in the generic metadata map):
   private static final String _creationdate = "creationdate";
   private static final String _changedate = "changedate";
   private static final String _srclang = TMXUtils.SRCLANG;
   private static final String _lang = "lang";
   private static final String _tuid = "tuid";

   public static List<String> getChildrenXml(Map<String, Object> metadata)
   {
      List<String> children = (List<String>) metadata.get(TMX_ELEMENT_CHILDREN);
      if (children == null)
      {
         return Collections.emptyList();
      }
      return children;
   }

   public static List<Element> getChildrenElements(Map<String, Object> metadata)
   {
      try
      {
         List<String> children = getChildrenXml(metadata);
         List<Element> result = Lists.newArrayListWithCapacity(children.size());
         for (String childXml : children)
         {
            Document doc = new nu.xom.Builder().build(childXml, null);
            Element elem = doc.getRootElement();
            result.add(elem);
         }
         return result;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Gets all the metadata in a single Map.
    * @param tu
    * @return
    */
   public static @Nonnull ImmutableMap<String, Object> getMetadata(TransMemory tm)
   {
      Builder<String, Object> m = ImmutableMap.builder();
      m.putAll(getSharedMetadata(tm));
      String srclang = tm.getSourceLanguage();
      if (srclang != null)
      {
         m.put(_srclang, srclang);
      }
      return m.build();
   }

   /**
    * Gets all the metadata in a single Map.
    * @param tu
    * @return
    */
   public static @Nonnull ImmutableMap<String, Object> getMetadata(TransMemoryUnit tu)
   {
      Builder<String, Object> m = ImmutableMap.builder();
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
    * Gets all the metadata in a single Map.
    * @param tu
    * @return
    */
   public static @Nonnull ImmutableMap<String, Object> getMetadata(TransMemoryUnitVariant tuv)
   {
      Builder<String, Object> m = ImmutableMap.builder();
      m.putAll(getSharedMetadata(tuv));
      String lang = tuv.getLanguage();
      if (lang != null)
      {
         m.put(_lang, lang);
      }
      return m.build();
   }

   private static @Nonnull ImmutableMap<String, Object> getSharedMetadata(HasTMMetadata fromEntity)
   {
      Builder<String, Object> m = ImmutableMap.builder();
      m.putAll(getGenericMetadata(fromEntity));
      Date creationDate = fromEntity.getCreationDate();
      if (creationDate != null)
      {
         m.put(_creationdate, toString(creationDate));
      }
      Date lastChanged = fromEntity.getLastChanged();
      if (lastChanged != null)
      {
         m.put(_changedate, toString(lastChanged));
      }
      return m.build();
   }

   @SuppressWarnings("null")
   private static @Nonnull Map<String, Object> getGenericMetadata(HasTMMetadata fromEntity)
   {
      String metadataString = fromEntity.getMetadata().get(TMMetadataType.TMX14);
      if (metadataString == null)
      {
         return ImmutableMap.of();
      }
      try
      {
         return jsonMapper.readValue(metadataString, Map.class);
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
      Map<String, Object> metadata = buildMetadata(headerElem);
      String srclang = (String) metadata.remove(_srclang);
      if (srclang != null)
      {
         toTransMemory.setSourceLanguage(checkLang(srclang));
      }
      setSharedMetadata(toTransMemory, metadata);
   }

   /**
    * Sets all the TU's metadata, taken from a Map
    * @param toTransUnit
    * @param tuElem
    * @param tmSrcLang 
    */
   public static void setMetadata(TransMemoryUnit toTransUnit, @Nonnull Element tuElem, String tmSrcLang)
   {
      Map<String, Object> metadata = buildMetadata(tuElem);
      String tuid = (String) metadata.remove(_tuid);
      if (tuid != null)
      {
         toTransUnit.setTransUnitId(tuid);
      }
      String srclang = (String) metadata.remove(_srclang);
      if (srclang != null)
      {
         if (srclang.equalsIgnoreCase(TMXUtils.ALL_LOCALE.toBCP47()))
         {
            toTransUnit.setSourceLanguage(null);
         }
         else
         {
            toTransUnit.setSourceLanguage(checkLang(srclang));
         }
      }
      else
      {
         toTransUnit.setSourceLanguage(tmSrcLang);
      }
      setSharedMetadata(toTransUnit, metadata);
   }

   public static void setMetadata(TransMemoryUnitVariant tuv, Element tuvElem)
   {
      Map<String, Object> metadata = buildMetadata(tuvElem);
      String lang = (String) metadata.remove(_lang);
      if (lang != null)
      {
         tuv.setLanguage(checkLang(lang));
      }
      setSharedMetadata(tuv, metadata);
   }

   /**
    * Throws IllegalArgumentException if the language is not accepted
    * @param lang
    * @return lang
    */
   private static String checkLang(String lang)
   {
      return new LocaleId(lang).getId();
   }

   private static void setSharedMetadata(HasTMMetadata toEntity, Map<String, Object> fromMetadata)
   {
      String creationdate = (String) fromMetadata.remove(_creationdate);
      if (creationdate != null)
      {
         toEntity.setCreationDate(toDate(creationdate));
      }
      String changedate = (String) fromMetadata.remove(_changedate);
      if (changedate != null)
      {
         toEntity.setLastChanged(toDate(changedate));
      }
      setGenericMetadata(toEntity, fromMetadata);
   }

   private static void setGenericMetadata(HasTMMetadata toEntity, Map<String, Object> fromMetadata)
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

   private static Map<String, Object> buildMetadata(Element elem)
   {
      Map<String, Object> metadata = Maps.newHashMap();
      for (int i = 0; i < elem.getAttributeCount(); i++)
      {
         Attribute attr = elem.getAttribute(i);
         String uri = attr.getNamespaceURI();
         String name = attr.getLocalName();
         if (inTmxNamespace(uri) || (uri.equals(XMLConstants.XML_NS_URI) && name.equals(_lang)))
         {
            String value = attr.getValue();
            metadata.put(name, value);
         }
      }
      List<String> childrenXml = getChildrenAsXml(elem);
      metadata.put(TMX_ELEMENT_CHILDREN, childrenXml);
      return metadata;
   }

   private static List<String> getChildrenAsXml(Element elem)
   {
      // elem might also have sub nodes (save them as pure xml)
      List<String> childrenXml = Lists.newArrayList();
      Elements childElements = elem.getChildElements();
      for (int i = 0; i < childElements.size(); i++)
      {
         Element child = childElements.get(i);
         String uri = child.getNamespaceURI();
         String name = child.getLocalName();
         if (inTmxNamespace(uri) && (name.equals("prop") || name.equals("note")))
         {
            Element copy = (Element) child.copy();
            copy.setNamespacePrefix("");
            copy.setNamespaceURI("");
            childrenXml.add(copy.toXML());
         }
      }
      return childrenXml;
   }

   private static boolean inTmxNamespace(String uri)
   {
      return uri.equals(EMPTY_NAMESPACE) || uri.equals(TMXUtils.TMX14_NAMESPACE);
   }
}
