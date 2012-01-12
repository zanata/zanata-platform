package org.zanata.webtrans.shared.auth;

import java.io.Serializable;
import java.util.HashSet;

import org.zanata.webtrans.shared.model.Person;


public class Identity implements Serializable
{

   private static final long serialVersionUID = 1L;

   private SessionId sessionId;
   private HashSet<Permission> permissions = new HashSet<Permission>();
   private HashSet<String> roles = new HashSet<String>();
   private Person person;

   @SuppressWarnings("unused")
   private Identity()
   {
   }

   public Identity(SessionId sessionId, Person person, HashSet<Permission> permissions, HashSet<String> roles)
   {
      this.sessionId = sessionId;
      this.person = person;
      this.permissions = permissions;
      this.roles = roles;

   }

   public boolean hasRole(String role)
   {
      return roles.contains(role);
   }

   public boolean hasPermission(Permission permission)
   {
      return permissions.contains(permission);
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
