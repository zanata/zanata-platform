package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.Person;

//@ExposeEntity 
public class EnterWorkspace implements SessionEventData, HasEnterWorkspaceData
{
   private static final long serialVersionUID = 1L;

   private Person person;

   // for ExposeEntity
   public EnterWorkspace()
   {

   }

   public EnterWorkspace(Person person)
   {
      this.person = person;
   }

   @Override
   public Person getPerson()
   {
      return person;
   }
}
