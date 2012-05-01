package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;

//@ExposeEntity 
public class ExitWorkspace implements SessionEventData, HasExitWorkspaceData
{
   private static final long serialVersionUID = 1L;

   private Person person;
   private SessionId sessionId;

   // for ExposeEntity
   public ExitWorkspace()
   {
      // TODO Auto-generated constructor stub
   }

   public ExitWorkspace(SessionId sessionId, Person person)
   {
      this.person = person;
      this.sessionId = sessionId;
   }

   @Override
   public Person getPerson()
   {
      return person;
   }

   @Override
   public SessionId getSessionId()
   {
      return sessionId;
   }
}
