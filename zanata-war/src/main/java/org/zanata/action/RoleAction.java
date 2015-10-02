package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import javax.inject.Named;
import org.jboss.seam.core.Conversation;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.ZanataSecured;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.ScopeType.PAGE;
import static org.jboss.seam.annotations.Install.APPLICATION;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("zanataRoleAction")
@javax.faces.bean.ViewScoped
@Install(precedence = APPLICATION)
@ZanataSecured
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

    public String cancel() {
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
