package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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

   public static class TransUnitBuilder
   {
      private ContentState status = ContentState.New;
      private TransUnitId id;
      private String resId;
      private LocaleId localeId;
      private boolean plural;
      private ArrayList<String> sources = Lists.newArrayList();
      private String sourceComment;
      private ArrayList<String> targets = Lists.newArrayList();
      private String msgContext;
      private String lastModifiedBy;
      private String lastModifiedTime;
      private int rowIndex;

      public TransUnitBuilder(TransUnit transUnit)
      {
         this.status = transUnit.status;
         this.id = transUnit.id;
         this.resId = transUnit.resId;
         this.localeId = transUnit.localeId;
         this.plural = transUnit.plural;
         this.sources = nullToEmpty(transUnit.sources);
         this.sourceComment = transUnit.sourceComment;
         this.targets = nullToEmpty(transUnit.targets);
         this.msgContext = transUnit.msgContext;
         this.lastModifiedBy = transUnit.lastModifiedBy;
         this.lastModifiedTime = transUnit.lastModifiedTime;
         this.rowIndex = transUnit.rowIndex;
      }

      private TransUnitBuilder()
      {
      }

      public TransUnit build()
      {
         Preconditions.checkNotNull(id, "transUnitId can not be null");
         Preconditions.checkNotNull(resId, "resId can not be null");
         Preconditions.checkNotNull(localeId, "localeId can not be null");
         Preconditions.checkState(sources != null && !sources.isEmpty());
         Preconditions.checkState(rowIndex >= 0);

         lastModifiedBy = Strings.nullToEmpty(lastModifiedBy);
         lastModifiedTime = Strings.nullToEmpty(lastModifiedTime);

         return new TransUnit(id, resId, localeId, plural, sources, sourceComment, targets, status, lastModifiedBy, lastModifiedTime, msgContext, rowIndex);
      }

      public static TransUnitBuilder builder()
      {
         return new TransUnitBuilder();
      }

      private static ArrayList<String> nullToEmpty(ArrayList<String> contents)
      {
         return contents == null ? Lists.<String>newArrayList() : contents;
      }

      public TransUnitBuilder setStatus(ContentState status)
      {
         this.status = status;
         return this;
      }

      public TransUnitBuilder setId(TransUnitId id)
      {
         this.id = id;
         return this;
      }

      public TransUnitBuilder setId(long id)
      {
         this.id = new TransUnitId(id);
         return this;
      }

      public TransUnitBuilder setResId(String resId)
      {
         this.resId = resId;
         return this;
      }

      public TransUnitBuilder setLocaleId(LocaleId localeId)
      {
         this.localeId = localeId;
         return this;
      }

      public TransUnitBuilder setLocaleId(String localeString)
      {
         this.localeId = new LocaleId(localeString);
         return this;
      }

      public TransUnitBuilder setPlural(boolean plural)
      {
         this.plural = plural;
         return this;
      }

      public TransUnitBuilder setSources(ArrayList<String> sources)
      {
         this.sources = nullToEmpty(sources);
         return this;
      }

      public TransUnitBuilder addSource(String... sourceStrings)
      {
         Collections.addAll(sources, sourceStrings);
         return this;
      }

      public TransUnitBuilder setSourceComment(String sourceComment)
      {
         this.sourceComment = sourceComment;
         return this;
      }

      public TransUnitBuilder setTargets(ArrayList<String> targets)
      {
         this.targets = nullToEmpty(targets);
         return this;
      }

      public TransUnitBuilder addTargets(String... targetStrings)
      {
         Collections.addAll(targets, targetStrings);
         return this;
      }

      public TransUnitBuilder setMsgContext(String msgContext)
      {
         this.msgContext = msgContext;
         return this;
      }

      public TransUnitBuilder setLastModifiedBy(String lastModifiedBy)
      {
         this.lastModifiedBy = lastModifiedBy;
         return this;
      }

      public TransUnitBuilder setLastModifiedTime(String lastModifiedTime)
      {
         this.lastModifiedTime = lastModifiedTime;
         return this;
      }

      public TransUnitBuilder setRowIndex(int rowIndex)
      {
         this.rowIndex = rowIndex;
         return this;
      }
   }
}
