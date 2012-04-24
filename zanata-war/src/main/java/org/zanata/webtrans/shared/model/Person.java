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
   public boolean equals(Object obj)
   {
      Person other = (Person) obj;
      return (id.equals(other.getId())) && (avatarUrl.equals(other.getAvatarUrl()));
   }
}
