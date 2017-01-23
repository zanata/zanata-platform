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

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.annotations.NaturalId;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.FilterCacheModeType;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.zanata.hibernate.search.LocaleFilterFactory;
import org.zanata.hibernate.search.LocaleIdBridge;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Cacheable
@Indexed
@FullTextFilterDef(name = "glossaryLocaleFilter",
        impl = LocaleFilterFactory.class,
        cache = FilterCacheModeType.INSTANCE_ONLY)
public class HGlossaryTerm extends ModelEntityBase {
    private static final long serialVersionUID = 1854278563597070432L;
    private String content;
    private String comment;
    private HGlossaryEntry glossaryEntry;
    private HLocale locale;
    private HPerson lastModifiedBy;

    public HGlossaryTerm(String content) {
        setContent(content);
    }

    @NotNull
    @javax.persistence.Lob
    @Field(analyzer = @Analyzer(impl = StandardAnalyzer.class))
    public String getContent() {
        return content;
    }

    public String getComment() {
        return comment;
    }
    // TODO PERF @NaturalId(mutable=false) for better criteria caching

    @NaturalId
    @ManyToOne
    @JoinColumn(name = "glossaryEntryId", nullable = false)
    @IndexedEmbedded
    public HGlossaryEntry getGlossaryEntry() {
        return glossaryEntry;
    }
    // TODO PERF @NaturalId(mutable=false) for better criteria caching

    @NaturalId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localeId", nullable = false)
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = LocaleIdBridge.class)
    public HLocale getLocale() {
        return locale;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "last_modified_by_id", nullable = true)
    public HPerson getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setGlossaryEntry(final HGlossaryEntry glossaryEntry) {
        this.glossaryEntry = glossaryEntry;
    }

    public void setLocale(final HLocale locale) {
        this.locale = locale;
    }

    public void setLastModifiedBy(final HPerson lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public HGlossaryTerm() {
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HGlossaryTerm))
            return false;
        final HGlossaryTerm other = (HGlossaryTerm) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$content = this.content;
        final Object other$content = other.content;
        if (this$content == null ? other$content != null
                : !this$content.equals(other$content))
            return false;
        final Object this$comment = this.comment;
        final Object other$comment = other.comment;
        if (this$comment == null ? other$comment != null
                : !this$comment.equals(other$comment))
            return false;
        final Object this$locale = this.locale;
        final Object other$locale = other.locale;
        if (this$locale == null ? other$locale != null
                : !this$locale.equals(other$locale))
            return false;
        final Object this$lastModifiedBy = this.lastModifiedBy;
        final Object other$lastModifiedBy = other.lastModifiedBy;
        if (this$lastModifiedBy == null ? other$lastModifiedBy != null
                : !this$lastModifiedBy.equals(other$lastModifiedBy))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HGlossaryTerm;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $content = this.content;
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $comment = this.comment;
        result = result * PRIME + ($comment == null ? 43 : $comment.hashCode());
        final Object $locale = this.locale;
        result = result * PRIME + ($locale == null ? 43 : $locale.hashCode());
        final Object $lastModifiedBy = this.lastModifiedBy;
        result = result * PRIME
                + ($lastModifiedBy == null ? 43 : $lastModifiedBy.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "HGlossaryTerm(content=" + this.content + ", comment="
                + this.comment + ", glossaryEntry=" + this.glossaryEntry
                + ", locale=" + this.locale + ", lastModifiedBy="
                + this.lastModifiedBy + ")";
    }
}
