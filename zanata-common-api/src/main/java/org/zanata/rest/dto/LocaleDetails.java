/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.LocaleId;
import org.zanata.common.Namespaces;

@XmlType(name = "localeDetailsType")
@XmlRootElement(name = "localeDetails")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"localeId", "displayName", "alias"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LocaleDetails implements Serializable {

    private LocaleId localeId;
    private String displayName;
    private String alias;

    // TODO check if no args constructor is needed
    public LocaleDetails() {
    }

    public LocaleDetails(LocaleId localeId, String displayName, String alias) {
        this.localeId = localeId;
        this.displayName = displayName;
        this.alias = alias;
    }

    @XmlAttribute(name = "localeId", required = true)
    @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
    @NotNull
    public LocaleId getLocaleId() {
      return localeId;
    }

    public void setLocaleId(LocaleId localeId) {
      this.localeId = localeId;
    }

    @XmlAttribute(name = "displayName", required = true)
    @NotEmpty
    public String getDisplayName() {
      return displayName;
    }

    public void setDisplayName(String displayName) {
      this.displayName = displayName;
    }

    @XmlAttribute(name = "alias", required = false)
    public String getAlias() {
      return alias;
    }

    public void setAlias(String alias) {
      this.alias = alias;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public int hashCode() {
        String composite = localeId.toString() + displayName + alias;
        return composite.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LocaleDetails)) {
            return false;
        }
        LocaleDetails other = (LocaleDetails) obj;
        if (!localeId.equals(other.localeId)) {
          return false;
        }
        // TODO is displayName nullable?
        if (!displayName.equals(other.displayName)) {
          return false;
        }
        if (alias == null) {
          if (other.alias != null) {
            return false;
          }
        } else if (!alias.equals(other.alias)) {
          return false;
        }
        return true;
    }

}