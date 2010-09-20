package net.openl10n.flies.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.model.po.HPoHeader;
import net.openl10n.flies.model.po.HPoTargetHeader;
import net.openl10n.flies.rest.dto.deprecated.Document;
import net.openl10n.flies.rest.dto.deprecated.TextFlow;
import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Contexts;

/**
 * @see AbstractResourceMeta
 * @see Resource
 * @see ResourceMeta
 * @see TranslationsResource
 * 
 */
@Entity
public class HDocument extends AbstractFliesEntity implements IDocumentHistory
{

   private String docId;
   private String name;
   private String path;
   private ContentType contentType;
   private Integer revision = 1;
   private HLocale locale;
   private HPerson lastModifiedBy;

   private HProjectIteration projectIteration;

   private Map<String, HTextFlow> allTextFlows;
   private List<HTextFlow> textFlows;
   private boolean obsolete = false;
   private HPoHeader poHeader;
   private Map<HLocale, HPoTargetHeader> poTargetHeaders;

   public HDocument(String fullPath, ContentType contentType, HLocale locale)
   {
      this.contentType = contentType;
      this.locale = locale;
      setFullPath(fullPath);
   }

   public HDocument(String docId, String name, String path, ContentType contentType, HLocale locale)
   {
      this(docId, name, path, contentType, locale, 1);
   }


   public HDocument(String docId, String name, String path, ContentType contentType, HLocale locale, int revision)
   {
      this.docId = docId;
      this.name = name;
      this.path = path;
      this.contentType = contentType;
      this.locale = locale;
   }

   public HDocument()
   {
   }

   @Deprecated
   public HDocument(Document docInfo, HLocale locale)
   {
      this.docId = docInfo.getId();
      this.name = docInfo.getName();
      this.path = docInfo.getPath();
      this.contentType = docInfo.getContentType();
      this.locale = locale;
      this.revision = docInfo.getRevision();
   }

   public HTextFlow create(TextFlow res, int nextDocRev)
   {
      HTextFlow tf = new HTextFlow(res, nextDocRev);
      getTextFlows().add(tf);
      tf.setDocument(this);
      return tf;
   }

   public void setFullPath(String fullPath)
   {
      this.docId = fullPath;
      int lastSepChar = fullPath.lastIndexOf('/');
      switch (lastSepChar)
      {
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
   @Length(max = 255)
   @NotEmpty
   public String getDocId()
   {
      return docId;
   }

   /**
    * Use setFullPath to ensure consistent parsing of the document id/path
    */
   @Deprecated
   public void setDocId(String docId)
   {
      this.docId = docId;
   }

   @NotEmpty
   public String getName()
   {
      return name;
   }

   /**
    * Use setFullPath to ensure consistent parsing of the document id/path
    */
   @Deprecated
   public void setName(String name)
   {
      this.name = name;
   }

   @NotNull
   public String getPath()
   {
      return path;
   }

   /**
    * Use setFullPath to ensure consistent parsing of the document id/path
    */
   @Deprecated
   public void setPath(String path)
   {
      this.path = path;
   }

   @ManyToOne
   @JoinColumn(name = "locale", nullable = false)
   public HLocale getLocale()
   {
      return this.locale;
   }

   public void setLocale(HLocale locale)
   {
      this.locale = locale;
   }

   @ManyToOne(cascade = CascadeType.PERSIST)
   @JoinColumn(name = "project_iteration_id", nullable = false)
   @NaturalId
   public HProjectIteration getProjectIteration()
   {
      return projectIteration;
   }

   public void setProjectIteration(HProjectIteration projectIteration)
   {
      this.projectIteration = projectIteration;
   }

   @ManyToOne
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   @Override
   public HPerson getLastModifiedBy()
   {
      return lastModifiedBy;
   }

   protected void setLastModifiedBy(HPerson lastModifiedBy)
   {
      this.lastModifiedBy = lastModifiedBy;
   }

   @NotNull
   public Integer getRevision()
   {
      return revision;
   }

   public void setRevision(Integer revision)
   {
      this.revision = revision;
   }

   @Transient
   public void incrementRevision()
   {
      revision++;
   }

   @Type(type = "contentType")
   @NotNull
   public ContentType getContentType()
   {
      return contentType;
   }

   public void setContentType(ContentType contentType)
   {
      this.contentType = contentType;
   }

   @OneToMany
   @JoinColumn(name = "document_id", insertable = false, updatable = false/*
                                                                           * ,
                                                                           * nullable
                                                                           * =
                                                                           * true
                                                                           */)
   @MapKey(name = "resId")
   /**
    * NB Don't modify this collection.  Add to the TextFlows list instead.
    * TODO get ImmutableMap working here.
    */
   public Map<String, HTextFlow> getAllTextFlows()
   {
      if (allTextFlows == null)
         allTextFlows = new HashMap<String, HTextFlow>();
      return allTextFlows;
   }

   @SuppressWarnings("unused")
   // used only by Hibernate
   private void setAllTextFlows(Map<String, HTextFlow> allTextFlows)
   {
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
   public List<HTextFlow> getTextFlows()
   {
      if (textFlows == null)
         textFlows = new ArrayList<HTextFlow>();
      return textFlows;
      // return ImmutableList.copyOf(textFlows);
   }

   /**
    * NB: Any elements which are removed from this list must have obsolete set
    * to true, and any elements which are added to this list must have obsolete
    * set to false.
    */
   public void setTextFlows(List<HTextFlow> textFlows)
   {
      this.textFlows = textFlows;
   }

   public boolean isObsolete()
   {
      return obsolete;
   }

   public void setObsolete(boolean obsolete)
   {
      this.obsolete = obsolete;
   }


   public void setPoHeader(HPoHeader poHeader)
   {
      this.poHeader = poHeader;
   }

   @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
   public HPoHeader getPoHeader()
   {
      return poHeader;
   }

   public void setPoTargetHeaders(Map<HLocale, HPoTargetHeader> poTargetHeaders)
   {
      this.poTargetHeaders = poTargetHeaders;
   }

   @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "document")
   @MapKey(name = "targetLanguage")
   public Map<HLocale, HPoTargetHeader> getPoTargetHeaders()
   {
      if (poTargetHeaders == null)
         poTargetHeaders = new HashMap<HLocale, HPoTargetHeader>();
      return poTargetHeaders;
   }


   /**
    * Used for debugging
    */
   public String toString()
   {
      return String.format("HDocument(name:%s path:%s docID:%s locale:%s rev:%d)", getName(), getPath(), getDocId(), getLocale(), getRevision());
   }

   @PreUpdate
   public void onUpdate()
   {
      if (Contexts.isSessionContextActive())
      {
         HPerson person = (HPerson) Component.getInstance("authenticatedPerson", ScopeType.SESSION);
         setLastModifiedBy(person);
      }
   }

}
