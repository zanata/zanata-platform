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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import org.hibernate.search.annotations.AnalyzerDiscriminator;
import org.hibernate.search.annotations.ClassBridge;
import org.zanata.hibernate.search.TextContainerAnalyzerDiscriminator;
import org.zanata.hibernate.search.TransUnitVariantClassBridge;
import org.zanata.model.ModelEntityBase;
import org.zanata.util.HashUtil;
import org.zanata.util.OkapiUtil;

/**
 * A translation unit variant. This is the equivalent of a translated string.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@ClassBridge(impl = TransUnitVariantClassBridge.class)
@AnalyzerDiscriminator(impl = TextContainerAnalyzerDiscriminator.class)
public class TransMemoryUnitVariant extends ModelEntityBase
        implements HasTMMetadata {
    private static final long serialVersionUID = 1L;
    // This is the BCP-47 language code
    @Column(nullable = false)
    private String language;
    @Column(name = "tagged_segment", nullable = false,
            length = Integer.MAX_VALUE)
    private String taggedSegment;
    @Column(name = "plain_text_segment", nullable = false,
            length = Integer.MAX_VALUE)
    private String plainTextSegment;
    @Column(name = "plain_text_segment_hash", nullable = false)
    private String plainTextSegmentHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_type", nullable = true)
    private TMMetadataType metadataType;
    @Column(nullable = true)
    @Basic(fetch = FetchType.LAZY)
    private String metadata;

    public static TransMemoryUnitVariant tuv(String language, String content) {
        return new TransMemoryUnitVariant(language, content);
    }

    public TransMemoryUnitVariant(String language, String taggedSegment) {
        this.language = language;
        this.setTaggedSegment(taggedSegment);
    }

    public void setTaggedSegment(String taggedSegment) {
        this.taggedSegment = taggedSegment;
        updatePlainTextSegment();
    }

    private void updatePlainTextSegment() {
        this.plainTextSegment = OkapiUtil.removeFormattingMarkup(taggedSegment);
        updatePlainTextSegmentHash();
    }

    private void updatePlainTextSegmentHash() {
        this.plainTextSegmentHash =
                HashUtil.generateHash(this.plainTextSegment);
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

    public static Map<String, TransMemoryUnitVariant>
            newMap(TransMemoryUnitVariant... tuvs) {
        Map<String, TransMemoryUnitVariant> map = new HashMap<>();
        Arrays.stream(tuvs).forEach(tuv -> map.put(tuv.getLanguage(), tuv));
        return map;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransMemoryUnitVariant))
            return false;
        final TransMemoryUnitVariant other = (TransMemoryUnitVariant) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$language = this.getLanguage();
        final Object other$language = other.getLanguage();
        if (this$language == null ? other$language != null
                : !this$language.equals(other$language))
            return false;
        final Object this$taggedSegment = this.getTaggedSegment();
        final Object other$taggedSegment = other.getTaggedSegment();
        if (this$taggedSegment == null ? other$taggedSegment != null
                : !this$taggedSegment.equals(other$taggedSegment))
            return false;
        final Object this$plainTextSegment = this.getPlainTextSegment();
        final Object other$plainTextSegment = other.getPlainTextSegment();
        if (this$plainTextSegment == null ? other$plainTextSegment != null
                : !this$plainTextSegment.equals(other$plainTextSegment))
            return false;
        final Object this$plainTextSegmentHash = this.getPlainTextSegmentHash();
        final Object other$plainTextSegmentHash =
                other.getPlainTextSegmentHash();
        if (this$plainTextSegmentHash == null
                ? other$plainTextSegmentHash != null
                : !this$plainTextSegmentHash.equals(other$plainTextSegmentHash))
            return false;
        final Object this$metadataType = this.getMetadataType();
        final Object other$metadataType = other.getMetadataType();
        if (this$metadataType == null ? other$metadataType != null
                : !this$metadataType.equals(other$metadataType))
            return false;
        final Object this$metadata = this.getMetadata();
        final Object other$metadata = other.getMetadata();
        if (this$metadata == null ? other$metadata != null
                : !this$metadata.equals(other$metadata))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TransMemoryUnitVariant;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $language = this.getLanguage();
        result = result * PRIME
                + ($language == null ? 43 : $language.hashCode());
        final Object $taggedSegment = this.getTaggedSegment();
        result = result * PRIME
                + ($taggedSegment == null ? 43 : $taggedSegment.hashCode());
        final Object $plainTextSegment = this.getPlainTextSegment();
        result = result * PRIME + ($plainTextSegment == null ? 43
                : $plainTextSegment.hashCode());
        final Object $plainTextSegmentHash = this.getPlainTextSegmentHash();
        result = result * PRIME + ($plainTextSegmentHash == null ? 43
                : $plainTextSegmentHash.hashCode());
        final Object $metadataType = this.getMetadataType();
        result = result * PRIME
                + ($metadataType == null ? 43 : $metadataType.hashCode());
        final Object $metadata = this.getMetadata();
        result = result * PRIME
                + ($metadata == null ? 43 : $metadata.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TransMemoryUnitVariant(language=" + this.getLanguage()
                + ", taggedSegment=" + this.getTaggedSegment()
                + ", plainTextSegment=" + this.getPlainTextSegment()
                + ", metadataType=" + this.getMetadataType() + ", metadata="
                + this.getMetadata() + ")";
    }

    public TransMemoryUnitVariant() {
    }

    public String getLanguage() {
        return this.language;
    }

    public String getTaggedSegment() {
        return this.taggedSegment;
    }

    public String getPlainTextSegment() {
        return this.plainTextSegment;
    }

    public String getPlainTextSegmentHash() {
        return this.plainTextSegmentHash;
    }

    public TMMetadataType getMetadataType() {
        return this.metadataType;
    }

    public String getMetadata() {
        return this.metadata;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public void setMetadataType(final TMMetadataType metadataType) {
        this.metadataType = metadataType;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    protected void setPlainTextSegmentHash(final String plainTextSegmentHash) {
        this.plainTextSegmentHash = plainTextSegmentHash;
    }
}
