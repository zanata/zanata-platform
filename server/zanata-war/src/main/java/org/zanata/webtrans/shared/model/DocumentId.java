package org.zanata.webtrans.shared.model;

import java.io.Serializable;

public class DocumentId implements Identifier<Long>, Serializable
{

   private static final long serialVersionUID = 6291339842619640513L;

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

   public void setId(long id)
   {
      this.id = id;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null)
         return false;
      if (obj instanceof DocumentId)
      {
         return ((DocumentId) obj).id == id;
      }
      return false;
   }
}
