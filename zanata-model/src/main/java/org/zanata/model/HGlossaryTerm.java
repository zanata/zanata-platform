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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.FilterCacheModeType;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.Indexed;
import org.zanata.hibernate.search.LocaleFilterFactory;
import org.zanata.hibernate.search.LocaleIdBridge;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Indexed
@FullTextFilterDef(name = "glossaryLocaleFilter",
        impl = LocaleFilterFactory.class,
        cache = FilterCacheModeType.INSTANCE_ONLY)
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true,
        exclude = "glossaryEntry")
@ToString(doNotUseGetters = true)
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
}
