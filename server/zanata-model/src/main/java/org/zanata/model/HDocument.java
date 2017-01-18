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

import com.google.common.collect.ImmutableList;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
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
import org.zanata.security.annotations.Authenticated;
import org.zanata.util.Contexts;

import javax.annotation.Nonnull;
import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @see AbstractResourceMeta
 * @see Resource
 * @see ResourceMeta
 * @see TranslationsResource
 *
 */
@Entity
@EntityListeners({HDocument.EntityListener.class})
@Cacheable
@TypeDef(name = "contentType", typeClass = ContentTypeType.class)
public class HDocument extends ModelEntityBase implements DocumentWithId,
        IDocumentHistory, Serializable, Iterable<ITextFlow>, IsEntityWithType {
    private static final long serialVersionUID = 5129552589912687504L;
    private static final Logger log =
            org.slf4j.LoggerFactory.getLogger(HDocument.class);
    private String docId;
    private String name;
    private String path;
    private ContentType contentType;
    private Integer revision = 1;
    private HLocale locale;
    private HPerson lastModifiedBy;

    private HProjectIteration projectIteration;

    private Map<String, HTextFlow> allTextFlows;
    /**
     * NB: Any elements which are removed from this list must have obsolete set
     * to true, and any elements which are added to this list must have obsolete
     * set to false.
     */
    private List<HTextFlow> textFlows;
    private boolean obsolete = false;
    private HPoHeader poHeader;
    private Map<HLocale, HPoTargetHeader> poTargetHeaders;

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

    public HDocument() {
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

    // TODO make this case sensitive
    @NaturalId
    @Size(max = 255)
    @NotEmpty
    public String getDocId() {
        return docId;
    }

    @NotEmpty
    public String getName() {
        return name;
    }

    @Transient
    @Override
    public String getQualifiedDocId() {
        HProjectIteration iter = getProjectIteration();
        HProject proj = iter.getProject();
        return proj.getSlug() + ":" + iter.getSlug() + ":" + getDocId();
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @SuppressWarnings("null")
    @ManyToOne
    @JoinColumn(name = "locale", nullable = false)
    @Override
    public @Nonnull
    HLocale getLocale() {
        return this.locale;
    }

    @Transient
    @Override
    public @Nonnull
    LocaleId getSourceLocaleId() {
        return locale.getLocaleId();
    }

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "project_iteration_id", nullable = false)
    @NaturalId
    public HProjectIteration getProjectIteration() {
        return projectIteration;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_id", nullable = true)
    @Override
    public HPerson getLastModifiedBy() {
        return lastModifiedBy;
    }

    protected void setLastModifiedBy(HPerson lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @NotNull
    public Integer getRevision() {
        return revision;
    }

    @Transient
    public void incrementRevision() {
        revision++;
    }

    @Type(type = "contentType")
    @NotNull
    public ContentType getContentType() {
        return contentType;
    }

    @OneToMany
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    // , nullable = true
    @MapKey(name = "resId")
    /**
     * NB Don't modify this collection.  Add to the TextFlows list instead.
     * TODO get ImmutableMap working here.
     */
    public Map<String, HTextFlow> getAllTextFlows() {
        if (allTextFlows == null) {
            allTextFlows = new HashMap<String, HTextFlow>();
        }
        return allTextFlows;
    }

    @SuppressWarnings("unused")
    // used only by Hibernate
            private
            void setAllTextFlows(Map<String, HTextFlow> allTextFlows) {
        this.allTextFlows = allTextFlows;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @Where(clause = "obsolete=0")
    @IndexColumn(name = "pos", base = 0, nullable = false)
    @JoinColumn(name = "document_id", nullable = false)
    /**
     * NB: Any elements which are removed from this list must have obsolete set
     * to true, and any elements which are added to this list must have obsolete
     * set to false.
     */
    public List<HTextFlow> getTextFlows() {
        if (textFlows == null) {
            textFlows = new ArrayList<HTextFlow>();
        }
        return textFlows;
    }

    @Override
    public Iterator<ITextFlow> iterator() {
        return ImmutableList.<ITextFlow> copyOf(getTextFlows()).iterator();
    }

    public boolean isObsolete() {
        return obsolete;
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            optional = true)
    public HPoHeader getPoHeader() {
        return poHeader;
    }

    // private setter for Hibernate
    @SuppressWarnings("unused")
    private void setPoTargetHeaders(
            Map<HLocale, HPoTargetHeader> poTargetHeaders) {
        this.poTargetHeaders = poTargetHeaders;
    }

    // TODO use orphanRemoval=true: requires JPA 2.0
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            mappedBy = "document", orphanRemoval = true)
    @MapKey(name = "targetLanguage")
    public Map<HLocale, HPoTargetHeader> getPoTargetHeaders() {
        if (poTargetHeaders == null) {
            poTargetHeaders = new HashMap<HLocale, HPoTargetHeader>();
        }
        return poTargetHeaders;
    }

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinTable(name = "HDocument_RawDocument", joinColumns = @JoinColumn(
            name = "documentId"), inverseJoinColumns = @JoinColumn(
            name = "rawDocumentId"))
    public HRawDocument getRawDocument() {
        return rawDocument;
    }

    @Override
    @Transient
    public EntityType getEntityType() {
        return EntityType.HDocument;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public void setLocale(HLocale locale) {
        this.locale = locale;
    }

    public void setProjectIteration(HProjectIteration projectIteration) {
        this.projectIteration = projectIteration;
    }

    public void setTextFlows(List<HTextFlow> textFlows) {
        this.textFlows = textFlows;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public void setPoHeader(HPoHeader poHeader) {
        this.poHeader = poHeader;
    }

    public void setRawDocument(HRawDocument rawDocument) {
        this.rawDocument = rawDocument;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof HDocument)) return false;
        final HDocument other = (HDocument) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$docId = this.getDocId();
        final Object other$docId = other.getDocId();
        if (this$docId == null ? other$docId != null :
                !this$docId.equals(other$docId)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null :
                !this$name.equals(other$name)) return false;
        final Object this$path = this.getPath();
        final Object other$path = other.getPath();
        if (this$path == null ? other$path != null :
                !this$path.equals(other$path)) return false;
        final Object this$contentType = this.getContentType();
        final Object other$contentType = other.getContentType();
        if (this$contentType == null ? other$contentType != null :
                !this$contentType.equals(other$contentType)) return false;
        final Object this$revision = this.getRevision();
        final Object other$revision = other.getRevision();
        if (this$revision == null ? other$revision != null :
                !this$revision.equals(other$revision)) return false;
        final Object this$locale = this.getLocale();
        final Object other$locale = other.getLocale();
        if (this$locale == null ? other$locale != null :
                !this$locale.equals(other$locale)) return false;
        final Object this$lastModifiedBy = this.getLastModifiedBy();
        final Object other$lastModifiedBy = other.getLastModifiedBy();
        if (this$lastModifiedBy == null ? other$lastModifiedBy != null :
                !this$lastModifiedBy.equals(other$lastModifiedBy)) return false;
        final Object this$projectIteration = this.getProjectIteration();
        final Object other$projectIteration = other.getProjectIteration();
        if (this$projectIteration == null ? other$projectIteration != null :
                !this$projectIteration.equals(other$projectIteration))
            return false;
        final Object this$allTextFlows = this.getAllTextFlows();
        final Object other$allTextFlows = other.getAllTextFlows();
        if (this$allTextFlows == null ? other$allTextFlows != null :
                !this$allTextFlows.equals(other$allTextFlows)) return false;
        final Object this$textFlows = this.getTextFlows();
        final Object other$textFlows = other.getTextFlows();
        if (this$textFlows == null ? other$textFlows != null :
                !this$textFlows.equals(other$textFlows)) return false;
        if (this.isObsolete() != other.isObsolete()) return false;
        final Object this$poHeader = this.getPoHeader();
        final Object other$poHeader = other.getPoHeader();
        if (this$poHeader == null ? other$poHeader != null :
                !this$poHeader.equals(other$poHeader)) return false;
        final Object this$poTargetHeaders = this.getPoTargetHeaders();
        final Object other$poTargetHeaders = other.getPoTargetHeaders();
        if (this$poTargetHeaders == null ? other$poTargetHeaders != null :
                !this$poTargetHeaders.equals(other$poTargetHeaders))
            return false;
        final Object this$rawDocument = this.getRawDocument();
        final Object other$rawDocument = other.getRawDocument();
        if (this$rawDocument == null ? other$rawDocument != null :
                !this$rawDocument.equals(other$rawDocument)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $docId = this.getDocId();
        result = result * PRIME + ($docId == null ? 43 : $docId.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $path = this.getPath();
        result = result * PRIME + ($path == null ? 43 : $path.hashCode());
        final Object $contentType = this.getContentType();
        result = result * PRIME +
                ($contentType == null ? 43 : $contentType.hashCode());
        final Object $revision = this.getRevision();
        result = result * PRIME +
                ($revision == null ? 43 : $revision.hashCode());
        final Object $locale = this.getLocale();
        result = result * PRIME + ($locale == null ? 43 : $locale.hashCode());
        final Object $lastModifiedBy = this.getLastModifiedBy();
        result = result * PRIME +
                ($lastModifiedBy == null ? 43 : $lastModifiedBy.hashCode());
        final Object $projectIteration = this.getProjectIteration();
        result = result * PRIME +
                ($projectIteration == null ? 43 : $projectIteration.hashCode());
        final Object $allTextFlows = this.getAllTextFlows();
        result = result * PRIME +
                ($allTextFlows == null ? 43 : $allTextFlows.hashCode());
        final Object $textFlows = this.getTextFlows();
        result = result * PRIME +
                ($textFlows == null ? 43 : $textFlows.hashCode());
        result = result * PRIME + (this.isObsolete() ? 79 : 97);
        final Object $poHeader = this.getPoHeader();
        result = result * PRIME +
                ($poHeader == null ? 43 : $poHeader.hashCode());
        final Object $poTargetHeaders = this.getPoTargetHeaders();
        result = result * PRIME +
                ($poTargetHeaders == null ? 43 : $poTargetHeaders.hashCode());
        final Object $rawDocument = this.getRawDocument();
        result = result * PRIME +
                ($rawDocument == null ? 43 : $rawDocument.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof HDocument;
    }

    public String toString() {
        return "org.zanata.model.HDocument(docId=" + this.getDocId() +
                ", name=" + this.getName() + ", path=" + this.getPath() +
                ", revision=" + this.getRevision() + ", locale=" +
                this.getLocale() + ")";
    }

    public static class EntityListener {

        @PreUpdate
        private void onUpdate(HDocument doc) {
            if (Contexts.isSessionContextActive()) {
                HAccount account;
                try {
                    account = BeanProvider
                            .getContextualReference(HAccount.class,
                                    true,
                                    new AnnotationLiteral<Authenticated>() {
                                    });
                } catch (IllegalStateException e) {
                    account = null;
                }
                if (account != null) {
                    doc.setLastModifiedBy(account.getPerson());
                }
            }
        }
    }

}
