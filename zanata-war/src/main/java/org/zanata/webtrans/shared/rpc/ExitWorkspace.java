package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.PersonId;

//@ExposeEntity 
public class ExitWorkspace implements SessionEventData, HasExitWorkspaceData
{
   private static final long serialVersionUID = 1L;

   private PersonId personId;

   // for ExposeEntity
   public ExitWorkspace()
   {
      // TODO Auto-generated constructor stub
   }

   public ExitWorkspace(PersonId personId)
   {
   }

   @Override
   public PersonId getPersonId()
   {
      return personId;
   }

   public void setPersonId(PersonId personId)
   {
      this.personId = personId;
   }

}
