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
import javax.persistence.Cacheable;
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
import javax.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.zanata.model.security.HCredentials;
import org.zanata.rest.dto.Account;

/**
 * @see Account
 */
@Entity
@Cacheable
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Indexed
public class HAccount extends ModelEntityBase
        implements Serializable, HasUserFriendlyToString {

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
    private Set<AllowedApp> allowedApps;

    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY,
            mappedBy = "account")
    public HAccountActivationKey getAccountActivationKey() {
        return accountActivationKey;
    }

    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY,
            mappedBy = "account")
    public HAccountResetPasswordKey getAccountResetPasswordKey() {
        return accountResetPasswordKey;
    }

    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    public HPerson getPerson() {
        return person;
    }
    // @UserPrincipal

    @NaturalId
    @Field
    public String getUsername() {
        return username;
    }

    @Transient
    public boolean isPersonAccount() {
        return person != null;
    }
    // @UserPassword(hash = PasswordHash.ALGORITHM_MD5)

    public String getPasswordHash() {
        return passwordHash;
    }
    // @UserEnabled

    public boolean isEnabled() {
        return enabled;
    }

    @Size(min = 32, max = 32)
    public String getApiKey() {
        return apiKey;
    }
    // @UserRoles

    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @ManyToMany(targetEntity = HAccountRole.class)
    @JoinTable(name = "HAccountMembership",
            joinColumns = @JoinColumn(name = "accountId"),
            inverseJoinColumns = @JoinColumn(name = "memberOf"))
    public Set<HAccountRole> getRoles() {
        if (roles == null) {
            roles = new HashSet<>();
            setRoles(roles);
        }
        return roles;
    }

    @OneToMany(mappedBy = "account", cascade = { CascadeType.ALL },
            orphanRemoval = true)
    public Set<HCredentials> getCredentials() {
        if (credentials == null) {
            credentials = new HashSet<>();
        }
        return credentials;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mergedInto")
    public HAccount getMergedInto() {
        return mergedInto;
    }

    @OneToMany(mappedBy = "account", cascade = { CascadeType.ALL },
            orphanRemoval = true)
    @MapKey(name = "name")
    public Map<String, HAccountOption> getEditorOptions() {
        return editorOptions;
    }

    /**
     * @return all authorized third party apps that is allowed by this user
     */
    @OneToMany(mappedBy = "account", cascade = { CascadeType.ALL },
            orphanRemoval = true)
    public Set<AllowedApp> getAllowedApps() {
        if (allowedApps == null) {
            allowedApps = new HashSet<>();
        }
        return allowedApps;
    }

    @Override
    public String userFriendlyToString() {
        return String.format("Account(username=%s, enabled=%s, roles=%s)",
                getUsername(), isEnabled(), getRoles());
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public void setPerson(final HPerson person) {
        this.person = person;
    }

    public void setRoles(final Set<HAccountRole> roles) {
        this.roles = roles;
    }

    public void setAccountActivationKey(
            final HAccountActivationKey accountActivationKey) {
        this.accountActivationKey = accountActivationKey;
    }

    public void setAccountResetPasswordKey(
            final HAccountResetPasswordKey accountResetPasswordKey) {
        this.accountResetPasswordKey = accountResetPasswordKey;
    }

    public void setCredentials(final Set<HCredentials> credentials) {
        this.credentials = credentials;
    }

    public void setMergedInto(final HAccount mergedInto) {
        this.mergedInto = mergedInto;
    }

    public void
            setEditorOptions(final Map<String, HAccountOption> editorOptions) {
        this.editorOptions = editorOptions;
    }

    public void setAllowedApps(final Set<AllowedApp> allowedApps) {
        this.allowedApps = allowedApps;
    }

    @Override
    public String toString() {
        return "HAccount(super=" + super.toString() + ", username="
                + this.getUsername() + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HAccount))
            return false;
        final HAccount other = (HAccount) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null
                : !this$username.equals(other$username))
            return false;
        final Object this$passwordHash = this.getPasswordHash();
        final Object other$passwordHash = other.getPasswordHash();
        if (this$passwordHash == null ? other$passwordHash != null
                : !this$passwordHash.equals(other$passwordHash))
            return false;
        if (this.isEnabled() != other.isEnabled())
            return false;
        final Object this$apiKey = this.getApiKey();
        final Object other$apiKey = other.getApiKey();
        if (this$apiKey == null ? other$apiKey != null
                : !this$apiKey.equals(other$apiKey))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HAccount;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $username = this.getUsername();
        result = result * PRIME
                + ($username == null ? 43 : $username.hashCode());
        final Object $passwordHash = this.getPasswordHash();
        result = result * PRIME
                + ($passwordHash == null ? 43 : $passwordHash.hashCode());
        result = result * PRIME + (this.isEnabled() ? 79 : 97);
        final Object $apiKey = this.getApiKey();
        result = result * PRIME + ($apiKey == null ? 43 : $apiKey.hashCode());
        return result;
    }
}
