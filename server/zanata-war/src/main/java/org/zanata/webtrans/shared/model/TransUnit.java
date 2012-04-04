package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.Comparator;

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

   private String source;
   private String sourceComment;
   private String target;
   private String msgContext;
   private String lastModifiedBy;
   private String lastModifiedTime;
   private int rowIndex;

   private static Comparator<TransUnit> rowIndexComparator;

   // for GWT
   @SuppressWarnings("unused")
   private TransUnit()
   {
   }

   public TransUnit(TransUnitId id, String resId, LocaleId localeId, String source, String sourceComment, String target, ContentState status, String lastModifiedBy, String lastModifiedTime, String msgContext, int rowIndex)
   {
      this.id = id;
      this.resId = resId;
      this.localeId = localeId;
      this.source = source;
      this.sourceComment = sourceComment;
      this.target = target;
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

   public static Comparator<TransUnit> getRowIndexComparator()
   {
      if (rowIndexComparator == null)
      {
         rowIndexComparator = new Comparator<TransUnit>()
               {

            @Override
            public int compare(TransUnit o1, TransUnit o2)
            {
               if (o1 == o2)
               {
                  return 0;
               }
               if (o1 != null)
               {
                  return (o2 != null ? new Integer(o1.getRowIndex()).compareTo(o2.getRowIndex()) : 1);
               }
               return -1;
            }
         };
      }
      return rowIndexComparator;
   }

}
