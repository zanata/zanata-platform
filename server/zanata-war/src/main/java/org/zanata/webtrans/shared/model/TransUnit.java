package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
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

   private boolean plural;
   private ArrayList<String> sources;
   private String sourceComment;
   private ArrayList<String> targets;
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

   public TransUnit(TransUnitId id, String resId, LocaleId localeId, boolean plural, ArrayList<String> sources, String sourceComment, ArrayList<String> targets, ContentState status, String lastModifiedBy, String lastModifiedTime, String msgContext, int rowIndex)
   {
      this.id = id;
      this.resId = resId;
      this.localeId = localeId;
      this.plural = plural;
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

   /**
    * @return the pluralSupported
    */
   public boolean isPlural()
   {
      return plural;
   }

   /**
    * @param plural the plural to set
    */
   public void setPlural(boolean plural)
   {
      this.plural = plural;
   }

   public ArrayList<String> getSources()
   {
      return sources;
   }

   public void setSources(ArrayList<String> sources)
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

   public ArrayList<String> getTargets()
   {
      return targets;
   }

   public void setTargets(ArrayList<String> targets)
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
