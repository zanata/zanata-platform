package org.zanata.seam.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.zanata.security.ZanataIdentity;
import org.zanata.util.Contexts;
import org.zanata.util.ServiceLocator;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;


/**
 * Based on seam's IdentityManager.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@javax.enterprise.context.RequestScoped
@Named("identityManager")
@Slf4j
public class IdentityManager implements Serializable {
    public static final String USER_PERMISSION_NAME = "seam.user";
    public static final String ROLE_PERMISSION_NAME = "seam.role";

    public static final String PERMISSION_CREATE = "create";
    public static final String PERMISSION_READ = "read";
    public static final String PERMISSION_UPDATE = "update";
    public static final String PERMISSION_DELETE = "delete";
    private static final long serialVersionUID = 8306433833437687248L;

    private ZanataJpaIdentityStore identityStore;

    @PostConstruct
    public void create() {
        if (identityStore == null) {
            identityStore =
                    ServiceLocator.instance().getInstance(
                            ZanataJpaIdentityStore.class);
        }
    }

    // TODO [CDI] revisit this
    public static IdentityManager instance() {
        if (!Contexts.isRequestContextActive()) {
            throw new IllegalStateException("No active request context");
        }
        return ServiceLocator.instance().getInstance(
                IdentityManager.class);
    }

    public boolean createUser(String name, String password) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_CREATE);
        return identityStore.createUser(name, password);
    }

    public boolean deleteUser(String name) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_DELETE);
        return identityStore.deleteUser(name);
    }

    public boolean enableUser(String name) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_UPDATE);
        return identityStore.enableUser(name);
    }

    public boolean disableUser(String name) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_UPDATE);
        return identityStore.disableUser(name);
    }

    public boolean changePassword(String name, String password) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_UPDATE);
        return identityStore.changePassword(name, password);
    }

    public boolean isUserEnabled(String name) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_READ);
        return identityStore.isUserEnabled(name);
    }

    public boolean grantRole(String name, String role) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_UPDATE);
        return identityStore.grantRole(name, role);
    }

    public boolean revokeRole(String name, String role) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_UPDATE);
        return identityStore.revokeRole(name, role);
    }

    public boolean createRole(String role) {
        ZanataIdentity.instance().checkPermission(ROLE_PERMISSION_NAME,
                PERMISSION_CREATE);
        return identityStore.createRole(role);
    }

    public boolean deleteRole(String role) {
        ZanataIdentity.instance().checkPermission(ROLE_PERMISSION_NAME,
                PERMISSION_DELETE);
        return identityStore.deleteRole(role);
    }

    public boolean addRoleToGroup(String role, String group) {
        ZanataIdentity.instance().checkPermission(ROLE_PERMISSION_NAME,
                PERMISSION_UPDATE);
        return identityStore.addRoleToGroup(role, group);
    }

    public boolean removeRoleFromGroup(String role, String group) {
        ZanataIdentity.instance().checkPermission(ROLE_PERMISSION_NAME,
                PERMISSION_UPDATE);
        return identityStore.removeRoleFromGroup(role, group);
    }

    public boolean userExists(String name) {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_READ);
        return identityStore.userExists(name);
    }

    public boolean roleExists(String name) {
        return identityStore.roleExists(name);
    }

    public List<String> listUsers() {
        ZanataIdentity.instance().checkPermission(USER_PERMISSION_NAME,
                PERMISSION_READ);
        List<String> users = identityStore.listUsers();

        Collections.sort(users, new Comparator<String>() {
            public int compare(String value1, String value2) {
                return value1.compareTo(value2);
            }
        });

        return users;
    }

    public List<String> listRoles() {
        ZanataIdentity.instance().checkPermission(ROLE_PERMISSION_NAME,
                PERMISSION_READ);
        List<String> roles = identityStore.listRoles();

        Collections.sort(roles, new Comparator<String>() {
            public int compare(String value1, String value2) {
                return value1.compareTo(value2);
            }
        });

        return roles;
    }

    public List<String> listGrantableRoles() {
        List<String> roles = identityStore.listGrantableRoles();

        Collections.sort(roles, new Comparator<String>() {
            public int compare(String value1, String value2) {
                return value1.compareTo(value2);
            }
        });

        return roles;
    }

    /**
     * Returns a list of the roles that are explicitly granted to the specified
     * user;
     *
     * @param name
     *            The user for which to return a list of roles
     * @return List containing the names of the granted roles
     */
    public List<String> getGrantedRoles(String name) {
        return identityStore.getGrantedRoles(name);
    }

    /**
     * Returns a list of roles that are either explicitly or indirectly granted
     * to the specified user.
     *
     * @param name
     *            The user for which to return the list of roles
     * @return List containing the names of the implied roles
     */
    public List<String> getImpliedRoles(String name) {
        return identityStore.getImpliedRoles(name);
    }

    public List<Principal> listMembers(String role) {
        ZanataIdentity.instance().checkPermission(ROLE_PERMISSION_NAME,
                PERMISSION_READ);
        return identityStore.listMembers(role);
    }

    public List<String> getRoleGroups(String name) {
        return identityStore.getRoleGroups(name);
    }

    public boolean authenticate(String username, String password) {
        if (Strings.isNullOrEmpty(username)) {
            return false;
        }
        return identityStore.authenticate(username, password);
    }

    public boolean isEnabled() {
        return identityStore != null;
    }
}
