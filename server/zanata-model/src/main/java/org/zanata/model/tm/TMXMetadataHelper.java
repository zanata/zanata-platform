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
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.zanata.util.TMXConstants;
import org.zanata.util.TMXParseException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ibm.icu.util.ULocale;

/**
 * Adapts TMX metadata to the generic translation memory objects.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class TMXMetadataHelper {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TMXMetadataHelper.class);

    private static final String EMPTY_NAMESPACE = XMLConstants.NULL_NS_URI;
    private static final String TMX_ELEMENT_CHILDREN =
            "__TMX_ELEMENT_CHILDREN__";
    private static final DateTimeFormatter ISO8601Z =
            DateTimeFormat.forPattern("yyyyMMdd\'T\'HHmmss\'Z").withZoneUTC();
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    // TMX attributes which we store as fields (*not* in the generic metadata
    // map):
    private static final String CREATION_DATE = "creationdate";
    private static final String CHANGE_DATE = "changedate";
    private static final String SRC_LANG = TMXConstants.SRCLANG;
    private static final String XML_LANG = "xml:lang";
    private static final String TUID = "tuid";

    private static List<String> getChildrenXml(Map<String, Object> metadata) {
        List<String> children =
                (List<String>) metadata.get(TMX_ELEMENT_CHILDREN);
        if (children == null) {
            return Collections.emptyList();
        }
        return children;
    }

    public static ImmutableList<Element> getChildren(HasTMMetadata fromEntity) {
        try {
            String metadataString =
                    fromEntity.getMetadata(TMMetadataType.TMX14);
            if (metadataString == null) {
                return ImmutableList.of();
            }
            Map<String, Object> metadata =
                    jsonMapper.readValue(metadataString, Map.class);
            List<String> children = getChildrenXml(metadata);
            Builder<Element> result = ImmutableList.builder();
            for (String childXml : children) {
                Document doc = new nu.xom.Builder().build(childXml, null);
                Element elem = (Element) doc.getRootElement().copy();
                result.add(elem);
            }
            return result.build();
        } catch (Exception e) {
            // error parsing XML or json, which "shouldn't happen"
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets all the entity's metadata in a single Map.
     *
     * @param tu
     * @return
     */
    @Nonnull
    public static ImmutableMap<String, String>
            getAttributes(TransMemory fromTm) {
        ImmutableMap.Builder<String, String> m = ImmutableMap.builder();
        m.putAll(getSharedMetadata(fromTm));
        String srclang = fromTm.getSourceLanguage();
        if (srclang != null) {
            m.put(SRC_LANG, srclang);
        }
        return m.build();
    }

    /**
     * Gets all the entity's metadata in a single Map.
     *
     * @param fromTu
     * @return
     */
    @Nonnull
    public static ImmutableMap<String, String>
            getAttributes(TransMemoryUnit fromTu) {
        ImmutableMap.Builder<String, String> m = ImmutableMap.builder();
        m.putAll(getSharedMetadata(fromTu));
        String tuid = fromTu.getTransUnitId();
        if (tuid != null) {
            m.put(TUID, tuid);
        }
        String srclang = fromTu.getSourceLanguage();
        if (srclang != null) {
            m.put(SRC_LANG, srclang);
        }
        return m.build();
    }

    /**
     * Gets all the entity's metadata in a single Map.
     *
     * @param tu
     * @return
     */
    @Nonnull
    public static ImmutableMap<String, String>
            getAttributes(TransMemoryUnitVariant fromTuv) {
        ImmutableMap.Builder<String, String> m = ImmutableMap.builder();
        m.putAll(getSharedMetadata(fromTuv));
        String lang = fromTuv.getLanguage();
        assert lang != null;
        m.put(XML_LANG, lang);
        return m.build();
    }

    @Nonnull
    private static ImmutableMap<String, String>
            getSharedMetadata(HasTMMetadata fromEntity) {
        ImmutableMap.Builder<String, String> m = ImmutableMap.builder();
        m.putAll(getGenericMetadata(fromEntity));
        Date creationDate = fromEntity.getCreationDate();
        if (creationDate != null) {
            m.put(CREATION_DATE, toString(creationDate));
        }
        Date lastChanged = fromEntity.getLastChanged();
        if (lastChanged != null) {
            m.put(CHANGE_DATE, toString(lastChanged));
        }
        return m.build();
    }

    @SuppressWarnings("null")
    @Nonnull
    private static Map<String, String>
            getGenericMetadata(HasTMMetadata fromEntity) {
        String metadataString = fromEntity.getMetadata(TMMetadataType.TMX14);
        if (metadataString == null) {
            return ImmutableMap.of();
        }
        try {
            Map<String, String> map =
                    jsonMapper.readValue(metadataString, Map.class);
            map.remove(TMX_ELEMENT_CHILDREN);
            return map;
        } catch (Exception e) {
            // error parsing json, which "shouldn't happen"
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets all the Translation Memory's metadata (attributes and children)
     *
     * @param toTransMemory
     * @param fromHeaderElem
     * @throws TMXParseException
     */
    public static void setMetadata(TransMemory toTransMemory,
            @Nonnull Element fromHeaderElem) throws TMXParseException {
        Map<String, Object> metadata = buildMetadata(fromHeaderElem);
        String srclang = (String) metadata.remove(SRC_LANG);
        if (srclang != null) {
            toTransMemory.setSourceLanguage(getValidLang(srclang));
        } else {
            throw new TMXParseException("missing srclang in header");
        }
        setSharedMetadata(toTransMemory, metadata);
    }

    /**
     * Sets all the TU's metadata (attributes and children)
     *
     * @param toTransUnit
     * @param fromTuElem
     * @param tmSrcLang
     *            srclang to use if the TU does not specify srclang
     */
    public static void setMetadata(TransMemoryUnit toTransUnit,
            @Nonnull Element fromTuElem, String tmSrcLang) {
        Map<String, Object> metadata = buildMetadata(fromTuElem);
        String tuid = (String) metadata.remove(TUID);
        if (tuid != null) {
            toTransUnit.setTransUnitId(tuid);
        }
        String srclang = (String) metadata.remove(SRC_LANG);
        if (srclang != null) {
            if (srclang.equalsIgnoreCase(TMXConstants.ALL_LOCALE)) {
                toTransUnit.setSourceLanguage(null);
            } else {
                toTransUnit.setSourceLanguage(getValidLang(srclang));
            }
        } else {
            toTransUnit.setSourceLanguage(tmSrcLang);
        }
        setSharedMetadata(toTransUnit, metadata);
    }

    /**
     * Sets all the TUV's metadata (attributes and children)
     *
     * @throws TMXParseException
     */
    public static void setMetadata(TransMemoryUnitVariant toTuv,
            Element fromTuvElem) throws TMXParseException {
        Map<String, Object> metadata = buildMetadata(fromTuvElem);
        String lang = (String) metadata.remove(XML_LANG);
        if (lang != null) {
            toTuv.setLanguage(getValidLang(lang));
        } else {
            throw new TMXParseException(
                    "missing xml:lang in tuv: " + fromTuvElem.toXML());
        }
        setSharedMetadata(toTuv, metadata);
    }

    /**
     * Throws IllegalArgumentException if lang is not a valid code (loose BCP-47
     * check)
     *
     * @param lang
     * @return lang
     */
    private static String getValidLang(final String lang) {
        return ULocale.canonicalize(lang).replace('_', '-');
    }

    private static void setSharedMetadata(HasTMMetadata toEntity,
            Map<String, Object> fromMetadata) {
        String creationdate = (String) fromMetadata.remove(CREATION_DATE);
        if (creationdate != null) {
            toEntity.setCreationDate(toDate(creationdate));
        }
        String changedate = (String) fromMetadata.remove(CHANGE_DATE);
        if (changedate != null) {
            toEntity.setLastChanged(toDate(changedate));
        }
        setGenericMetadata(toEntity, fromMetadata);
    }

    private static void setGenericMetadata(HasTMMetadata toEntity,
            Map<String, Object> fromMetadata) {
        try {
            String json = jsonMapper.writeValueAsString(fromMetadata);
            toEntity.setMetadata(TMMetadataType.TMX14, json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("null")
    @Nonnull
    public static Date toDate(String dateString) {
        return ISO8601Z.parseDateTime(dateString).toDate();
    }

    @SuppressWarnings("null")
    @Nonnull
    public static String toString(Date date) {
        return ISO8601Z.print(date.getTime());
    }

    private static Map<String, Object> buildMetadata(Element fromElem) {
        Map<String, Object> metadata = Maps.newHashMap();
        for (int i = 0; i < fromElem.getAttributeCount(); i++) {
            Attribute attr = fromElem.getAttribute(i);
            String uri = attr.getNamespaceURI();
            String name = attr.getLocalName();
            if (inTmxNamespace(uri)) {
                String value = attr.getValue();
                metadata.put(name, value);
            } else if (attr.getQualifiedName().equals(XML_LANG)) {
                String value = attr.getValue();
                metadata.put(attr.getQualifiedName(), value);
            }
        }
        List<String> childrenXml = getChildrenAsXml(fromElem);
        metadata.put(TMX_ELEMENT_CHILDREN, childrenXml);
        return metadata;
    }

    /**
     * Build a list of supported child Elements in XML string form
     *
     * @param fromElem
     * @return
     */
    private static List<String> getChildrenAsXml(Element fromElem) {
        Builder<String> childrenXml = ImmutableList.builder();
        Elements childElements = fromElem.getChildElements();
        for (int i = 0; i < childElements.size(); i++) {
            Element child = childElements.get(i);
            addChildIfSupported(child, childrenXml);
        }
        return childrenXml.build();
    }

    /**
     * Supported children are currently {@code <prop>} and {@code <note>}.
     *
     * @param child
     * @param childrenXml
     */
    private static void addChildIfSupported(Element child,
            Builder<String> childrenXml) {
        String uri = child.getNamespaceURI();
        String name = child.getLocalName();
        if (inTmxNamespace(uri)
                && (name.equals("prop") || name.equals("note"))) {
            Element copy = (Element) child.copy();
            copy.setNamespacePrefix("");
            copy.setNamespaceURI("");
            childrenXml.add(copy.toXML());
        }
    }

    private static boolean inTmxNamespace(String uri) {
        return uri.equals(EMPTY_NAMESPACE)
                || uri.equals(TMXConstants.TMX14_NAMESPACE);
    }
}
