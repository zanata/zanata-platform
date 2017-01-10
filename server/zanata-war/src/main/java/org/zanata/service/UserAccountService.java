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
package org.zanata.service;

import javax.annotation.Nullable;

import org.zanata.model.HAccount;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.model.security.HCredentials;

/**
 * Business Service interface for User accounts.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface UserAccountService {
    void clearPasswordResetRequests(HAccount account);

    @Nullable HAccountResetPasswordKey requestPasswordReset(HAccount account);

    /**
     * This will generate a reset password key for an account that has the
     * matching username. Email will be used to generate the random hash key.
     * @param username username for an account
     * @param email email for that account
     * @return key that contains a randomly generated hash
     */
    HAccountResetPasswordKey requestPasswordReset(String username, String email);

    /**
     * Runs all dynamic role assignment rules against an account.
     *
     * @param account
     *            Account to run the rules against.
     * @param credentials
     *            Optional credentials with which the user logged in.
     * @param policyName
     *            The policy name used to authenticate the user.
     * @return The updated account object, which will be persistent in the
     *         databse.
     */
    HAccount runRoleAssignmentRules(HAccount account, HCredentials credentials,
            String policyName);

    /**
     * Edits an account's user name.
     *
     * @param currentUsername
     *            The account's current user name.
     * @param newUsername
     *            The new user name for the account.
     */
    void editUsername(String currentUsername, String newUsername);

    /**
     * Check to see if a username is used already.
     *
     * @param username
     *         username
     * @return true if username is already used
     */
    boolean isUsernameUsed(String username);
}
