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

@XmlType(name = "localeDetailsType")
@XmlRootElement(name = "localeDetails")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"localeId", "displayName", "alias", "nativeName", "enabled", "enabledByDefault", "pluralForms"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LocaleDetails implements Serializable {

    private LocaleId localeId;
    private String displayName;
    private String alias;
    private String nativeName;
    private boolean enabled;
    private boolean enabledByDefault;
    private String pluralForms;

    // TODO check if no args constructor is needed
    public LocaleDetails() {
        this(null, null, null, null, false, false, null);
    }

    public LocaleDetails(LocaleId localeId, String displayName, String alias,
        String nativeName, boolean enabled, boolean enabledByDefault,
        String pluralForms) {
        this.localeId = localeId;
        this.displayName = displayName;
        this.alias = alias;
        this.nativeName = nativeName;
        this.enabled = enabled;
        this.enabledByDefault = enabledByDefault;
        this.pluralForms = pluralForms;
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

    @XmlAttribute(name = "nativeName", required = false)
    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    @XmlAttribute(name = "enabled", required = true)
    @NotNull
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @XmlAttribute(name = "enabledByDefault", required = true)
    @NotNull
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public void setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    @XmlAttribute(name = "pluralForms", required = false)
    public String getPluralForms() {
        return pluralForms;
    }

    public void setPluralForms(String pluralForms) {
        this.pluralForms = pluralForms;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocaleDetails)) return false;

        LocaleDetails that = (LocaleDetails) o;

        if (enabled != that.enabled) return false;
        if (enabledByDefault != that.enabledByDefault) return false;
        if (localeId != null ? !localeId.equals(that.localeId) :
            that.localeId != null) return false;
        if (displayName != null ? !displayName.equals(that.displayName) :
            that.displayName != null) return false;
        if (alias != null ? !alias.equals(that.alias) : that.alias != null)
            return false;
        if (nativeName != null ? !nativeName.equals(that.nativeName) :
            that.nativeName != null) return false;
        return pluralForms != null ? pluralForms.equals(that.pluralForms) :
            that.pluralForms == null;

    }

    @Override
    public int hashCode() {
        int result = localeId != null ? localeId.hashCode() : 0;
        result =
            31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (nativeName != null ? nativeName.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (enabledByDefault ? 1 : 0);
        result =
            31 * result + (pluralForms != null ? pluralForms.hashCode() : 0);
        return result;
    }
}
