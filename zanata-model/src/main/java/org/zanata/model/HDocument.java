/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.model.po.HPoHeader;
import org.zanata.model.po.HPoTargetHeader;
import org.zanata.model.type.ContentTypeType;
import org.zanata.model.type.EntityType;
import org.zanata.rest.dto.resource.AbstractResourceMeta;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @see AbstractResourceMeta
 * @see Resource
 * @see ResourceMeta
 * @see TranslationsResource
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@TypeDef(name = "contentType", typeClass = ContentTypeType.class)
@Setter
@Getter
@Access(AccessType.FIELD)
@NoArgsConstructor
@ToString(of = { "name", "path", "docId", "locale", "revision" })
public class HDocument extends ModelEntityBase implements DocumentWithId,
        IDocumentHistory, Serializable, Iterable<ITextFlow>, IsEntityWithType {
    private static final long serialVersionUID = 5129552589912687504L;

    // TODO make this case sensitive
    @NaturalId
    @Size(max = 255)
    @NotEmpty
    private String docId;

    @NotEmpty
    private String name;

    @NotNull
    private String path;

    @Type(type = "contentType")
    @NotNull
    private ContentType contentType;

    @NotNull
    private Integer revision = 1;

    @SuppressWarnings("null")
    @ManyToOne
    @JoinColumn(name = "locale", nullable = false)
    private @Nonnull
    HLocale locale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_id", nullable = true)
    @Setter(AccessLevel.PROTECTED)
    private HPerson lastModifiedBy;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "project_iteration_id", nullable = false)
    @NaturalId
    private HProjectIteration projectIteration;

    @OneToMany
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    @MapKey(name = "resId")
    /**
     * NB Don't modify this collection.  Add to the TextFlows list instead.
     * TODO get ImmutableMap working here.
     */
    @Setter(AccessLevel.PRIVATE)
    private Map<String, HTextFlow> allTextFlows = Maps.newHashMap();

    @OneToMany(cascade = CascadeType.ALL)
    @Where(clause = "obsolete=0")
    @IndexColumn(name = "pos", base = 0, nullable = false)
    @JoinColumn(name = "document_id", nullable = false)
    /**
     * NB: Any elements which are removed from this list must have obsolete set
     * to true, and any elements which are added to this list must have obsolete
     * set to false.
     */
    private List<HTextFlow> textFlows = Lists.newArrayList();

    private boolean obsolete = false;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            optional = true)
    private HPoHeader poHeader;

    // TODO use orphanRemoval=true: requires JPA 2.0
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            mappedBy = "document")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @MapKey(name = "targetLanguage")
    @Setter(AccessLevel.PRIVATE)
    private Map<HLocale, HPoTargetHeader> poTargetHeaders = Maps.newHashMap();

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinTable(name = "HDocument_RawDocument", joinColumns = @JoinColumn(
            name = "documentId"), inverseJoinColumns = @JoinColumn(
            name = "rawDocumentId"))
    private HRawDocument rawDocument;

    public HDocument(String fullPath, ContentType contentType, HLocale locale) {
        this.contentType = contentType;
        this.locale = locale;
        setFullPath(fullPath);
    }

    public HDocument(String docId, String name, String path,
            ContentType contentType, HLocale locale) {
        this.docId = docId;
        this.name = name;
        this.path = path;
        this.contentType = contentType;
        this.locale = locale;
    }

    public void setFullPath(String fullPath) {
        this.docId = fullPath;
        int lastSepChar = fullPath.lastIndexOf('/');
        switch (lastSepChar) {
        case -1:
            this.path = "";
            this.name = fullPath;
            break;
        case 0:
            this.path = "/";
            this.name = fullPath.substring(1);
            break;
        default:
            this.path = fullPath.substring(0, lastSepChar + 1);
            this.name = fullPath.substring(lastSepChar + 1);
        }
    }

    @Transient
    @Override
    public String getQualifiedDocId() {
        HProjectIteration iter = getProjectIteration();
        HProject proj = iter.getProject();
        return proj.getSlug() + ":" + iter.getSlug() + ":" + getDocId();
    }

    @Transient
    @Override
    public @Nonnull
    LocaleId getSourceLocaleId() {
        return locale.getLocaleId();
    }

    @Transient
    public void incrementRevision() {
        revision++;
    }

    @Override
    public Iterator<ITextFlow> iterator() {
        return ImmutableList.<ITextFlow> copyOf(getTextFlows()).iterator();
    }

    @PreUpdate
    public void onUpdate() {
        if (Contexts.isSessionContextActive()) {
            HAccount account =
                    (HAccount) Component.getInstance(
                            JpaIdentityStore.AUTHENTICATED_USER,
                            ScopeType.SESSION);
            // TODO In some cases there is no session ( such as when pushing
            // async )
            if (account != null) {
                setLastModifiedBy(account.getPerson());
            }
        }
    }

    @Override
    @Transient
    public EntityType getEntityType() {
        return EntityType.HDocument;
    }

}
