package net.openl10n.flies.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.type.ContentTypeType;
import net.openl10n.flies.model.type.LocaleIdType;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
@TypeDefs( { @TypeDef(name = "localeId", typeClass = LocaleIdType.class), @TypeDef(name = "contentType", typeClass = ContentTypeType.class) })
@org.hibernate.annotations.Entity(mutable = false)
public class HDocumentHistory implements IDocumentHistory
{

   private String docId;
   private String name;
   private String path;
   private ContentType contentType;
   private Integer revision;
   private LocaleId locale;
   private HPerson lastModifiedBy;
   protected Long id;
   protected Date lastChanged;
   private boolean obsolete;
   private HDocument document;

   @Id
   @GeneratedValue
   public Long getId()
   {
      return id;
   }

   protected void setId(Long id)
   {
      this.id = id;
   }

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "document_id")
   public HDocument getDocument()
   {
      return document;
   }

   public void setDocument(HDocument document)
   {
      this.document = document;
   }

   @NaturalId
   public Integer getRevision()
   {
      return revision;
   }

   public void setRevision(Integer revision)
   {
      this.revision = revision;
   }

   @Length(max = 255)
   @NotEmpty
   public String getDocId()
   {
      return docId;
   }

   public void setDocId(String docId)
   {
      this.docId = docId;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   @NotNull
   @Type(type = "localeId")
   public LocaleId getLocale()
   {
      return locale;
   }

   public void setLocale(LocaleId locale)
   {
      this.locale = locale;
   }

   @ManyToOne
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   @Override
   public HPerson getLastModifiedBy()
   {
      return lastModifiedBy;
   }

   public void setLastModifiedBy(HPerson lastModifiedBy)
   {
      this.lastModifiedBy = lastModifiedBy;
   }

   public Date getLastChanged()
   {
      return lastChanged;
   }

   public void setLastChanged(Date lastChanged)
   {
      this.lastChanged = lastChanged;
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

   public boolean isObsolete()
   {
      return obsolete;
   }

   public void setObsolete(boolean obsolete)
   {
      this.obsolete = obsolete;
   }

}
