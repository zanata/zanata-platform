package org.zanata.webtrans.shared.model;

import java.io.Serializable;

public class PersonSessionDetails implements Serializable
{
   private static final long serialVersionUID = -3927926641310793285L;

   private Person person;
   private TransUnit selectedTransUnit;

   // for GWT
   @SuppressWarnings("unused")
   private PersonSessionDetails()
   {
   }

   public PersonSessionDetails(Person person, TransUnit selectedTransUnit)
   {
      this.person = person;
      this.selectedTransUnit = selectedTransUnit;
   }

   public Person getPerson()
   {
      return person;
   }

   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }

   public void setSelectedTransUnit(TransUnit selectedTransUnit)
   {
      this.selectedTransUnit = selectedTransUnit;
   }

   public void setPerson(Person person)
   {
      this.person = person;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((person == null) ? 0 : person.hashCode());
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
      PersonSessionDetails other = (PersonSessionDetails) obj;
      if (person == null)
      {
         if (other.person != null)
            return false;
      }
      else if (!person.equals(other.person))
         return false;
      return true;
   }

}
