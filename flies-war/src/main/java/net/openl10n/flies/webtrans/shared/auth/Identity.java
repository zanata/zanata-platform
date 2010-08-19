package net.openl10n.flies.webtrans.shared.auth;

import java.io.Serializable;
import java.util.HashSet;

import net.openl10n.flies.webtrans.shared.model.Person;

public class Identity implements Serializable
{

   private static final long serialVersionUID = 1L;

   private SessionId sessionId;
   private HashSet<Permission> permissions = new HashSet<Permission>();
   private HashSet<Role> roles = new HashSet<Role>();
   private Person person;

   @SuppressWarnings("unused")
   private Identity()
   {
   }

   public Identity(SessionId sessionId, Person person, HashSet<Permission> permissions, HashSet<Role> roles)
   {
      this.sessionId = sessionId;
      this.person = person;
      this.permissions = permissions;
      this.roles = roles;

   }

   public boolean hasRole(Role role)
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
