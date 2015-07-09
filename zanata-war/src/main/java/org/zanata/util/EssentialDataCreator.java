package org.zanata.util;

import static org.zanata.model.HAccountRole.RoleType.MANUAL;

import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;

/**
 * Ensures that roles 'user', 'admin' and 'translator' exist, and that there is
 * at least one admin user.
 *
 * @author Sean Flanigan
 */
@Name("essentialDataCreator")
@Scope(ScopeType.STATELESS)
@Slf4j
@Install(false)
public class EssentialDataCreator {

    @In
    private ApplicationConfiguration applicationConfiguration;

    private boolean prepared;

    @In
    private AccountDAO accountDAO;

    @In
    private AccountRoleDAO accountRoleDAO;

    @In
    private LocaleDAO localeDAO;

    public EssentialDataCreator() {
    }

    @VisibleForTesting
    protected EssentialDataCreator(
            ApplicationConfiguration applicationConfiguration,
            AccountDAO accountDAO, AccountRoleDAO accountRoleDAO, LocaleDAO localeDAO) {
        this.applicationConfiguration = applicationConfiguration;
        this.accountDAO = accountDAO;
        this.accountRoleDAO = accountRoleDAO;
        this.localeDAO = localeDAO;
    }

    // Do it when the application starts (but after everything else has been
    // loaded)
    @Observer("org.jboss.seam.postInitialization")
    @Transactional
    public void prepare() {
        if (!prepared) {
            boolean adminExists;

            createRoleIfNotExist("project-creator");

            if(accountRoleDAO.roleExists("user")) {
                addIncludeRole("user", "project-creator");
            } else {
                createRoleIfNotExist("user", "project-creator");
            }

            createRoleIfNotExist("glossarist");
            createRoleIfNotExist("glossary-admin", "glossarist");


            if (accountRoleDAO.roleExists("admin")) {
                List<?> adminUsers = accountRoleDAO.listMembers("admin");
                adminExists = !adminUsers.isEmpty();
            } else {
                createRole("admin", "user", "glossary-admin");
                adminExists = false;
            }

            for (String adminUsername : applicationConfiguration
                    .getAdminUsers()) {
                HAccount adminAccount = accountDAO.getByUsername(adminUsername);
                HAccountRole adminRole = accountRoleDAO.findByName("admin");
                if (adminAccount != null
                        && !adminAccount.getRoles().contains(adminRole)) {
                    log.info("Making user " + adminAccount.getUsername()
                            + " a system admin.");
                    adminAccount.getRoles().add(adminRole);
                    accountDAO.makePersistent(adminAccount);
                    accountDAO.flush();
                    adminExists = true;
                }
            }

            if (!adminExists) {
                log.warn("No admin users found. Admin users can be enabled in jndi bindings: java:global/zanata/security/admin-users");
            }

            // Enable en-US by default
            LocaleId localeId = new LocaleId("en-US");
            if (localeDAO.findByLocaleId(localeId) == null) {
                HLocale en_US = new HLocale(localeId);
                en_US.setActive(true);
                en_US.setEnabledByDefault(false);
                if (localeDAO.makePersistent(en_US) == null) {
                    throw new RuntimeException("Could not create 'en-US' locale");
                }
            }

            prepared = true;
        }
    }

    private void createRoleIfNotExist(@Nonnull String role, String... includesRoles) {
        if (!accountRoleDAO.roleExists(role)) {
            createRole(role, includesRoles);
        }
    }

    private void createRole(@Nonnull String role, String... includesRoles) {
        log.info("Creating '{}' role", role);
        if (accountRoleDAO.create(role, MANUAL, includesRoles) == null) {
            throw new RuntimeException("Could not create '" + role + "' role");
        }
    }

    private void addIncludeRole(@Nonnull String role, String... includesRoles) {
        log.info("Updating '{}' to include '{}'", role, includesRoles);
        accountRoleDAO.updateIncludeRoles(role, includesRoles);
    }
}
