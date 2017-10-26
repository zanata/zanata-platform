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

import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.LocaleId;

@XmlType(name = "localeDetailsType")
@XmlRootElement(name = "localeDetails")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"localeId", "displayName", "alias", "nativeName", "enabled", "enabledByDefault", "pluralForms"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Label("Locale Details")
public class LocaleDetails implements Serializable {

    private static final long serialVersionUID = -8133147543880728788L;
    private LocaleId localeId;
    private String displayName;
    private String alias;
    private String nativeName;
    private boolean enabled;
    private boolean enabledByDefault;
    private String pluralForms;
    private boolean rtl;

    // TODO check if no args constructor is needed
    public LocaleDetails() {
        this(null, null, null, null, false, false, null, false);
    }

    public LocaleDetails(LocaleId localeId, String displayName, String alias,
            String nativeName, boolean enabled, boolean enabledByDefault,
            String pluralForms) {
        this(localeId, displayName, alias, nativeName, enabled,
                enabledByDefault, pluralForms, false);
    }

    public LocaleDetails(LocaleId localeId, String displayName, String alias,
        String nativeName, boolean enabled, boolean enabledByDefault,
        String pluralForms, boolean rtl) {
        this.localeId = localeId;
        this.displayName = displayName;
        this.alias = alias;
        this.nativeName = nativeName;
        this.enabled = enabled;
        this.enabledByDefault = enabledByDefault;
        this.pluralForms = pluralForms;
        this.rtl = rtl;
    }

    /**
     * Unique locale identifier
     */
    @XmlAttribute(name = "localeId", required = true)
    @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
    @NotNull
    @DocumentationExample(value = "es-ES", value2 = "ja")
    public LocaleId getLocaleId() {
      return localeId;
    }

    public void setLocaleId(LocaleId localeId) {
      this.localeId = localeId;
    }

    /**
     * Locale's display name (in English)
     */
    @XmlAttribute(name = "displayName", required = true)
    @DocumentationExample(value = "Spanish (Spain)", value2 = "Japanese")
    public String getDisplayName() {
      return displayName;
    }

    public void setDisplayName(String displayName) {
      this.displayName = displayName;
    }

    /**
     * An alternative name (if present) for this locale
     */
    @XmlAttribute(name = "alias", required = false)
    @DocumentationExample(value = "es", value2 = "ja-JP")
    public String getAlias() {
      return alias;
    }

    public void setAlias(String alias) {
      this.alias = alias;
    }

    @XmlAttribute(name = "nativeName", required = false)
    @DocumentationExample(value = "Español", value2 = "日本語")
    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    /**
     * Indicates whether the locale is enabled in the system or not.
     */
    @XmlAttribute(name = "enabled", required = true)
    @NotNull
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Indicates whether the locale will be used automatically by the system.
     * e.g. when creating a new project, 'enabled by default' locales will
     * automatically be added to the project unless specifically indicating so.
     */
    @XmlAttribute(name = "enabledByDefault", required = true)
    @NotNull
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public void setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    /**
     * A string describing the formula for the locale's plural forms
     */
    @XmlAttribute(name = "pluralForms", required = false)
    @DocumentationExample(value = "nplurals=2; plural=(n != 1)",
            value2 = "nplurals=1; plural=0")
    public String getPluralForms() {
        return pluralForms;
    }

    public void setPluralForms(String pluralForms) {
        this.pluralForms = pluralForms;
    }

    /**
     * Indicates if this locale is Right-to-Left
     */
    @XmlAttribute(name = "rtl")
    public boolean isRtl() {
        return rtl;
    }

    public void setRTL(boolean rtl) {
        this.rtl = rtl;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocaleDetails that = (LocaleDetails) o;

        if (enabled != that.enabled) return false;
        if (enabledByDefault != that.enabledByDefault) return false;
        if (rtl != that.rtl) return false;
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
        result = 31 * result +
                (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (nativeName != null ? nativeName.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (enabledByDefault ? 1 : 0);
        result = 31 * result +
                (pluralForms != null ? pluralForms.hashCode() : 0);
        result = 31 * result + (rtl ? 1 : 0);
        return result;
    }
}
