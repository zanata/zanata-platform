package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnit implements IsSerializable, Serializable
{

   private static final long serialVersionUID = -8247442475446266600L;

   private ContentState status;

   private TransUnitId id;

   private LocaleId localeId;

   private String source;
   private String sourceComment;
   private String target;
   private String lastModifiedBy;
   private String lastModifiedTime;

   public String getLastModifiedBy()
   {
      return lastModifiedBy;
   }

   public void setLastModifiedBy(String lastModifiedBy)
   {
      this.lastModifiedBy = lastModifiedBy;
   }

   public String getLastModifiedTime()
   {
      return lastModifiedTime;
   }

   public void setLastModifiedTime(String lastModifiedTime)
   {
      this.lastModifiedTime = lastModifiedTime;
   }

   // for GWT
   @SuppressWarnings("unused")
   private TransUnit()
   {
   }
   
   public TransUnit(TransUnitId id, LocaleId localeId)
   {
      this.id = id;
      this.localeId = localeId;
      this.source = "";
      this.sourceComment = "";
      this.target = "";
      this.lastModifiedBy = "";
      this.lastModifiedTime = "";
   }

   public TransUnit(TransUnitId id, LocaleId localeId, String source, String sourceComment, String target, ContentState status, String lastModifiedBy, String lastModifiedTime)
   {
      this.id = id;
      this.localeId = localeId;
      this.source = source;
      this.sourceComment = sourceComment;
      this.target = target;
      this.status = status;
      this.lastModifiedBy = lastModifiedBy;
      this.lastModifiedTime = lastModifiedTime;
   }

   public TransUnit(TransUnit other)
   {
      this(other.id, other.localeId, other.source, other.sourceComment, other.target, other.status, other.lastModifiedBy, other.lastModifiedTime);
   }

   public static final TransUnit copy(TransUnit other)
   {
      return new TransUnit(other);
   }
   
   public TransUnitId getId()
   {
      return id;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public String getSource()
   {
      return source;
   }

   public String getSourceComment()
   {
      return sourceComment;
   }

   public void setSource(String source)
   {
      this.source = source;
   }

   public void setSourceComment(String sourceComment)
   {
      this.sourceComment = sourceComment;
   }

   public String getTarget()
   {
      return target;
   }

   public void setTarget(String target)
   {
      this.target = target;
   }

   public ContentState getStatus()
   {
      return status;
   }

   public void setStatus(ContentState status)
   {
      this.status = status;
   }
}
