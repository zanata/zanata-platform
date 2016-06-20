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

package org.zanata.dao;

import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.hibernate.Session;
import org.zanata.model.AllowedApp;
import org.zanata.model.HAccount;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class AllowedAppDAO extends AbstractDAOImpl<AllowedApp, Long> {

    @Inject
    private AccountDAO accountDAO;


    public AllowedAppDAO(
            Session session) {
        super(AllowedApp.class, session);
    }

    public AllowedAppDAO() {
        super(AllowedApp.class);
    }

    // TODO support client secret when we support pre-registration of clients
    public void persistClientId(String username, String clientId) {
        HAccount hAccount = accountDAO.getByUsername(username);
        AllowedApp allowedApp = new AllowedApp(hAccount, clientId);
        hAccount.getAllowedApps().add(allowedApp);

    }

    // e.g.
    // HAccount (1) -> (n) client id (1) -> (1) refresh token
    // potentially we may define scope later so different refresh may reference different scope combination
    // explanation of access token and refresh token
    // http://stackoverflow.com/questions/3487991/why-does-oauth-v2-have-both-access-and-refresh-tokens
    // and this answer: http://stackoverflow.com/a/12885823
    public void persistRefreshToken(HAccount hAccount, String clientId,
            String refreshToken) {
        AllowedApp allowedApp = (AllowedApp) getSession()
                .getNamedQuery(AllowedApp.QUERY_GET_BY_ACCOUNT_AND_CLIENT_ID)
                .setParameter("account", hAccount)
                .setParameter("clientId", clientId)
                .uniqueResult();

        if (allowedApp == null) {
            allowedApp = new AllowedApp(hAccount, clientId);
        }
        allowedApp.setRefreshToken(refreshToken);
        getSession().saveOrUpdate(allowedApp);
    }

    public Optional<HAccount> getAccountFromRefreshToken(String refreshToken) {
        AllowedApp allowedApp = (AllowedApp) getSession()
                .createQuery("from AllowedApp where refreshToken = :refreshToken")
                .setParameter("refreshToken", refreshToken)
                .uniqueResult();

        return Optional.ofNullable(allowedApp)
                .flatMap(app -> Optional.of(app.getAccount()));
    }

    public Optional<AllowedApp> getAllowedAppForAccount(HAccount account, String clientId) {
        AllowedApp allowedApp = (AllowedApp) getSession()
                .getNamedQuery(AllowedApp.QUERY_GET_BY_ACCOUNT_AND_CLIENT_ID)
                .setParameter("account", account)
                .setParameter("clientId", clientId)
                .uniqueResult();

        return Optional.ofNullable(allowedApp);
    }
}
