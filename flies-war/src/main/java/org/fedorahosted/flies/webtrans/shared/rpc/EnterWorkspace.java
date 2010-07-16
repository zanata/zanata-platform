package org.fedorahosted.flies.webtrans.shared.rpc;

import org.fedorahosted.flies.webtrans.shared.model.PersonId;

//@ExposeEntity 
public class EnterWorkspace implements SessionEventData, HasEnterWorkspaceData
{
   private static final long serialVersionUID = 1L;

   private PersonId personId;

   // for ExposeEntity
   public EnterWorkspace()
   {

   }

   public EnterWorkspace(PersonId personId)
   {
      this.personId = personId;
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
