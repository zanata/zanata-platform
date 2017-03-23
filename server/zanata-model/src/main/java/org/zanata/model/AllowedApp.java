/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

/**
 * Represents an authorized third party app that is allowed to access Zanata on
 * behave of the associated account.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@NamedQueries({ @NamedQuery(
        name = AllowedApp.QUERY_GET_BY_ACCOUNT_AND_CLIENT_ID,
        query = "from AllowedApp where account = :account and clientId = :clientId") })
public class AllowedApp extends ModelEntityBase {
    public static final String QUERY_GET_BY_ACCOUNT_AND_CLIENT_ID =
            "AllowedApp.getByAccountAndClientId";
    @ManyToOne(targetEntity = HAccount.class, optional = false)
    @JoinColumn(name = "accountId", nullable = false)
    private HAccount account;
    @NotNull
    private String clientId;
    private String refreshToken;

    public AllowedApp(HAccount account, String clientId) {
        this.account = account;
        this.clientId = clientId;
    }

    public AllowedApp() {
    }

    public HAccount getAccount() {
        return this.account;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(final String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        if (!super.equals(other)) return false;

        AllowedApp that = (AllowedApp) other;

        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null)
            return false;
        return refreshToken != null ? refreshToken.equals(that.refreshToken) : that.refreshToken == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (refreshToken != null ? refreshToken.hashCode() : 0);
        return result;
    }
}
