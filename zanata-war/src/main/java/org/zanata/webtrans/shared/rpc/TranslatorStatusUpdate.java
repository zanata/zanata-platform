package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;

public class TranslatorStatusUpdate implements SessionEventData, HasTranslatorStatusUpdateData
{
   private static final long serialVersionUID = 5332535583909340461L;
   private Person person;
   private TransUnit selectedTransUnit;
   private SessionId sessionId;

   public TranslatorStatusUpdate(SessionId sessionId, Person person, TransUnit selectedTransUnit)
   {
      this.sessionId = sessionId;
      this.person = person;
      this.selectedTransUnit = selectedTransUnit;
   }

   // for ExposeEntity
   public TranslatorStatusUpdate()
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