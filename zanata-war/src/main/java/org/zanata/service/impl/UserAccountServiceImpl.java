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
package org.zanata.service.impl;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Query;
import org.hibernate.Session;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountResetPasswordKeyDAO;
import org.zanata.dao.RoleAssignmentRuleDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.model.HRoleAssignmentRule;
import org.zanata.model.security.HCredentials;
import org.zanata.service.UserAccountService;
import org.zanata.util.HashUtil;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("userAccountServiceImpl")
@javax.enterprise.context.Dependent
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {
    @Inject
    private Session session;

    @Inject
    private AccountDAO accountDAO;

    @Inject
    private AccountResetPasswordKeyDAO accountResetPasswordKeyDAO;

    @Inject
    private RoleAssignmentRuleDAO roleAssignmentRuleDAO;

    @Override
    public void clearPasswordResetRequests(HAccount account) {
        HAccountResetPasswordKey key =
                accountResetPasswordKeyDAO.findByAccount(account.getId());
        if(key != null) {
            accountResetPasswordKeyDAO.makeTransient(key);
            accountResetPasswordKeyDAO.flush();
        }
    }

    @Override
    public HAccountResetPasswordKey requestPasswordReset(
            @Nonnull HAccount account) {
        if (account.getPerson() == null) {
            return null;
        }

        clearPasswordResetRequests(account);

        HAccountResetPasswordKey key = new HAccountResetPasswordKey();
        key.setAccount(account);
        key.setKeyHash(HashUtil.generateHash(account.getUsername()
            + account.getPasswordHash() + account.getPerson().getEmail()
            + account.getPerson().getName() + System.currentTimeMillis()));

        account.setAccountResetPasswordKey(key);
        accountResetPasswordKeyDAO.makePersistent(key);

        log.info("Sent password reset key to {} ({})", account.getPerson()
                .getName(), account.getUsername());
        return key;
    }

    @Override
    public HAccount runRoleAssignmentRules(HAccount account,
            HCredentials credentials, String policyName) {
        List<HRoleAssignmentRule> allRules = roleAssignmentRuleDAO.findAll();

        for (HRoleAssignmentRule rule : allRules) {
            boolean ruleMatches = false;

            if (rule.getIdentityRegExp() != null) {
                Pattern rulePattern = Pattern.compile(rule.getIdentityRegExp());
                String userName = account.getUsername();
                if (credentials != null) {
                    userName = credentials.getUser();
                }

                if (rulePattern.matcher(userName).matches()) {
                    ruleMatches = true;
                }
            }

            if (rule.getPolicyName() != null
                    && rule.getPolicyName().equals(policyName)) {
                ruleMatches = ruleMatches && true;
            }

            if (ruleMatches) {
                // apply the rule
                account.getRoles().add(rule.getRoleToAssign());
            }
        }

        HAccount persistedAcc = accountDAO.makePersistent(account);
        accountDAO.flush();
        return persistedAcc;
    }

    public void editUsername(String currentUsername, String newUsername) {
        Query updateQuery =
                session.createQuery(
                        "update HAccount set username = :newUsername where username = :currentUsername")
                        .setParameter("newUsername", newUsername)
                        .setParameter("currentUsername", currentUsername);
        updateQuery.setComment("UserAccountServiceImpl.editUsername");
        updateQuery.executeUpdate();
        session.getSessionFactory().evictQueries(); // Because a Natural Id was
                                                    // modified
    }
}
