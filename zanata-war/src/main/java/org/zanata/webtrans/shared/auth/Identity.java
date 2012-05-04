package org.zanata.webtrans.shared.auth;

import java.io.Serializable;

import org.zanata.webtrans.shared.model.Person;


public class Identity implements Serializable
{

   private static final long serialVersionUID = 1L;

   private SessionId sessionId;
   private Person person;

   @SuppressWarnings("unused")
   private Identity()
   {
   }

   public Identity(SessionId sessionId, Person person)
   {
      this.sessionId = sessionId;
      this.person = person;
   }

   public Person getPerson()
   {
      return person;
   }

   public SessionId getSessionId()
   {
      return sessionId;
   }

}
