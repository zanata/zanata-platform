package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;

public class TransUnitEdit implements SessionEventData, HasTransUnitEditData
{
   private static final long serialVersionUID = 5332535583909340461L;
   private Person person;
   private TransUnit selectedTransUnit;
   private SessionId sessionId;

   public TransUnitEdit(SessionId sessionId, Person person, TransUnit selectedTransUnit)
   {
      this.sessionId = sessionId;
      this.person = person;
      this.selectedTransUnit = selectedTransUnit;
   }

   // for ExposeEntity
   public TransUnitEdit()
   {
   }

   @Override
   public Person getPerson()
   {
      return person;
   }

   @Override
   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }

   @Override
   public SessionId getSessionId()
   {
      return sessionId;
   }
}