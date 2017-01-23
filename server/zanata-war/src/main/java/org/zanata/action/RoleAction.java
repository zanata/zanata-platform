package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckLoggedIn;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("zanataRoleAction")
@ViewScoped
@Model
@Transactional
@CheckLoggedIn
public class RoleAction implements Serializable {
    private static final long serialVersionUID = -3830647911484729768L;
    private String originalRole;
    private String role;
    private List<String> groups;

    @Inject
    IdentityManager identityManager;

    @Inject
    ZanataIdentity identity;

    public void loadRole() {
        if (role == null) {
            // creating new role
            groups = new ArrayList<>();
        } else {
            this.originalRole = role;
            groups = identityManager.getRoleGroups(role);
        }
    }

    public String save() {
        if (role != null && originalRole != null &&
                !role.equals(originalRole)) {
            identityManager.deleteRole(originalRole);
        }

        if (identityManager.roleExists(role)) {
            return saveExistingRole();
        } else {
            return saveNewRole();
        }
    }

    private String saveNewRole() {
        boolean success = identityManager.createRole(role);

        if (success) {
            for (String r : groups) {
                identityManager.addRoleToGroup(role, r);
            }

        }

        return "success";
    }

    private String saveExistingRole() {
        List<String> grantedRoles = identityManager.getRoleGroups(role);

        if (grantedRoles != null) {
            for (String r : grantedRoles) {
                if (!groups.contains(r)) {
                    identityManager.removeRoleFromGroup(role, r);
                }
            }
        }

        for (String r : groups) {
            if (grantedRoles == null || !grantedRoles.contains(r)) {
                identityManager.addRoleToGroup(role, r);
            }
        }

        return "success";
    }

    public String getRole() {
        return role;
    }

    public List<String> getAssignableRoles() {
        List<String> roles = identityManager.listGrantableRoles();
        roles.remove(role);
        return roles;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
