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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.hibernate.Query;
import org.hibernate.Session;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.EntityStatus;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountResetPasswordKeyDAO;
import org.zanata.dao.RoleAssignmentRuleDAO;
import org.zanata.exception.NoSuchUserException;
import org.zanata.file.DocumentStorage;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectMember;
import org.zanata.model.HRawDocument;
import org.zanata.model.HRoleAssignmentRule;
import org.zanata.model.ProjectRole;
import org.zanata.model.security.HCredentials;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.UserAccountService;
import org.zanata.util.HashUtil;
import com.google.common.collect.Lists;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("userAccountServiceImpl")
@RequestScoped
@Transactional
public class UserAccountServiceImpl implements UserAccountService {
    private static final long serialVersionUID = -6318120298386269907L;
    private static final Logger log =
            LoggerFactory.getLogger(UserAccountServiceImpl.class);

    @Inject
    private Session session;
    @Inject
    private AccountDAO accountDAO;
    @Inject
    private AccountResetPasswordKeyDAO accountResetPasswordKeyDAO;
    @Inject
    private RoleAssignmentRuleDAO roleAssignmentRuleDAO;
    @Inject
    @Authenticated
    private HAccount authenticatedUser;
    @Inject
    @DocumentStorage
    private File documentStorageFolder;

    @Override
    public void clearPasswordResetRequests(HAccount account) {
        HAccountResetPasswordKey key =
                accountResetPasswordKeyDAO.findByAccount(account.getId());
        if (key != null) {
            accountResetPasswordKeyDAO.makeTransient(key);
            accountResetPasswordKeyDAO.flush();
        }
    }

    @Override
    public HAccountResetPasswordKey
            requestPasswordReset(@Nonnull HAccount account) {
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
        key = accountResetPasswordKeyDAO.makePersistent(key);
        return key;
    }

    /**
     * @inheritDoc
     */
    @Override
    public HAccountResetPasswordKey requestPasswordReset(String username,
            String email) {
        HAccount account = accountDAO.getByUsername(username);
        if (account == null) {
            throw new NoSuchUserException(username + " can not be found");
        }
        clearPasswordResetRequests(account);
        HAccountResetPasswordKey key = new HAccountResetPasswordKey();
        key.setAccount(account);
        key.setKeyHash(
                HashUtil.generateHash(username + account.getPasswordHash()
                        + email + System.currentTimeMillis()));
        account.setAccountResetPasswordKey(key);
        key = accountResetPasswordKeyDAO.makePersistent(key);
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
            /* FIXME this does nothing, but what was the intention?
            if (rule.getPolicyName() != null
                    && rule.getPolicyName().equals(policyName)) {
                ruleMatches = ruleMatches && true;
            }
            */
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
        Query updateQuery = session
                .createQuery(
                        "update HAccount set username = :newUsername where username = :currentUsername")
                .setParameter("newUsername", newUsername)
                .setParameter("currentUsername", currentUsername);
        updateQuery.setComment("UserAccountServiceImpl.editUsername");
        updateQuery.executeUpdate();
        // Because a Natural Id was modified:
        session.getSessionFactory().getCache()
                .evictQueryRegion(AccountDAO.REGION);
    }

    @Override
    public boolean isUsernameUsed(String username) {
        Long usernameCount = (Long) session
                .createQuery(
                        "select count(*) from HAccount where username = :username")
                .setParameter("username", username)
                .setComment("UserAccountServiceImpl.isUsernameUsed")
                .uniqueResult();
        return usernameCount != 0;
    }

    @Override
    public void eraseUserData(@Nonnull HAccount account) {
        String originalUsername = account.getUsername();
        account.erase(authenticatedUser);
        HPerson person = account.getPerson();
        person.erase(authenticatedUser);

        if (account.getAccountActivationKey() != null) {
            session.delete(account.getAccountActivationKey());
        }
        if (account.getAccountResetPasswordKey() != null) {
            session.delete(account.getAccountResetPasswordKey());
        }
        if (account.getEditorOptions() != null && !account.getEditorOptions().isEmpty()) {
            account.getEditorOptions().clear();
            session.merge(account);
        }
        account.getCredentials().forEach(c -> session.delete(c));

        // remove all review comment
        session.createQuery("delete from HTextFlowTargetReviewComment c where c.commenter = :commenter")
                .setParameter("commenter", person)
                .executeUpdate();

        // remove from language team
        person.getLanguageTeamMemberships()
                .forEach(membership -> session.delete(membership));
        // remove from version group
        List<HIterationGroup> soleMaintainerGroups = Lists.newLinkedList();
        person.getMaintainerVersionGroups().forEach(group -> {
            if (group.getMaintainers().size() == 1) {
                soleMaintainerGroups.add(group);
            }
            group.getMaintainers().remove(person);
        });
        session.flush();
        soleMaintainerGroups.forEach(group -> {
            // for some reason below doesn't work!!! even though it triggers preRemove callback
//            session.delete(group);
            session.createQuery("delete from HIterationGroup g where g.id = :id")
                    .setParameter("id", group.getId())
                    .executeUpdate();

        });
        session.flush();
        // remove from project membership
        // delete projects that is solely maintained by this user
        List<HProjectMember> soleMaintainerProjects = session.createQuery(
                "from HProjectMember pm where pm.person = :person and pm.role = :role and 1 = (select count(*) as n from HProjectMember pm1 where pm1.role = :role and pm1.project = pm.project)")
                .setParameter("person", person)
                .setParameter("role", ProjectRole.Maintainer)
                .list();

        List<HProject> projectsToDelete = Lists.newLinkedList();
        soleMaintainerProjects.forEach(pm -> {
            projectsToDelete.add(pm.getProject());
            session.delete(pm);
        });
        // soft delete the project and its versions
        projectsToDelete.forEach(p -> {
            p.setStatus(EntityStatus.OBSOLETE);
            p.setSlug(p.changeToDeletedSlug());
            p.getProjectIterations().forEach(v -> {
                v.setStatus(EntityStatus.OBSOLETE);
                v.setSlug(v.changeToDeletedSlug());
            });
        });
        session.flush();

        // remove rest of the project membership link for this user
        session.createQuery("delete from HProjectMember pm where pm.person = :person")
                .setParameter("person", person)
                .executeUpdate();

        // mark HRawDocument as deleted and delete raw document files on disk
        List<HRawDocument> rawDocs = session.createQuery(
                "from HRawDocument doc where doc.uploadedBy = :username")
                .setParameter("username", originalUsername)
                .list();
        rawDocs.forEach(doc -> {
            File fileOnDisk = new File(documentStorageFolder, doc.getFileId());
            try {
                Files.deleteIfExists(fileOnDisk.toPath());
            } catch (IOException e) {
                log.warn("unable to delete {}", fileOnDisk);
            }
            doc.getDocument().setRawDocument(null);
            session.update(doc.getDocument());
            session.delete(doc);
        });
        session.flush();
    }
}
