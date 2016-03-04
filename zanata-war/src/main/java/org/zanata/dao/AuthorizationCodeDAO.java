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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.enterprise.context.RequestScoped;

import org.hibernate.Session;
import org.zanata.model.HAccount;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class AuthorizationCodeDAO extends AbstractDAOImpl<HAccount, Long> {
    // FIXME do the real database
    private static Cache<String, Set<String>> usernameToClientIds =
            CacheBuilder.newBuilder().build();
    // FIXME persist in the database
    private static Set<ValidRefreshCode> validRefreshCodes = Sets.newHashSet();


    public AuthorizationCodeDAO(
            Session session) {
        super(HAccount.class, session);
    }

    public AuthorizationCodeDAO() {
        super(HAccount.class);
    }

    // TODO support client secret as well?
    public void persistClientId(String username, String clientId) {
        try {
            Set<String> clientIds =
                    usernameToClientIds.get(username, Sets::<String>newHashSet);
            clientIds.add(clientId);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    public Optional<HAccount> getClientIdAuthorizer(String clientId) {
        Optional<Map.Entry<String, Set<String>>> any =
                usernameToClientIds.asMap().entrySet().stream()
                        .filter(entry -> entry.getValue().contains(clientId))
                        .findAny();

        if (any.isPresent()) {
            return any.flatMap(entry -> {
                HAccount hAccount = new HAccount();
                hAccount.setUsername(entry.getKey());
                return Optional.of(hAccount);
            });
        }
        return Optional.empty();
    }

    // e.g.
    // HAccount (1) -> (n) client id (1) -> (1) refresh token
    // potentially we may define scope later so different refresh may reference different scope combination
    // explanation of access token and refresh token
    // http://stackoverflow.com/questions/3487991/why-does-oauth-v2-have-both-access-and-refresh-tokens
    // and this answer: http://stackoverflow.com/a/12885823
    public void persistRefreshToken(HAccount hAccount, String clientId,
            String refreshToken) {
        ValidRefreshCode validRefreshCode =
                new ValidRefreshCode(clientId,
                        refreshToken, hAccount.getUsername());
        validRefreshCodes.add(validRefreshCode);
    }

    public Optional<String> getUsernameFromClientIdAndFreshToken(String clientId,
            String refreshToken) {
        Optional<ValidRefreshCode> first = validRefreshCodes.stream()
                .filter(entry -> entry.clientId.equals(clientId) &&
                        entry.refreshToken.equals(refreshToken)).findFirst();
        return first.flatMap(thing -> Optional.of(thing.username));
    }

    private static class ValidRefreshCode {
        private String clientId;
        private String refreshToken;
        private String username;

        public ValidRefreshCode(String clientId,
                String refreshToken, String username) {
            this.clientId = clientId;
            this.refreshToken = refreshToken;
            this.username = username;
        }
    }
}
