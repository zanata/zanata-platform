package org.zanata.webtrans.shared.model;

import java.io.Serializable;

public class Person implements HasIdentifier<PersonId>, Serializable
{
   private static final long serialVersionUID = 510785473431813586L;

   private PersonId id;
   private String name;
   private String avatarUrl;

   // for GWT
   @SuppressWarnings("unused")
   private Person()
   {
   }

   public Person(PersonId id, String name, String avatarUrl)
   {
      if (id == null || name == null)
      {
         throw new IllegalStateException("id/name cannot be null");
      }
      this.id = id;
      this.name = name;
      this.avatarUrl = avatarUrl;
   }

   public PersonId getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public String getAvatarUrl()
   {
      return avatarUrl == null ? "" : avatarUrl;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((avatarUrl == null) ? 0 : avatarUrl.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Person other = (Person) obj;
      if (avatarUrl == null)
      {
         if (other.avatarUrl != null)
            return false;
      }
      else if (!avatarUrl.equals(other.avatarUrl))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      return true;
   }
}
