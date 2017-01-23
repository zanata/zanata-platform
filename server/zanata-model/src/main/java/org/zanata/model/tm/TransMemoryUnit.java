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
import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.zanata.model.ModelEntityBase;
import com.google.common.collect.Maps;

/**
 * A single translation memory unit belonging to a Translation Memory.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@Indexed
public class TransMemoryUnit extends ModelEntityBase implements HasTMMetadata {
    private static final long serialVersionUID = 1L;

    public static TransMemoryUnit tu(TransMemory tm, String uniqueId,
            String transUnitId, String sourceLanguage, String sourceContent,
            TransMemoryUnitVariant... transUnitVariants) {
        return new TransMemoryUnit(tm, uniqueId, transUnitId, sourceLanguage,
                sourceContent,
                TransMemoryUnitVariant.newMap(transUnitVariants));
    }

    public TransMemoryUnit(TransMemory tm, String uniqueId, String transUnitId,
            String sourceLanguage, String sourceContent,
            Map<String, TransMemoryUnitVariant> transUnitVariants) {
        this.translationMemory = tm;
        this.uniqueId = uniqueId;
        this.transUnitId = transUnitId;
        this.transUnitVariants = transUnitVariants;
        this.sourceLanguage = sourceLanguage;
        this.transUnitVariants.put(sourceLanguage,
                new TransMemoryUnitVariant(sourceLanguage, sourceContent));
    }

    @Column(name = "trans_unit_id", nullable = true)
    private String transUnitId;
    // This is the BCP-47 language code, or null iff the TU supports all source
    // languages (*all* in TMX)
    @Column(name = "source_language", nullable = true)
    @Field
    private String sourceLanguage;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_id", nullable = false)
    private TransMemory translationMemory;
    @Column(name = "unique_id", nullable = false)
    private String uniqueId;
    @Column(nullable = true)
    private Integer position;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "trans_unit_id", nullable = false)
    @MapKey(name = "language")
    @IndexedEmbedded
    private Map<String, TransMemoryUnitVariant> transUnitVariants =
            Maps.newHashMap();
    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_type", nullable = true)
    private TMMetadataType metadataType;
    @Column(nullable = true)
    @Basic(fetch = FetchType.LAZY)
    private String metadata;

    public TransMemoryUnit(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    protected boolean logPersistence() {
        return false;
    }

    @Override
    public String getMetadata(TMMetadataType tmType) {
        if (this.metadataType == tmType) {
            return this.metadata;
        }
        return null;
    }

    @Override
    public void setMetadata(@Nonnull TMMetadataType tmType, String metadata) {
        assert this.metadataType == null
                || this.metadataType == tmType : "Only one type of metadata is supported for this entity";
        setMetadataType(tmType);
        setMetadata(metadata);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransMemoryUnit))
            return false;
        final TransMemoryUnit other = (TransMemoryUnit) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$transUnitId = this.getTransUnitId();
        final Object other$transUnitId = other.getTransUnitId();
        if (this$transUnitId == null ? other$transUnitId != null
                : !this$transUnitId.equals(other$transUnitId))
            return false;
        final Object this$sourceLanguage = this.getSourceLanguage();
        final Object other$sourceLanguage = other.getSourceLanguage();
        if (this$sourceLanguage == null ? other$sourceLanguage != null
                : !this$sourceLanguage.equals(other$sourceLanguage))
            return false;
        final Object this$translationMemory = this.getTranslationMemory();
        final Object other$translationMemory = other.getTranslationMemory();
        if (this$translationMemory == null ? other$translationMemory != null
                : !this$translationMemory.equals(other$translationMemory))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TransMemoryUnit;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $transUnitId = this.getTransUnitId();
        result = result * PRIME
                + ($transUnitId == null ? 43 : $transUnitId.hashCode());
        final Object $sourceLanguage = this.getSourceLanguage();
        result = result * PRIME
                + ($sourceLanguage == null ? 43 : $sourceLanguage.hashCode());
        final Object $translationMemory = this.getTranslationMemory();
        result = result * PRIME + ($translationMemory == null ? 43
                : $translationMemory.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TransMemoryUnit(transUnitId=" + this.getTransUnitId()
                + ", sourceLanguage=" + this.getSourceLanguage() + ", uniqueId="
                + this.getUniqueId() + ", position=" + this.getPosition()
                + ", transUnitVariants=" + this.getTransUnitVariants()
                + ", metadataType=" + this.getMetadataType() + ", metadata="
                + this.getMetadata() + ")";
    }

    public String getTransUnitId() {
        return this.transUnitId;
    }

    public String getSourceLanguage() {
        return this.sourceLanguage;
    }

    public TransMemory getTranslationMemory() {
        return this.translationMemory;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public Integer getPosition() {
        return this.position;
    }

    public Map<String, TransMemoryUnitVariant> getTransUnitVariants() {
        return this.transUnitVariants;
    }

    public TMMetadataType getMetadataType() {
        return this.metadataType;
    }

    public String getMetadata() {
        return this.metadata;
    }

    public void setTransUnitId(final String transUnitId) {
        this.transUnitId = transUnitId;
    }

    public void setSourceLanguage(final String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public void setTranslationMemory(final TransMemory translationMemory) {
        this.translationMemory = translationMemory;
    }

    public void setUniqueId(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public void setTransUnitVariants(
            final Map<String, TransMemoryUnitVariant> transUnitVariants) {
        this.transUnitVariants = transUnitVariants;
    }

    public void setMetadataType(final TMMetadataType metadataType) {
        this.metadataType = metadataType;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    public TransMemoryUnit() {
    }
}
