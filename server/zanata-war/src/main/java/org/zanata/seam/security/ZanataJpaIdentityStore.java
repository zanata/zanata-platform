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
package org.zanata.seam.security;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.AccountDAO;
import org.zanata.events.PostAuthenticateEvent;
import org.zanata.events.UserCreatedEvent;
import org.zanata.exception.IdentityManagementException;
import org.zanata.exception.NoSuchRoleException;
import org.zanata.exception.NoSuchUserException;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.security.AuthenticatedAccountHolder;
import org.zanata.security.AuthenticatedAccountSessionScopeHolder;
import org.zanata.security.Role;
import org.zanata.security.SimplePrincipal;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.util.PasswordUtil;
import org.zanata.util.ServiceLocator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This class is the replacement of seam's JpaIdentityStore. It no longer use
 * seam's annotation. e.g. UserPrincipal, UserRoles etc.
 */
@Named("identityStore")
@ApplicationScoped
public class ZanataJpaIdentityStore implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ZanataJpaIdentityStore.class);

    // see also org.zanata.model.HDocument.EntityListener.AUTHENTICATED_USER
    public static final String AUTHENTICATED_USER =
            "org.jboss.seam.security.management.authenticatedUser";
    private static final long serialVersionUID = 1L;
    @Inject
    private Event<UserCreatedEvent> userCreatedEventEvent;
    @Inject
    private Event<PostAuthenticateEvent> postAuthenticateEventEvent;
    @Inject
    private Instance<AuthenticatedAccountHolder> authenticatedAccountHolders;
    @Inject
    private EntityManager entityManager;

    private boolean apiKeyAuthenticate(String username, String apiKey) {
        HAccount user = lookupUser(username);
        if (user == null || !user.isEnabled()) {
            return false;
        }
        String userApiKey = user.getApiKey();
        if (userApiKey == null) {
            return false;
        }
        boolean success = apiKey.equals(userApiKey);
        setAuthenticateUser(user);
        return success;
    }

    /**
     * Custom authentication that ignores the account's enabled state.
     *
     * @param username
     * @param password
     * @return
     * @see {@link ZanataJpaIdentityStore#authenticate(String, String)}
     */
    public boolean checkPasswordIgnoringActivation(String username,
            String password) {
        HAccount user = lookupUser(username);
        if (user == null) {
            return false;
        }
        String passwordHash =
                generatePasswordHash(password, user.getUsername());
        return passwordHash.equals(user.getPasswordHash());
    }

    public boolean authenticate(String username, String password) {
        ZanataIdentity identity = ZanataIdentity.instance();
        if (identity.isApiRequest()) {
            return apiKeyAuthenticate(username, password);
        }
        HAccount user = lookupUser(username);
        if (user == null || !user.isEnabled()) {
            return false;
        }
        if (identity.isRequestUsingOAuth()) {
            // we've already validated accessToken in
            // org.zanata.rest.ZanataRestSecurityInterceptor
            getPostAuthenticateEvent().fire(new PostAuthenticateEvent(user));
            return true;
        } else {
            String passwordHash =
                    generatePasswordHash(password, user.getUsername());
            boolean success = passwordHash.equals(user.getPasswordHash());
            if (success) {
                getPostAuthenticateEvent()
                        .fire(new PostAuthenticateEvent(user));
            }
            return success;
        }
    }

    private Event<PostAuthenticateEvent> getPostAuthenticateEvent() {
        return postAuthenticateEventEvent;
    }

    public void setUserAccountForSession(@Observes PostAuthenticateEvent event,
            AuthenticatedAccountSessionScopeHolder holder) {
        holder.setAuthenticatedAccount(event.getAuthenticatedAccount());
    }

    public boolean isNewUser(String username) {
        HAccount user = lookupUser(username);
        // also look in the credentials table
        if (user == null) {
            AccountDAO accountDAO =
                    ServiceLocator.instance().getInstance(AccountDAO.class);
            user = accountDAO.getByCredentialsId(username);
        }
        return user == null;
    }

    public void setAuthenticateUser(HAccount user) {
        for (AuthenticatedAccountHolder accountHolder : authenticatedAccountHolders) {
            accountHolder.setAuthenticatedAccount(user);
        }
    }

    @Produces
    @Authenticated
    @Named(AUTHENTICATED_USER)
    HAccount getAuthenticatedAccount() {
        for (AuthenticatedAccountHolder accountHolder : authenticatedAccountHolders) {
            HAccount authenticatedAccount =
                    accountHolder.getAuthenticatedAccount();
            if (authenticatedAccount != null) {
                return authenticatedAccount;
            }
        }
        return null;
    }

    /**
     * Alternative producer for the Authenticated account which produces an
     * optional instead of a nullable instance.
     */
    @Produces
    @Authenticated
    Optional<HAccount>
            getAuthenticatedAccount(@Authenticated HAccount account) {
        return Optional.ofNullable(account);
    }

    public List<String> listUsers() {
        List<String> users =
                entityManager.createQuery("select u.username from HAccount u")
                        .getResultList();
        Collections.sort(users, new Comparator<String>() {

            public int compare(String value1, String value2) {
                return value1.compareTo(value2);
            }
        });
        return users;
    }

    @Transactional
    public boolean deleteUser(String name) {
        HAccount user = lookupUser(name);
        if (user == null) {
            throw new NoSuchUserException(
                    "Could not delete, user \'" + name + "\' does not exist");
        }
        entityManager.remove(user);
        return true;
    }

    public boolean isUserEnabled(String name) {
        HAccount user = lookupUser(name);
        return user != null && user.isEnabled();
    }

    public boolean userExists(String name) {
        return lookupUser(name) != null;
    }

    @Transactional
    public boolean createUser(String username, String password) {
        try {
            if (userExists(username)) {
                throw new IdentityManagementException(
                        "Could not create account, already exists");
            }
            HAccount user = new HAccount();
            user.setUsername(username);
            if (password == null) {
                user.setEnabled(false);
            } else {
                setUserPassword(user, password);
                user.setEnabled(true);
            }
            entityManager.persist(user);
            getUserCreatedEvent().fire(new UserCreatedEvent(user));
            return true;
        } catch (Exception ex) {
            if (ex instanceof IdentityManagementException) {
                throw (IdentityManagementException) ex;
            } else {
                throw new IdentityManagementException(
                        "Could not create account", ex);
            }
        }
    }

    private Event<UserCreatedEvent> getUserCreatedEvent() {
        return userCreatedEventEvent;
    }

    @Transactional
    public boolean enableUser(String name) {
        HAccount user = lookupUser(name);
        if (user == null) {
            throw new NoSuchUserException("Could not enable user, user \'"
                    + name + "\' does not exist");
        }
        // Can't enable an already-enabled user, return false
        if (user.isEnabled()) {
            return false;
        }
        user.setEnabled(true);
        return true;
    }

    @Transactional
    public boolean disableUser(String name) {
        HAccount user = lookupUser(name);
        if (user == null) {
            throw new NoSuchUserException("Could not disable user, user \'"
                    + name + "\' does not exist");
        }
        // Can't disable an already-disabled user, return false
        if (!user.isEnabled()) {
            return false;
        }
        user.setEnabled(false);
        return true;
    }

    @Transactional
    public boolean changePassword(String username, String password) {
        HAccount user = lookupUser(username);
        if (user == null) {
            throw new NoSuchUserException("Could not change password, user \'"
                    + username + "\' does not exist");
        }
        setUserPassword(user, password);
        return true;
    }

    protected void setUserPassword(HAccount user, String password) {
        user.setPasswordHash(
                generatePasswordHash(password, user.getUsername()));
    }

    protected String generatePasswordHash(String password, String salt) {
        Preconditions.checkState(!Strings.isNullOrEmpty(salt));
        return PasswordUtil.generateSaltedHash(password, salt);
    }

    public List<String> getGrantedRoles(String name) {
        HAccount user = lookupUser(name);
        if (user == null) {
            throw new NoSuchUserException("No such user \'" + name + "\'");
        }
        List<String> roles = new ArrayList<>();
        Set<HAccountRole> userRoles = user.getRoles();
        if (userRoles != null) {
            for (HAccountRole role : userRoles) {
                roles.add(role.getName());
            }
        }
        return roles;
    }

    @Transactional
    public boolean revokeRole(String username, String role) {
        HAccount user = lookupUser(username);
        if (user == null) {
            throw new NoSuchUserException(
                    "Could not revoke role, no such user \'" + username + "\'");
        }
        HAccountRole roleToRevoke = lookupRole(role);
        if (roleToRevoke == null) {
            throw new NoSuchRoleException("Could not revoke role, role \'"
                    + role + "\' does not exist");
        }
        boolean success = false;
        if (user.getRoles().contains(roleToRevoke)) {
            user.getRoles().remove(roleToRevoke);
            success = true;
        }
        return success;
    }

    public HAccount lookupUser(String username) {
        try {
            HAccount user = entityManager
                    .createQuery(
                            "select u from HAccount u where u.username = :username",
                            HAccount.class)
                    .setParameter("username", username).getSingleResult();
            return user;
        } catch (NoResultException ex) {
            return null;
        }
    }

    public HAccountRole lookupRole(String role) {
        try {
            return entityManager
                    .createQuery(
                            "select r from HAccountRole r where name = :role",
                            HAccountRole.class)
                    .setParameter("role", role).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Transactional
    public boolean grantRole(String username, String role) {
        HAccount user = lookupUser(username);
        if (user == null) {
            // We need to create a new user object
            if (createUser(username, null)) {
                user = lookupUser(username);
            } else {
                throw new IdentityManagementException(
                        "Could not grant role - user does not exist and an attempt to create the user failed.");
            }
        }
        HAccountRole roleToGrant = lookupRole(role);
        if (roleToGrant == null) {
            throw new NoSuchRoleException("Could not grant role, role \'" + role
                    + "\' does not exist");
        }
        Set<HAccountRole> userRoles = user.getRoles();
        if (userRoles == null) {
            userRoles = new HashSet<>();
            user.setRoles(userRoles);
        } else if (userRoles.contains(roleToGrant)) {
            return false;
        }
        user.getRoles().add(roleToGrant);
        return true;
    }

    public List<String> getRoleGroups(String name) {
        HAccountRole role = lookupRole(name);
        if (role == null) {
            throw new NoSuchUserException("No such role \'" + name + "\'");
        }
        List<String> groups = new ArrayList<String>();
        Collection<HAccountRole> roleGroups = role.getGroups();
        if (roleGroups != null) {
            for (HAccountRole group : roleGroups) {
                groups.add(group.getName());
            }
        }
        return groups;
    }

    public List<Principal> listMembers(String role) {
        List<Principal> members = new ArrayList<>();
        for (String user : listUserMembers(role)) {
            members.add(new SimplePrincipal(user));
        }
        for (String roleName : listRoleMembers(role)) {
            members.add(new Role(roleName));
        }
        return members;
    }

    private List<String> listUserMembers(String role) {
        HAccountRole roleEntity = lookupRole(role);
        return entityManager
                .createQuery(
                        "select u.username from HAccount u where :role member of u.roles")
                .setParameter("role", roleEntity).getResultList();
    }

    public List<String> getImpliedRoles(String name) {
        HAccount user = lookupUser(name);
        if (user == null) {
            throw new NoSuchUserException("No such user \'" + name + "\'");
        }
        Set<String> roles = new HashSet<>();
        Set<HAccountRole> userRoles = user.getRoles();
        if (userRoles != null) {
            for (HAccountRole role : userRoles) {
                addRoleAndMemberships(role.getName(), roles);
            }
        }
        return new ArrayList<>(roles);
    }

    private void addRoleAndMemberships(String role, Set<String> roles) {
        if (roles.add(role)) {
            HAccountRole instance = lookupRole(role);
            Set<HAccountRole> groups = instance.getGroups();
            if (groups != null) {
                for (HAccountRole group : groups) {
                    addRoleAndMemberships(group.getName(), roles);
                }
            }
        }
    }

    @Transactional
    public boolean deleteRole(String role) {
        HAccountRole roleToDelete = lookupRole(role);
        if (roleToDelete == null) {
            throw new NoSuchRoleException("Could not delete role, role \'"
                    + role + "\' does not exist");
        }
        List<String> roles = listRoleMembers(role);
        for (String r : roles) {
            removeRoleFromGroup(r, role);
        }
        entityManager.remove(roleToDelete);
        return true;
    }

    @Transactional
    public boolean removeRoleFromGroup(String role, String group) {
        HAccountRole roleToRemove = lookupRole(role);
        if (roleToRemove == null) {
            throw new NoSuchUserException(
                    "Could not remove role from group, no such role \'" + role
                            + "\'");
        }
        HAccountRole targetGroup = lookupRole(group);
        if (targetGroup == null) {
            throw new NoSuchRoleException(
                    "Could not remove role from group, no such group \'" + group
                            + "\'");
        }
        return roleToRemove.getGroups().remove(targetGroup);
    }

    public boolean roleExists(String name) {
        return lookupRole(name) != null;
    }

    @Transactional
    public boolean createRole(String role) {
        try {
            if (roleExists(role)) {
                throw new IdentityManagementException(
                        "Could not create role, already exists");
            }
            HAccountRole newRole = new HAccountRole();
            newRole.setName(role);
            entityManager.persist(newRole);
            return true;
        } catch (Exception ex) {
            if (ex instanceof IdentityManagementException) {
                throw (IdentityManagementException) ex;
            } else {
                throw new IdentityManagementException("Could not create role",
                        ex);
            }
        }
    }

    @Transactional
    public boolean addRoleToGroup(String role, String group) {
        HAccountRole targetRole = lookupRole(role);
        if (targetRole == null) {
            throw new NoSuchUserException(
                    "Could not add role to group, no such role \'" + role
                            + "\'");
        }
        HAccountRole targetGroup = lookupRole(group);
        if (targetGroup == null) {
            throw new NoSuchRoleException("Could not grant role, group \'"
                    + group + "\' does not exist");
        }
        Set<HAccountRole> roleGroups = targetRole.getGroups();
        if (roleGroups == null) {
            roleGroups = new HashSet<>();
            targetRole.setGroups(roleGroups);
        } else if (targetRole.getGroups().contains(targetGroup)) {
            return false;
        }
        targetRole.getGroups().add(targetGroup);
        return true;
    }

    public List<String> listGrantableRoles() {
        return entityManager
                .createQuery(
                        "select r.name from HAccountRole r where r.conditional = false")
                .getResultList();
    }

    public List<String> listRoles() {
        return entityManager.createQuery("select r.name from HAccountRole r")
                .getResultList();
    }

    private List<String> listRoleMembers(String role) {
        HAccountRole roleEntity = lookupRole(role);
        return entityManager
                .createQuery(
                        "select r.name from HAccountRole r where :role member of r.groups")
                .setParameter("role", roleEntity).getResultList();
    }
}
