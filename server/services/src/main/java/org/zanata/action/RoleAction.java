package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.ui.faces.FacesMessages;

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
    private final static Logger log = LoggerFactory.getLogger(RoleAction.class);
    private static final long serialVersionUID = -3830647911484729768L;
    private String originalRole;
    private String role;
    private List<String> groups;
    private final int MAX_NAME_SIZE = 255;

    @Inject
    IdentityManager identityManager;

    @Inject
    ZanataIdentity identity;

    @Inject
    FacesMessages facesMessages;

    public void loadRole() {
        if (StringUtils.isBlank(role)) {
            // creating new role
            groups = new ArrayList<>();
        } else {
            this.originalRole = role;
            groups = identityManager.getRoleGroups(role);
        }
    }

    @SuppressWarnings("unused")
    public boolean validateRoleName(ValueChangeEvent event) {
        String roleName = (String) event.getNewValue();
        String componentId = event.getComponent().getId();

        if (roleName.length() > MAX_NAME_SIZE) {
            facesMessages.addToControl(componentId, "Role name too long");
            return false;
        }
        if (identityManager.roleExists(roleName)){
            facesMessages.addToControl(componentId, "Role name not available");
            return false;
        }
        if (!StringUtils.isBlank(originalRole) && !roleName.equals(originalRole)) {
            facesMessages.addToControl(componentId,
                    "Role name change not allowed");
            return false;
        }
        return true;
    }

    public String save() {
        boolean roleExists = role != null && identityManager.roleExists(role);

        if (StringUtils.isBlank(role)) {
            facesMessages.addGlobal(FacesMessage.SEVERITY_ERROR,
                    "Empty role name");
            return "failure";
        } else if (role.length() > MAX_NAME_SIZE) {
            log.warn("User {} attempted to enter an excessive role name",
                    identity.getAccountUsername());
            facesMessages.addGlobal(FacesMessage.SEVERITY_ERROR,
                    "Role name exceeds " + String.valueOf(MAX_NAME_SIZE) +
                            " characters");
            setRole(originalRole);
            return "failure";
        }

        if (roleExists && !role.equals(originalRole)) {
            log.warn("User {} attempting to overwrite another role",
                identity.getAccountUsername());
            facesMessages.addGlobal(FacesMessage.SEVERITY_ERROR,
                    "Attempted to overwrite another role");
            setRole(originalRole);
            return "failure";
        }

        if (roleExists) {
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
            if (!grantedRoles.contains(r)) {
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
