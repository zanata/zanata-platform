package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.Person;

//@ExposeEntity 
public class ExitWorkspace implements SessionEventData, HasExitWorkspaceData
{
   private static final long serialVersionUID = 1L;

   private Person person;

   // for ExposeEntity
   public ExitWorkspace()
   {
      // TODO Auto-generated constructor stub
   }

   public ExitWorkspace(Person person)
   {
      this.person = person;
   }

   @Override
   public Person getPerson()
   {
      return person;
   }
}
