package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DocumentId implements Identifier<Long>, IsSerializable
{
   private long id;

   // for GWT
   @SuppressWarnings("unused")
   private DocumentId()
   {
   }

   public DocumentId(long id)
   {
      this.id = id;
   }

   @Override
   public String toString()
   {
      return String.valueOf(id);
   }

   @Override
   public int hashCode()
   {
      return (int) id;
   }

   @Override
   public Long getValue()
   {
      return id;
   }

   public long getId()
   {
      return id;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      return obj instanceof DocumentId && ((DocumentId) obj).id == id;
   }
}
