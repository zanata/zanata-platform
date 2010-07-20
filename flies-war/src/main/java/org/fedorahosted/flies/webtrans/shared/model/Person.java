package org.fedorahosted.flies.webtrans.shared.model;

import java.io.Serializable;

public class Person implements HasIdentifier<PersonId>, Serializable
{

   private static final long serialVersionUID = 1L;

   private PersonId id;
   private String name;

   @SuppressWarnings("unused")
   private Person()
   {
   }

   public Person(PersonId id, String name)
   {
      if (id == null || name == null)
      {
         throw new IllegalStateException("id/name cannot be null");
      }
      this.id = id;
      this.name = name;
   }

   public PersonId getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

}
