package org.zanata.util;

import static org.zanata.model.HAccountRole.RoleType.MANUAL;

import java.util.List;

import javax.persistence.EntityManager;

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
    private EntityManager entityManager;

    @In
    private ApplicationConfiguration applicationConfiguration;

    private boolean prepared;

    public String username;
    public String password;
    public String email;
    public String name;
    public String apiKey;

    @In
    AccountDAO accountDAO;

    @In
    AccountRoleDAO accountRoleDAO;

    @In
    LocaleDAO localeDAO;

    // Do it when the application starts (but after everything else has been
    // loaded)
    @Observer("org.jboss.seam.postInitialization")
    @Transactional
    public void prepare() {
        if (!prepared) {
            boolean adminExists;
            if (!accountRoleDAO.roleExists("user")) {
                log.info("Creating 'user' role");
                if (accountRoleDAO.create("user", MANUAL) == null) {
                    throw new RuntimeException("Couldn't create 'user' role");
                }
            }

            if (!accountRoleDAO.roleExists("glossarist")) {
                log.info("Creating 'glossarist' role");
                if (accountRoleDAO.create("glossarist", MANUAL) == null) {
                    throw new RuntimeException(
                            "Couldn't create 'glossarist' role");
                }
            }
            if (!accountRoleDAO.roleExists("glossary-admin")) {
                log.info("Creating 'glossary-admin' role");
                if (accountRoleDAO.create("glossary-admin", MANUAL,
                        "glossarist") == null) {
                    throw new RuntimeException(
                            "Couldn't create 'glossary-admin' role");
                }
            }

            if (accountRoleDAO.roleExists("admin")) {
                List<?> adminUsers = accountRoleDAO.listMembers("admin");
                adminExists = !adminUsers.isEmpty();
            } else {
                log.info("Creating 'admin' role");
                if (accountRoleDAO.create("admin", MANUAL, "user",
                        "glossary-admin") == null) {
                    throw new RuntimeException("Couldn't create 'admin' role");
                }
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
                log.warn("No admin users found. Admin users can be enabled in zanata.properties");
            }

            // Enable en-US by default
            LocaleId localeId = new LocaleId("en-US");
            if (localeDAO.findByLocaleId(localeId) == null) {
                HLocale en_US = new HLocale(localeId);
                en_US.setActive(true);
                en_US.setEnabledByDefault(false);
                if (localeDAO.makePersistent(en_US) == null) {
                    throw new RuntimeException("Couldn't create 'en-US' locale");
                }
            }

            prepared = true;
        }
    }

}
