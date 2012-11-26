/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.NaturalId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.Length;
import org.jboss.seam.annotations.security.management.UserEnabled;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.security.management.UserRoles;
import org.jboss.seam.security.management.PasswordHash;
import org.zanata.model.security.HCredentials;
import org.zanata.model.type.UserApiKey;
import org.zanata.rest.dto.Account;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * @see Account
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Indexed
@Setter
@ToString(callSuper = true, of = "username")
@EqualsAndHashCode(callSuper = true, of = {"enabled", "passwordHash", "username", "apiKey"})
public class HAccount extends ModelEntityBase implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String username;
   private String passwordHash;
   private boolean enabled;
   private String apiKey;
   private HPerson person;
   private Set<HAccountRole> roles;
   private HAccountActivationKey accountActivationKey;
   private HAccountResetPasswordKey accountResetPasswordKey;
   private Set<HCredentials> credentials;
   private HAccount mergedInto;
   private Map<String, HAccountOption> editorOptions;


   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "account")
   public HAccountActivationKey getAccountActivationKey()
   {
      return accountActivationKey;
   }

   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "account")
   public HAccountResetPasswordKey getAccountResetPasswordKey()
   {
      return accountResetPasswordKey;
   }

   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
   public HPerson getPerson()
   {
      return person;
   }

   // NB PERF @NaturalId(mutable=false) for better criteria caching. Mutable = true as user names can be changed.
   @NaturalId(mutable = true)
   @UserPrincipal
   @Field(index = Index.TOKENIZED)
   public String getUsername()
   {
      return username;
   }

   @Transient
   public boolean isPersonAccount()
   {
      return person != null;
   }

   @UserPassword(hash = PasswordHash.ALGORITHM_MD5)
   public String getPasswordHash()
   {
      return passwordHash;
   }

   @UserEnabled
   public boolean isEnabled()
   {
      return enabled;
   }

   @UserApiKey
   @Length(min = 32, max = 32)
   public String getApiKey()
   {
      return apiKey;
   }

   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   @UserRoles
   @ManyToMany(targetEntity = HAccountRole.class)
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

   @OneToMany(mappedBy = "account", cascade = {CascadeType.ALL})
   @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
   public Set<HCredentials> getCredentials()
   {
      if(credentials == null)
      {
         credentials = new HashSet<HCredentials>();
      }
      return credentials;
   }

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "mergedInto")
   public HAccount getMergedInto()
   {
      return mergedInto;
   }

   @OneToMany(mappedBy = "account", cascade = {CascadeType.ALL})
   @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
   @MapKey(name = "name")
   public Map<String, HAccountOption> getEditorOptions()
   {
      return editorOptions;
   }
}
