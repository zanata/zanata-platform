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
package org.zanata.model.security;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.zanata.model.HAccount;
import org.zanata.model.ModelEntityBase;
import org.zanata.model.validator.Unique;

/**
 * A set of credentials for a given user against an authentication mechanism.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type",
        discriminatorType = DiscriminatorType.STRING)
@Unique(properties = { "user" })
public abstract class HCredentials extends ModelEntityBase {
    private HAccount account;
    private String user;
    private String email;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    public HAccount getAccount() {
        return account;
    }

    @Column(unique = true, nullable = false)
    public String getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public HCredentials() {
    }

    public void setAccount(final HAccount account) {
        this.account = account;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
