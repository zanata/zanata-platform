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

import javax.persistence.Access;
import javax.persistence.AccessType;
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
import org.hibernate.search.annotations.Indexed;
import javax.validation.constraints.Size;
import org.jboss.seam.annotations.security.management.UserEnabled;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.security.management.UserRoles;
import org.jboss.seam.security.management.PasswordHash;
import org.zanata.model.security.HCredentials;
import org.zanata.model.type.UserApiKey;
import org.zanata.rest.dto.Account;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@Getter
@Access(AccessType.FIELD)
@ToString(callSuper = true, of = "username")
@EqualsAndHashCode(callSuper = true, of = { "enabled", "passwordHash",
        "username", "apiKey" })
public class HAccount extends ModelEntityBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @NaturalId
    @UserPrincipal
    @Field()
    private String username;

    @UserPassword(hash = PasswordHash.ALGORITHM_MD5)
    private String passwordHash;

    @UserEnabled
    private boolean enabled;

    @UserApiKey
    @Size(min = 32, max = 32)
    private String apiKey;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private HPerson person;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @UserRoles
    @ManyToMany(targetEntity = HAccountRole.class)
    @JoinTable(name = "HAccountMembership", joinColumns = @JoinColumn(
            name = "accountId"), inverseJoinColumns = @JoinColumn(
            name = "memberOf"))
    private Set<HAccountRole> roles = Sets.newHashSet();

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY,
            mappedBy = "account")
    private HAccountActivationKey accountActivationKey;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY,
            mappedBy = "account")
    private HAccountResetPasswordKey accountResetPasswordKey;

    @OneToMany(mappedBy = "account", cascade = { CascadeType.ALL })
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<HCredentials> credentials = Sets.newHashSet();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mergedInto")
    private HAccount mergedInto;

    @OneToMany(mappedBy = "account", cascade = { CascadeType.ALL })
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @MapKey(name = "name")
    private Map<String, HAccountOption> editorOptions = Maps.newHashMap();

    @Transient
    public boolean isPersonAccount() {
        return person != null;
    }
}
