package net.openl10n.flies.rest.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.MediaTypes;
import net.openl10n.flies.rest.MediaTypes.Format;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@XmlType(name = "accountType")
@XmlRootElement(name = "account")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(
{"email", "name", "username", "password"})
@JsonWriteNullProperties(false)
public class Account implements Serializable, HasMediaType
{

   private String email;

   private String name;

   private String username;

   private String passwordHash;

   private String apiKey;

   private boolean enabled;

   private Set<String> roles = new HashSet<String>();

   private Set<String> tribes = new HashSet<String>();

   public Account()
   {
   }

   public Account(String email, String name, String username, String passwordHash)
   {
      this.email = email;
      this.name = name;
      this.username = username;
      this.passwordHash = passwordHash;
   }

   @XmlAttribute(name = "email", required = true)
   @Email
   @NotNull
   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   @XmlAttribute(name = "name", required = true)
   @NotEmpty
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @XmlAttribute(name = "username", required = true)
   @NotEmpty
   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   @XmlAttribute(name = "passwordHash", required = true)
   @NotEmpty
   public String getPasswordHash()
   {
      return passwordHash;
   }

   public void setPasswordHash(String password)
   {
      this.passwordHash = password;
   }

   @XmlAttribute(name = "apiKey", required = false)
   @Length(min = 32, max = 32)
   public String getApiKey()
   {
      return apiKey;
   }

   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
   }

   @XmlAttribute(name = "enabled", required = true)
   @NotNull
   public boolean isEnabled()
   {
      return enabled;
   }

   public void setEnabled(boolean enabled)
   {
      this.enabled = enabled;
   }

   @XmlElement(name = "role")
   public Set<String> getRoles()
   {
      return roles;
   }

   public void setRoles(Set<String> roles)
   {
      this.roles = roles;
   }

   @XmlElement(name = "tribe")
   public Set<String> getTribes()
   {
      return tribes;
   }

   public void setTribes(Set<String> tribes)
   {
      this.tribes = tribes;
   }

   @Override
   public String getMediaType(Format format)
   {
      return MediaTypes.APPLICATION_FLIES_ACCOUNT + format;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((apiKey == null) ? 0 : apiKey.hashCode());
      result = prime * result + ((email == null) ? 0 : email.hashCode());
      result = prime * result + (enabled ? 1231 : 1237);
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((passwordHash == null) ? 0 : passwordHash.hashCode());
      result = prime * result + ((roles == null) ? 0 : roles.hashCode());
      result = prime * result + ((tribes == null) ? 0 : tribes.hashCode());
      result = prime * result + ((username == null) ? 0 : username.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof Account))
      {
         return false;
      }
      Account other = (Account) obj;
      if (apiKey == null)
      {
         if (other.apiKey != null)
         {
            return false;
         }
      }
      else if (!apiKey.equals(other.apiKey))
      {
         return false;
      }
      if (email == null)
      {
         if (other.email != null)
         {
            return false;
         }
      }
      else if (!email.equals(other.email))
      {
         return false;
      }
      if (enabled != other.enabled)
      {
         return false;
      }
      if (name == null)
      {
         if (other.name != null)
         {
            return false;
         }
      }
      else if (!name.equals(other.name))
      {
         return false;
      }
      if (passwordHash == null)
      {
         if (other.passwordHash != null)
         {
            return false;
         }
      }
      else if (!passwordHash.equals(other.passwordHash))
      {
         return false;
      }
      if (roles == null)
      {
         if (other.roles != null)
         {
            return false;
         }
      }
      else if (!roles.equals(other.roles))
      {
         return false;
      }
      if (tribes == null)
      {
         if (other.tribes != null)
         {
            return false;
         }
      }
      else if (!tribes.equals(other.tribes))
      {
         return false;
      }
      if (username == null)
      {
         if (other.username != null)
         {
            return false;
         }
      }
      else if (!username.equals(other.username))
      {
         return false;
      }
      return true;
   }

}
