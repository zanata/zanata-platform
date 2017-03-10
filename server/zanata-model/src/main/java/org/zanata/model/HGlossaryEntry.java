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
package org.zanata.model;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.zanata.hibernate.search.LocaleIdBridge;
import org.zanata.util.GlossaryUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@EntityListeners({ HGlossaryEntry.EntityListener.class })
@Cacheable
@Indexed
public class HGlossaryEntry extends ModelEntityBase {

    private static final long serialVersionUID = -4200183325180630061L;
    private Map<HLocale, HGlossaryTerm> glossaryTerms;
    private String sourceRef;
    private String contentHash;
    private String pos;
    private String description;
    private HLocale srcLocale;
    private Glossary glossary;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "glossaryEntry",
            orphanRemoval = true, fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "localeId", referencedColumnName = "id")
    public Map<HLocale, HGlossaryTerm> getGlossaryTerms() {
        if (glossaryTerms == null) {
            glossaryTerms = new HashMap<HLocale, HGlossaryTerm>();
        }
        return glossaryTerms;
    }

    @javax.persistence.Lob
    public String getSourceRef() {
        return sourceRef;
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "glossaryId", nullable = false)
    @IndexedEmbedded
    public Glossary getGlossary() {
        return glossary;
    }
    // TODO: this should be many to one

    @OneToOne
    @JoinColumn(name = "srcLocaleId", nullable = false)
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = LocaleIdBridge.class)
    public HLocale getSrcLocale() {
        return srcLocale;
    }

    @NotNull
    public String getContentHash() {
        return contentHash;
    }

    public String getPos() {
        return pos;
    }

    public String getDescription() {
        return description;
    }

    public static class EntityListener {

        @PreUpdate
        private void preUpdate(HGlossaryEntry entry) {
            entry.setContentHash(getHash(entry));
            updateSrcTermLastChanged(entry);
        }

        @PrePersist
        private void prePersist(HGlossaryEntry entry) {
            entry.setContentHash(getHash(entry));
            updateSrcTermLastChanged(entry);
        }

        private void updateSrcTermLastChanged(HGlossaryEntry entry) {
            HGlossaryTerm srcTerm =
                entry.getGlossaryTerms().get(entry.getSrcLocale());
            if (srcTerm != null) {
                srcTerm.setLastChanged(entry.getLastChanged());
            }
        }

        private String getHash(HGlossaryEntry entry) {
            HLocale srcLocale = entry.srcLocale;
            String sourceContent = "";
            if (entry.getGlossaryTerms().containsKey(srcLocale)) {
                sourceContent =
                        entry.getGlossaryTerms().get(srcLocale).getContent();
            }
            return GlossaryUtil.generateHash(srcLocale.getLocaleId(),
                    sourceContent, entry.getPos(), entry.getDescription());
        }
    }

    public void
            setGlossaryTerms(final Map<HLocale, HGlossaryTerm> glossaryTerms) {
        this.glossaryTerms = glossaryTerms;
    }

    public void setSourceRef(final String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public void setContentHash(final String contentHash) {
        this.contentHash = contentHash;
    }

    public void setPos(final String pos) {
        this.pos = pos;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setSrcLocale(final HLocale srcLocale) {
        this.srcLocale = srcLocale;
    }

    public void setGlossary(final Glossary glossary) {
        this.glossary = glossary;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HGlossaryEntry))
            return false;
        final HGlossaryEntry other = (HGlossaryEntry) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$sourceRef = this.sourceRef;
        final Object other$sourceRef = other.sourceRef;
        if (this$sourceRef == null ? other$sourceRef != null
                : !this$sourceRef.equals(other$sourceRef))
            return false;
        final Object this$contentHash = this.contentHash;
        final Object other$contentHash = other.contentHash;
        if (this$contentHash == null ? other$contentHash != null
                : !this$contentHash.equals(other$contentHash))
            return false;
        final Object this$pos = this.pos;
        final Object other$pos = other.pos;
        if (this$pos == null ? other$pos != null : !this$pos.equals(other$pos))
            return false;
        final Object this$description = this.description;
        final Object other$description = other.description;
        if (this$description == null ? other$description != null
                : !this$description.equals(other$description))
            return false;
        final Object this$srcLocale = this.srcLocale;
        final Object other$srcLocale = other.srcLocale;
        if (this$srcLocale == null ? other$srcLocale != null
                : !this$srcLocale.equals(other$srcLocale))
            return false;
        final Object this$glossary = this.glossary;
        final Object other$glossary = other.glossary;
        if (this$glossary == null ? other$glossary != null
                : !this$glossary.equals(other$glossary))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HGlossaryEntry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $sourceRef = this.sourceRef;
        result = result * PRIME
                + ($sourceRef == null ? 43 : $sourceRef.hashCode());
        final Object $contentHash = this.contentHash;
        result = result * PRIME
                + ($contentHash == null ? 43 : $contentHash.hashCode());
        final Object $pos = this.pos;
        result = result * PRIME + ($pos == null ? 43 : $pos.hashCode());
        final Object $description = this.description;
        result = result * PRIME
                + ($description == null ? 43 : $description.hashCode());
        final Object $srcLocale = this.srcLocale;
        result = result * PRIME
                + ($srcLocale == null ? 43 : $srcLocale.hashCode());
        final Object $glossary = this.glossary;
        result = result * PRIME
                + ($glossary == null ? 43 : $glossary.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "HGlossaryEntry(sourceRef=" + this.getSourceRef()
                + ", srcLocale=" + this.getSrcLocale() + ")";
    }
}
