/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import org.zanata.model.SlugEntityBase;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A translation Memory representation.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class TransMemory extends SlugEntityBase implements HasTMMetadata {
    private static final long serialVersionUID = 1L;
    private String description;
    // This is the BCP-47 language code. Null means any source language (*all*
    // in TMX)
    @Column(name = "source_language", nullable = true)
    private String sourceLanguage;

    public static TransMemory tm(String slug) {
        TransMemory tm = new TransMemory();
        tm.setSlug(slug);
        return tm;
    }

    @OneToMany(mappedBy = "translationMemory", orphanRemoval = true)
    private Set<TransMemoryUnit> translationUnits = Sets.newHashSet();

    /**
     * Map values are Json strings containing metadata for the particular type
     * of translation memory
     */
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "metadata_type")
    @JoinTable(name = "TransMemory_Metadata",
            joinColumns = { @JoinColumn(name = "trans_memory_id") })
    @Column(name = "metadata", length = Integer.MAX_VALUE)
    private Map<TMMetadataType, String> metadata = Maps.newHashMap();

    @Override
    public String getMetadata(TMMetadataType tmType) {
        return metadata.get(tmType);
    }

    @Override
    public void setMetadata(@Nonnull TMMetadataType tmType, String metadata) {
        this.metadata.put(tmType, metadata);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransMemory))
            return false;
        final TransMemory other = (TransMemory) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null
                : !this$description.equals(other$description))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TransMemory;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $description = this.getDescription();
        result = result * PRIME
                + ($description == null ? 43 : $description.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TransMemory(description=" + this.getDescription()
                + ", sourceLanguage=" + this.getSourceLanguage() + ", metadata="
                + this.getMetadata() + ")";
    }

    public TransMemory() {
    }

    public String getDescription() {
        return this.description;
    }

    public String getSourceLanguage() {
        return this.sourceLanguage;
    }

    public Set<TransMemoryUnit> getTranslationUnits() {
        return this.translationUnits;
    }

    /**
     * Map values are Json strings containing metadata for the particular type
     * of translation memory
     */
    public Map<TMMetadataType, String> getMetadata() {
        return this.metadata;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setSourceLanguage(final String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    /**
     * Map values are Json strings containing metadata for the particular type
     * of translation memory
     */
    public void setMetadata(final Map<TMMetadataType, String> metadata) {
        this.metadata = metadata;
    }

    protected void
            setTranslationUnits(final Set<TransMemoryUnit> translationUnits) {
        this.translationUnits = translationUnits;
    }
}
