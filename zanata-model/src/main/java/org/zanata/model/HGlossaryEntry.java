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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.zanata.hibernate.search.LocaleIdBridge;
import org.zanata.util.GlossaryUtil;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Entity
@EntityListeners({ HGlossaryEntry.EntityListener.class })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Indexed
@Setter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true,
        exclude = "glossaryTerms")
@ToString(of = { "sourceRef", "srcLocale" })
public class HGlossaryEntry extends ModelEntityBase {
    private static final long serialVersionUID = -4200183325180630061L;

    private Map<HLocale, HGlossaryTerm> glossaryTerms;
    private String sourceRef;

    private String contentHash;
    private String pos;
    private String description;

    private HLocale srcLocale;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "glossaryEntry",
            orphanRemoval = true, fetch = FetchType.EAGER)
    @MapKey(name = "locale")
    public Map<HLocale, HGlossaryTerm> getGlossaryTerms() {
        if (glossaryTerms == null) {
            glossaryTerms = new HashMap<HLocale, HGlossaryTerm>();
        }
        return glossaryTerms;
    }

    @Type(type = "text")
    public String getSourceRef() {
        return sourceRef;
    }

    //TODO: this should be many to one
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
        }

        @PrePersist
        private void prePersist(HGlossaryEntry entry) {
            entry.setContentHash(getHash(entry));
        }

        private String getHash(HGlossaryEntry entry) {
            HLocale srcLocale = entry.srcLocale;
            String sourceContent = "";
            if (entry.getGlossaryTerms().containsKey(srcLocale)) {
                sourceContent =
                        entry.getGlossaryTerms().get(srcLocale).getContent();
            }
            return GlossaryUtil.generateHash(srcLocale.getLocaleId(),
                    sourceContent,
                    entry.getPos(), entry.getDescription());
        }
    }
}
