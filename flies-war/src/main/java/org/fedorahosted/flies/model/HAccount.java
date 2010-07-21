package org.fedorahosted.flies.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.fedorahosted.flies.security.UserApiKey;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Length;
import org.jboss.seam.annotations.security.management.UserEnabled;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.security.management.UserRoles;
import org.jboss.seam.security.management.PasswordHash;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class HAccount extends AbstractFliesEntity implements Serializable
{

   private String username;

   private String passwordHash;

   private boolean enabled;

   private String apiKey;

   private HPerson person;

   private Set<HAccountRole> roles;

   @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
   public HPerson getPerson()
   {
      return person;
   }

   public void setPerson(HPerson person)
   {
      this.person = person;
   }

   @NaturalId
   @UserPrincipal
   public String getUsername()
   {
      return username;
   }

   @Transient
   public boolean isPersonAccount()
   {
      return person != null;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   @UserPassword(hash = PasswordHash.ALGORITHM_MD5)
   public String getPasswordHash()
   {
      return passwordHash;
   }

   public void setPasswordHash(String passwordHash)
   {
      this.passwordHash = passwordHash;
   }

   @UserEnabled
   public boolean isEnabled()
   {
      return enabled;
   }

   public void setEnabled(boolean enabled)
   {
      this.enabled = enabled;
   }

   @UserApiKey
   @Length(min = 32, max = 32)
   public String getApiKey()
   {
      return apiKey;
   }

   public void setApiKey(String key)
   {
      this.apiKey = key;
   }

   @UserRoles
   @ManyToMany(targetEntity = HAccountRole.class, cascade = CascadeType.ALL)
   @JoinTable(name = "HAccountMembership", joinColumns = @JoinColumn(name = "accountId"), inverseJoinColumns = @JoinColumn(name = "memberOf"))
   public Set<HAccountRole> getRoles()
   {
      if (roles == null)
      {
         roles = new HashSet<HAccountRole>();
         setRoles(roles);
      }
      return roles;
   }

   public void setRoles(Set<HAccountRole> roles)
   {
      this.roles = roles;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + (enabled ? 1231 : 1237);
      result = prime * result + ((passwordHash == null) ? 0 : passwordHash.hashCode());
      result = prime * result + ((username == null) ? 0 : username.hashCode());
      result = prime * result + ((apiKey == null) ? 0 : apiKey.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (getClass() != obj.getClass())
         return false;
      HAccount other = (HAccount) obj;
      if (enabled != other.enabled)
         return false;
      if (passwordHash == null)
      {
         if (other.passwordHash != null)
            return false;
      }
      else if (!passwordHash.equals(other.passwordHash))
         return false;
      if (username == null)
      {
         if (other.username != null)
            return false;
      }
      else if (!username.equals(other.username))
         return false;
      if (apiKey == null)
      {
         if (other.apiKey != null)
            return false;
      }
      else if (!apiKey.equals(other.apiKey))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return super.toString() + "[username=" + username + "]";
   }

}
