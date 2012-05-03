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
}
