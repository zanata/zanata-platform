package net.openl10n.flies.webtrans.shared.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnitId implements Serializable, IsSerializable, Identifier<Long>
{

   private static final long serialVersionUID = 6291339842619640513L;

   private long id;

   // for GWT
   @SuppressWarnings("unused")
   private TransUnitId()
   {
   }

   public TransUnitId(long id)
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
      if (obj instanceof TransUnitId)
      {
         return ((TransUnitId) obj).id == id;
      }
      return false;
   }

}
