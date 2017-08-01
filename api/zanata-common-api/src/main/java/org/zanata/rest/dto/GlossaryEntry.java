/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.LocaleId;
import org.zanata.common.Namespaces;
import org.zanata.rest.MediaTypes;

/**
 * A single glossary entry representing a single translated term in multiple
 * locales.
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@XmlRootElement(name = "glossaryEntry")
@XmlType(name = "glossaryEntryType", propOrder = { "id", "pos",
        "description", "sourceReference", "glossaryTerms", "termsCount", "qualifiedName" })
@JsonPropertyOrder({ "id", "pos", "description", "srcLang", "sourceReference", "glossaryTerms", "termsCount", "qualifiedName" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Label("Glossary Entry")
public class GlossaryEntry implements Serializable, HasMediaType {
    /**
    *
    */
    private static final long serialVersionUID = 1685907304736580890L;

    private Long id;

    private String pos;

    private String description;

    private List<GlossaryTerm> glossaryTerms;

    private LocaleId srcLang;

    private String sourceReference;

    private QualifiedName qualifiedName;

    private int termsCount = 0;

    public GlossaryEntry() {
        this(null);
    }

    public GlossaryEntry(Long id) {
        this.id = id;
    }

    /**
     * Unique identifier
     */
    @XmlElement(name = "id", namespace = Namespaces.ZANATA_OLD)
    @JsonProperty("id")
    @DocumentationExample(value = "444555", value2 = "444556")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Glossary entry's part of speech
     */
    @XmlElement(name = "pos", namespace = Namespaces.ZANATA_OLD)
    @JsonProperty("pos")
    @DocumentationExample(value = "verb", value2 = "noun")
    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    @XmlElement(name = "description", namespace = Namespaces.ZANATA_OLD)
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Number of translated terms. A term is the glossary entry's representation
     * for a specific locale
     */
    @XmlElement(name = "termsCount", namespace = Namespaces.ZANATA_API)
    @JsonProperty("termsCount")
    @DocumentationExample("2")
    public int getTermsCount() {
        return termsCount;
    }

    public void setTermsCount(int termsCount) {
        this.termsCount = termsCount;
    }

    /**
     * The full list of glossary terms
     */
    @XmlElement(name = "glossary-term", namespace = Namespaces.ZANATA_OLD)
    @JsonProperty("glossaryTerms")
    public List<GlossaryTerm> getGlossaryTerms() {
        if (glossaryTerms == null) {
            glossaryTerms = new ArrayList<GlossaryTerm>();
        }
        return glossaryTerms;
    }

    public void setGlossaryTerms(List<GlossaryTerm> glossaryTerms) {
        this.glossaryTerms = glossaryTerms;
    }

    /**
     * The source locale for this specific entry
     */
    @XmlAttribute(name = "src-lang")
    @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
    @JsonProperty("srcLang")
    @DocumentationExample("en-US")
    public LocaleId getSrcLang() {
        return srcLang;
    }

    public void setSrcLang(LocaleId srcLang) {
        this.srcLang = srcLang;
    }

    @XmlElement(name = "source-reference", required = false,
            namespace = Namespaces.ZANATA_OLD)
    @JsonProperty("sourceReference")
    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String ref) {
        this.sourceReference = ref;
    }

    @XmlElement(name = "qualified-name", namespace = Namespaces.ZANATA_API)
    @JsonProperty("qualifiedName")
    public QualifiedName getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(QualifiedName qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlossaryEntry)) return false;

        GlossaryEntry that = (GlossaryEntry) o;

        if (termsCount != that.termsCount) return false;
        if (description != null ? !description.equals(that.description) :
            that.description != null) return false;
        if (glossaryTerms != null ? !glossaryTerms.equals(that.glossaryTerms) :
            that.glossaryTerms != null) return false;
        if (pos != null ? !pos.equals(that.pos) : that.pos != null)
            return false;
        if (sourceReference != null ?
            !sourceReference.equals(that.sourceReference) :
            that.sourceReference != null) return false;
        if (srcLang != null ? !srcLang.equals(that.srcLang) :
            that.srcLang != null)
            return false;
        return qualifiedName != null ?
            qualifiedName.equals(that.qualifiedName) :
            that.qualifiedName == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        result =
            31 * result + (description != null ? description.hashCode() : 0);
        result =
            31 * result +
                (glossaryTerms != null ? glossaryTerms.hashCode() : 0);
        result = 31 * result + (srcLang != null ? srcLang.hashCode() : 0);
        result = 31 * result +
            (sourceReference != null ? sourceReference.hashCode() : 0);
        result =
            31 * result +
                (qualifiedName != null ? qualifiedName.hashCode() : 0);
        result = 31 * result + termsCount;
        return result;
    }

    @Override
    public String getMediaType(MediaTypes.Format format) {
        return MediaTypes.APPLICATION_ZANATA_GLOSSARY + format;
    }
}
