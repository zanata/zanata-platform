package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnit implements IsSerializable, Serializable
{
   private static final long serialVersionUID = -8247442475446266600L;

   private ContentState status;

   private TransUnitId id;
   private String resId;

   private LocaleId localeId;

   private List<String> sources;
   private String sourceComment;
   private List<String> targets;
   private String msgContext;
   private String lastModifiedBy;
   private String lastModifiedTime;
   private int rowIndex;

   // for GWT
   @SuppressWarnings("unused")
   private TransUnit()
   {
   }

   public TransUnit(TransUnitId id, String resId, LocaleId localeId, List<String> sources, String sourceComment, List<String> targets, ContentState status, String lastModifiedBy, String lastModifiedTime, String msgContext, int rowIndex)
   {
      this.id = id;
      this.resId = resId;
      this.localeId = localeId;
      this.sources = sources;
      this.sourceComment = sourceComment;
      this.targets = targets;
      this.status = status;
      this.lastModifiedBy = lastModifiedBy;
      this.lastModifiedTime = lastModifiedTime;
      this.msgContext = msgContext;
      this.rowIndex = rowIndex;
   }
   
   public TransUnitId getId()
   {
      return id;
   }

   public String getResId()
   {
      return resId;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public List<String> getSources()
   {
      return sources;
   }

   public void setSources(List<String> sources)
   {
      this.sources = sources;
   }

   public String getSourceComment()
   {
      return sourceComment;
   }

   public void setSourceComment(String sourceComment)
   {
      this.sourceComment = sourceComment;
   }

   public List<String> getTargets()
   {
      return targets;
   }

   public void setTargets(List<String> targets)
   {
      this.targets = targets;
   }

   public ContentState getStatus()
   {
      return status;
   }

   public void setStatus(ContentState status)
   {
      this.status = status;
   }

   public String getMsgContext()
   {
      return msgContext;
   }

   public void setMsgContext(String msgContext)
   {
      this.msgContext = msgContext;
   }

   public int getRowIndex()
   {
      return rowIndex;
   }

   public void setRowIndex(int rowIndex)
   {
      this.rowIndex = rowIndex;
   }

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

}
