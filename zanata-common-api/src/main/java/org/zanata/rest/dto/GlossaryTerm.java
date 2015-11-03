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
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.LocaleId;
import org.zanata.common.Namespaces;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/

@XmlType(name = "glossaryTermType", propOrder = {"comment", "content", "locale", "lastModifiedDate", "lastModifiedBy"})
@JsonPropertyOrder({ "content", "comment", "locale", "lastModifiedDate", "lastModifiedBy" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GlossaryTerm implements Serializable {
    /**
    *
    */
    private static final long serialVersionUID = 6140176481272689471L;

    @NotNull
    private LocaleId locale;

    private String content;

    private String comment;

    private String lastModifiedBy;

    private Date lastModifiedDate;

    public GlossaryTerm() {
    }

    @XmlAttribute(name = "lang", namespace = Namespaces.XML)
    @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
    @JsonProperty("locale")
    public LocaleId getLocale() {
        return locale;
    }

    public void setLocale(LocaleId locale) {
        this.locale = locale;
    }

    @XmlElement(name = "content", required = false,
            namespace = Namespaces.ZANATA_OLD)
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @XmlElement(name = "comment", namespace = Namespaces.ZANATA_API)
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @XmlElement(name = "lastModifiedBy", required = false,
        namespace = Namespaces.ZANATA_API)
    @JsonProperty("lastModifiedBy")
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @XmlElement(name = "lastModifiedDate", required = false,
        namespace = Namespaces.ZANATA_API)
    @JsonProperty("lastModifiedDate")
    public Date getLastModifiedDate() {
        return lastModifiedDate != null ? new Date(
            lastModifiedDate.getTime()) : null;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate =
            lastModifiedDate != null ? new Date(
                lastModifiedDate.getTime()) : null;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlossaryTerm)) return false;

        GlossaryTerm that = (GlossaryTerm) o;

        if (comment != null ? !comment.equals(that.comment) :
            that.comment != null)
            return false;
        if (content != null ? !content.equals(that.content) :
            that.content != null)
            return false;
        if (lastModifiedBy != null ?
            !lastModifiedBy.equals(that.lastModifiedBy) :
            that.lastModifiedBy != null) return false;
        if (lastModifiedDate != null ?
            !lastModifiedDate.equals(that.lastModifiedDate) :
            that.lastModifiedDate != null) return false;
        if (locale != null ? !locale.equals(that.locale) : that.locale != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = locale != null ? locale.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result =
            31 * result +
                (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result +
            (lastModifiedDate != null ? lastModifiedDate.hashCode() : 0);
        return result;
    }
}
